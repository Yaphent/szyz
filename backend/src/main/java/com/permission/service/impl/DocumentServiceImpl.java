package com.permission.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.permission.common.PageResult;
import com.permission.dto.DocumentDTO;
import com.permission.dto.DocumentQueryDTO;
import com.permission.dto.DocumentVO;
import com.permission.dto.DifyDocumentProcessResult;
import com.permission.dto.FileUploadVO;
import com.permission.entity.Document;
import com.permission.entity.DocumentFile;
import com.permission.entity.DocumentTag;
import com.permission.exception.BusinessException;
import com.permission.mapper.DocumentFileMapper;
import com.permission.mapper.DocumentMapper;
import com.permission.mapper.DocumentTagMapper;
import com.permission.service.DocumentService;
import com.permission.service.DifyAIService;
import com.permission.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 法规文档服务实现
 *
 * <p>负责文档主表、文件关联、标签关联以及文件存储、解析等核心业务。</p>
 */
@Slf4j
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {

    /** 单文件大小上限 50MB */
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    /** 支持的文件扩展名 */
    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList(
            "html", "htm", "doc", "docx", "pdf", "txt", "xls", "xlsx", "md"
    ));

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private DocumentFileMapper documentFileMapper;

    @Autowired
    private DocumentTagMapper documentTagMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private DifyAIService difyAIService;

    // ================================================================
    // 查询
    // ================================================================

    @Override
    public PageResult<DocumentVO> pageQuery(DocumentQueryDTO query) {
        // 参数兜底
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        IPage<DocumentVO> page = new Page<>(pageNum, pageSize);
        IPage<DocumentVO> result = documentMapper.selectDocumentPage(page, query);

        // 填充每条记录的标签
        List<DocumentVO> records = result.getRecords();
        if (records != null && !records.isEmpty()) {
            List<Long> ids = records.stream().map(DocumentVO::getId).collect(Collectors.toList());
            List<DocumentTag> tagList = documentTagMapper.selectList(
                    new LambdaQueryWrapper<DocumentTag>().in(DocumentTag::getDocumentId, ids));
            for (DocumentVO vo : records) {
                List<String> tags = tagList.stream()
                        .filter(t -> Objects.equals(t.getDocumentId(), vo.getId()))
                        .map(DocumentTag::getTagName)
                        .collect(Collectors.toList());
                vo.setTags(tags);
            }
        }

        return new PageResult<>(records, result.getTotal(), pageNum, pageSize);
    }

    @Override
    public DocumentVO detail(Long id) {
        Document doc = this.getById(id);
        if (doc == null) {
            throw new BusinessException(404, "文档不存在");
        }
        DocumentVO vo = new DocumentVO();
        BeanUtils.copyProperties(doc, vo);

        // 主文件
        if (doc.getMainFileId() != null) {
            DocumentFile main = documentFileMapper.selectById(doc.getMainFileId());
            vo.setMainFile(main);
        }

        // 附件列表（file_category = 2）
        List<DocumentFile> attachments = documentFileMapper.selectList(
                new LambdaQueryWrapper<DocumentFile>()
                        .eq(DocumentFile::getDocumentId, id)
                        .eq(DocumentFile::getFileCategory, 2)
                        .orderByAsc(DocumentFile::getUploadTime));
        vo.setAttachments(attachments);

        // 标签
        List<DocumentTag> tags = documentTagMapper.selectList(
                new LambdaQueryWrapper<DocumentTag>().eq(DocumentTag::getDocumentId, id));
        vo.setTags(tags.stream().map(DocumentTag::getTagName).collect(Collectors.toList()));

        return vo;
    }

    // ================================================================
    // 新增 / 更新
    // ================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DocumentDTO dto) {
        validateDates(dto);

        Document entity = new Document();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(null);
        entity.setStatus(1);
        entity.setDeleted(0);
        // 初始化 Dify 相关字段
        entity.setDifyDocumentId(null);
        entity.setSummaryStatus(0); // 待处理
        entity.setVectorizationStatus(0); // 待处理
        this.save(entity);

        // 关联主文件
        if (dto.getMainFileId() != null) {
            linkFile(dto.getMainFileId(), entity.getId(), 1);
        }

        // 关联附件
        if (dto.getAttachmentIds() != null) {
            for (Long fid : dto.getAttachmentIds()) {
                linkFile(fid, entity.getId(), 2);
            }
        }

        // 标签
        saveTags(entity.getId(), dto.getTags());

        // 异步处理 Dify 摘要抽取和向量化
        processDocumentWithDifyAsync(entity.getId());

        log.info("文档新增成功, id={}, name={}", entity.getId(), entity.getName());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DocumentDTO dto) {
        Document exists = this.getById(id);
        if (exists == null) {
            throw new BusinessException(404, "文档不存在");
        }
        validateDates(dto);

        Document entity = new Document();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        this.updateById(entity);

        // 主文件变更：将旧主文件解绑（保留记录但改为附件或清除）
        if (dto.getMainFileId() != null && !Objects.equals(dto.getMainFileId(), exists.getMainFileId())) {
            linkFile(dto.getMainFileId(), id, 1);
        }

        // 附件：先删除原有附件关系，再重新建立
        documentFileMapper.delete(new LambdaQueryWrapper<DocumentFile>()
                .eq(DocumentFile::getDocumentId, id)
                .eq(DocumentFile::getFileCategory, 2));
        if (dto.getAttachmentIds() != null) {
            for (Long fid : dto.getAttachmentIds()) {
                linkFile(fid, id, 2);
            }
        }

        // 标签：覆盖式更新
        documentTagMapper.delete(new LambdaQueryWrapper<DocumentTag>().eq(DocumentTag::getDocumentId, id));
        saveTags(id, dto.getTags());

        // 如果主文件发生变化，重新处理 Dify 摘要抽取和向量化
        if (dto.getMainFileId() != null && !Objects.equals(dto.getMainFileId(), exists.getMainFileId())) {
            // 删除旧的 Dify 文档
            if (exists.getDifyDocumentId() != null) {
                difyAIService.deleteDocumentFromKnowledgeBase(exists.getDifyDocumentId());
            }
            // 重新处理新的文档
            processDocumentWithDifyAsync(id);
        }

        log.info("文档更新成功, id={}", id);
    }

    // ================================================================
    // 删除 / 状态
    // ================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        // 1. 查询文档信息，获取 Dify 文档 ID
        Document document = this.getById(id);
        String difyDocumentId = document != null ? document.getDifyDocumentId() : null;
        
        // 2. 查询关联的文件
        List<DocumentFile> files = documentFileMapper.selectList(
                new LambdaQueryWrapper<DocumentFile>().eq(DocumentFile::getDocumentId, id));
        
        // 3. 删除文档记录
        if (!this.removeById(id)) {
            throw new BusinessException("删除失败");
        }
        
        // 4. 删除服务器上的文件
        for (DocumentFile file : files) {
            try {
                fileStorageService.delete(file.getFilePath());
                log.info("删除服务器文件成功: {}", file.getFilePath());
            } catch (Exception e) {
                log.error("删除服务器文件失败: {}", file.getFilePath(), e);
            }
        }
        
        // 5. 删除 Dify 知识库中的文档
        if (difyDocumentId != null) {
            try {
                difyAIService.deleteDocumentFromKnowledgeBase(difyDocumentId);
                log.info("删除 Dify 知识库中的文档成功: {}", difyDocumentId);
            } catch (Exception e) {
                log.error("删除 Dify 知识库中的文档失败: {}", difyDocumentId, e);
            }
        }
        
        log.info("文档已删除, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("ID 列表不能为空");
        }
        
        // 1. 查询所有关联的文档，获取 Dify 文档 ID
        List<Document> documents = this.list(new LambdaQueryWrapper<Document>().in(Document::getId, ids));
        List<String> difyDocumentIds = documents.stream()
                .map(Document::getDifyDocumentId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toList());
        
        // 2. 查询所有关联的文件
        List<DocumentFile> files = documentFileMapper.selectList(
                new LambdaQueryWrapper<DocumentFile>().in(DocumentFile::getDocumentId, ids));
        
        // 3. 批量删除文档记录
        this.removeByIds(ids);
        
        // 4. 删除服务器上的文件
        for (DocumentFile file : files) {
            try {
                fileStorageService.delete(file.getFilePath());
                log.info("删除服务器文件成功: {}", file.getFilePath());
            } catch (Exception e) {
                log.error("删除服务器文件失败: {}", file.getFilePath(), e);
            }
        }
        
        // 5. 删除 Dify 知识库中的文档
        for (String difyDocumentId : difyDocumentIds) {
            try {
                difyAIService.deleteDocumentFromKnowledgeBase(difyDocumentId);
                log.info("删除 Dify 知识库中的文档成功: {}", difyDocumentId);
            } catch (Exception e) {
                log.error("删除 Dify 知识库中的文档失败: {}", difyDocumentId, e);
            }
        }
        
        log.info("批量删除文档, ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFile(Long fileId) {
        DocumentFile file = documentFileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException("文件不存在");
        }
        
        // 检查是否为主文件，如果是，需要处理相关的 Dify 文档
        if (file.getFileCategory() != null && file.getFileCategory() == 1) { // 主文件
            // 查找使用此文件作为主文件的文档
            Document document = this.getOne(new LambdaQueryWrapper<Document>()
                    .eq(Document::getMainFileId, file.getId()));
            if (document != null && document.getDifyDocumentId() != null) {
                // 删除 Dify 知识库中的文档
                try {
                    difyAIService.deleteDocumentFromKnowledgeBase(document.getDifyDocumentId());
                    log.info("删除 Dify 知识库中的文档成功: {}", document.getDifyDocumentId());
                    
                    // 清除文档的 Dify 相关字段
                    Document updateDoc = new Document();
                    updateDoc.setId(document.getId());
                    updateDoc.setDifyDocumentId(null);
                    updateDoc.setSummary(null);
                    updateDoc.setSummaryStatus(0);
                    updateDoc.setVectorizationStatus(0);
                    this.updateById(updateDoc);
                } catch (Exception e) {
                    log.error("删除 Dify 知识库中的文档失败: {}", document.getDifyDocumentId(), e);
                }
            }
        }
        
        // 1. 删除服务器上的文件
        try {
            fileStorageService.delete(file.getFilePath());
            log.info("删除服务器文件成功: {}", file.getFilePath());
        } catch (Exception e) {
            log.error("删除服务器文件失败: {}", file.getFilePath(), e);
        }
        
        // 2. 删除文件记录
        documentFileMapper.deleteById(fileId);
        log.info("文件记录已删除, fileId={}", fileId);
    }

    @Override
    public void vectorizeDocument(Long id) {
        Document document = this.getById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        // 检查文档是否有主文件
        if (document.getMainFileId() == null) {
            throw new BusinessException("文档没有主文件，无法进行向量化处理");
        }

        // 异步处理文档的 Dify 向量化
        processDocumentWithDifyAsync(id);
    }

    @Override
    public void deleteVectorizedDocument(Long id) {
        Document document = this.getById(id);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        String difyDocumentId = document.getDifyDocumentId();
        if (difyDocumentId != null) {
            // 删除 Dify 知识库中的文档
            try {
                difyAIService.deleteDocumentFromKnowledgeBase(difyDocumentId);
                log.info("删除 Dify 知识库中的文档成功: {}", difyDocumentId);
                
                // 清除文档的 Dify 相关字段
                Document updateDoc = new Document();
                updateDoc.setId(id);
                updateDoc.setDifyDocumentId(null);
                updateDoc.setSummary(null);
                updateDoc.setSummaryStatus(0);
                updateDoc.setVectorizationStatus(0);
                this.updateById(updateDoc);
            } catch (Exception e) {
                log.error("删除 Dify 知识库中的文档失败: {}", difyDocumentId, e);
                throw new BusinessException("删除向量化数据失败: " + e.getMessage());
            }
        } else {
            // 如果文档没有 Dify ID，只需更新状态
            Document updateDoc = new Document();
            updateDoc.setId(id);
            updateDoc.setDifyDocumentId(null);
            updateDoc.setSummary(null);
            updateDoc.setSummaryStatus(0);
            updateDoc.setVectorizationStatus(0);
            this.updateById(updateDoc);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("ID 列表不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值非法");
        }
        
        // 获取需要更新状态的文档信息
        List<Document> documents = this.list(new LambdaQueryWrapper<Document>().in(Document::getId, ids));
        
        LambdaUpdateWrapper<Document> wrapper = new LambdaUpdateWrapper<Document>()
                .in(Document::getId, ids)
                .set(Document::getStatus, status);
        this.update(wrapper);
        
        // 根据状态更新 Dify 知识库中的文档
        for (Document document : documents) {
            if (document.getDifyDocumentId() != null) {
                try {
                    if (status == 0) { // 停用
                        difyAIService.disableDocumentInKnowledgeBase(document.getDifyDocumentId());
                        log.info("已停用 Dify 知识库中的文档: {}", document.getDifyDocumentId());
                    } else { // 启用
                        difyAIService.enableDocumentInKnowledgeBase(document.getDifyDocumentId());
                        log.info("已启用 Dify 知识库中的文档: {}", document.getDifyDocumentId());
                    }
                } catch (Exception e) {
                    log.error("更新 Dify 知识库中文档状态失败: {}", document.getDifyDocumentId(), e);
                }
            }
        }
        
        log.info("批量修改文档状态, ids={}, status={}", ids, status);
    }

    // ================================================================
    // 文件上传 / 下载
    // ================================================================

    @Override
    public FileUploadVO uploadFile(MultipartFile file, Integer category) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过 50MB");
        }

        String originalName = file.getOriginalFilename();
        String ext = getExtension(originalName);
        if (!ALLOWED_EXT.contains(ext.toLowerCase())) {
            throw new BusinessException("不支持的文件格式: " + ext);
        }

        // 生成对象存储路径：document/yyyyMM/uuid.ext
        String datePath = LocalDate.now().toString().substring(0, 7).replace("-", "");
        String objectName = "document/" + datePath + "/" + IdUtil.simpleUUID() + "." + ext;

        String url = fileStorageService.upload(file, objectName);

        DocumentFile entity = new DocumentFile();
        entity.setFileName(originalName);
        entity.setFilePath(objectName);
        entity.setFileUrl(url);
        entity.setFileSize(file.getSize());
        entity.setFileType(ext);
        entity.setFileCategory(category == null ? 2 : category);
        entity.setStorageType(fileStorageService.getStorageType());
        entity.setUploadTime(LocalDateTime.now());
        documentFileMapper.insert(entity);

        FileUploadVO vo = new FileUploadVO();
        vo.setFileId(entity.getId());
        vo.setFileName(originalName);
        vo.setFileUrl(url);
        vo.setFileSize(file.getSize());
        vo.setFileType(ext);
        vo.setStorageType(entity.getStorageType());
        return vo;
    }

    @Override
    public InputStream downloadFile(Long fileId, DocumentFile[] holder) {
        DocumentFile file = documentFileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException(404, "文件不存在");
        }
        if (holder != null && holder.length > 0) {
            holder[0] = file;
        }
        return fileStorageService.download(file.getFilePath());
    }

    // ================================================================
    // 辅助方法
    // ================================================================

    /** 校验日期逻辑 */
    private void validateDates(DocumentDTO dto) {
        if (dto.getExpiryDate() != null && dto.getEffectiveDate() != null
                && dto.getExpiryDate().isBefore(dto.getEffectiveDate())) {
            throw new BusinessException("失效日期必须晚于实施日期");
        }
    }

    /** 将已上传文件关联到文档 */
    private void linkFile(Long fileId, Long documentId, Integer category) {
        DocumentFile file = documentFileMapper.selectById(fileId);
        if (file == null) {
            log.warn("关联文件失败, fileId={} 不存在", fileId);
            return;
        }
        file.setDocumentId(documentId);
        file.setFileCategory(category);
        documentFileMapper.updateById(file);
    }

    /** 保存标签关联 */
    private void saveTags(Long documentId, List<String> tags) {
        if (tags == null || tags.isEmpty() || documentId == null) {
            return;
        }
        List<String> distinct = tags.stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        for (String tag : distinct) {
            DocumentTag dt = new DocumentTag();
            dt.setDocumentId(documentId);
            dt.setTagName(tag);
            documentTagMapper.insert(dt);
        }
    }

    /** 获取文件扩展名 */
    private String getExtension(String fileName) {
        if (StrUtil.isBlank(fileName)) {
            return "";
        }
        int idx = fileName.lastIndexOf('.');
        return idx < 0 ? "" : fileName.substring(idx + 1);
    }

    /**
     * 异步处理文档的 Dify 摘要抽取和向量化
     */
    private void processDocumentWithDifyAsync(Long documentId) {
        // 使用线程池异步处理，避免阻塞主流程
        new Thread(() -> {
            try {
                // 更新状态为处理中
                updateDifyProcessingStatus(documentId, 1, 1);

                // 获取文档详情
                Document document = this.getById(documentId);
                if (document == null) {
                    log.error("文档不存在，ID: {}", documentId);
                    updateDifyProcessingStatus(documentId, 3, 3); // 设置为失败状态
                    return;
                }

                // 获取主文件内容
                String content = getDocumentContent(document);
                if (content == null || content.isEmpty()) {
                    log.warn("文档内容为空，ID: {}", documentId);
                    updateDifyProcessingStatus(documentId, 3, 3); // 设置为失败状态
                    return;
                }

                // 调用 Dify 服务处理文档
                DifyDocumentProcessResult result = difyAIService.processDocument(
                    document.getName(), content);

                if (result.getSuccess()) {
                    // 更新文档的 Dify 相关字段
                    Document updateDoc = new Document();
                    updateDoc.setId(documentId);
                    updateDoc.setDifyDocumentId(result.getDocumentId());
                    updateDoc.setSummary(result.getSummary());
                    updateDoc.setSummaryStatus(2); // 设置为已完成
                    updateDoc.setVectorizationStatus(2); // 设置为已完成
                    this.updateById(updateDoc);

                    log.info("Dify 处理文档成功，文档ID: {}, Dify文档ID: {}", documentId, result.getDocumentId());
                } else {
                    log.error("Dify 处理文档失败，文档ID: {}，错误信息: {}", documentId, result.getMessage());
                    updateDifyProcessingStatus(documentId, 3, 3); // 设置为失败状态
                }
            } catch (Exception e) {
                log.error("异步处理 Dify 文档时发生异常，文档ID: {}", documentId, e);
                updateDifyProcessingStatus(documentId, 3, 3); // 设置为失败状态
            }
        }).start();
    }

    /**
     * 更新文档的 Dify 处理状态
     */
    private void updateDifyProcessingStatus(Long documentId, Integer summaryStatus, Integer vectorizationStatus) {
        Document updateDoc = new Document();
        updateDoc.setId(documentId);
        updateDoc.setSummaryStatus(summaryStatus);
        updateDoc.setVectorizationStatus(vectorizationStatus);
        this.updateById(updateDoc);
    }

    /**
     * 获取文档内容
     */
    private String getDocumentContent(Document document) {
        if (document.getMainFileId() == null) {
            return null;
        }

        // 获取主文件信息
        DocumentFile mainFile = documentFileMapper.selectById(document.getMainFileId());
        if (mainFile == null) {
            return null;
        }

        // 从文件存储服务获取文件内容
        try (InputStream inputStream = fileStorageService.download(mainFile.getFilePath())) {
            return new java.util.Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();
        } catch (Exception e) {
            log.error("获取文件内容失败，文件路径: {}", mainFile.getFilePath(), e);
            return null;
        }
    }
}
