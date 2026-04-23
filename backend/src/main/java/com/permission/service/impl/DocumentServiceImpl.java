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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.DisposableBean;

/**
 * 法规文档服务实现
 *
 * <p>负责文档主表、文件关联、标签关联以及文件存储、解析等核心业务。</p>
 */
@Slf4j
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService, DisposableBean {
    
    private final int coreSize = Runtime.getRuntime().availableProcessors() * 4; // 4倍经验值
    private final int maxSize = coreSize * 2;
    private final long keepAlive = 60L;
    private final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(5000); // 有界队列

    ThreadPoolExecutor executorService = new ThreadPoolExecutor(
        coreSize, maxSize,
        keepAlive, TimeUnit.SECONDS,
        workQueue,
        Executors.defaultThreadFactory(), // 给线程池命名
       new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：避免抛弃关键任务
    );

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
    
    @Value("${dify.dataset.id:}")
    private String datasetId;

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
        this.save(entity);

        // 关联主文件
        if (dto.getMainFileId() != null) {
            linkFile(dto.getMainFileId(), entity.getId(), 1);
        }



        // 标签
        saveTags(entity.getId(), dto.getTags());

        // 异步处理 Dify 摘要抽取和向量化
        processDocumentWithDifyAsync(entity.getId());
        
        // 如果有主文件，异步上传到 Dify 知识流水线
        if (dto.getMainFileId() != null) {
            uploadFileToDifyPipelineAsync(dto.getMainFileId(), entity.getId());
        }

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
    public FileUploadVO uploadFileToDifyPipeline(MultipartFile file, Integer category) {
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

        // 上传文件到 Dify 知识流水线
        java.util.Map<String, Object> difyResult = difyAIService.uploadPipelineFile(file);
        
        // 检查 Dify 上传是否成功
        if (difyResult.containsKey("error")) {
            log.error("上传文件到 Dify 知识流水线失败: {}", difyResult.get("error"));
            // 即使 Dify 上传失败，也要保存文件到本地
        }

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

        // 如果 Dify 上传成功，保存相关信息到文档表
        if (!difyResult.containsKey("error")) {
            // 创建一个文档记录来保存 Dify 相关信息
            Document document = new Document();
            document.setName(originalName);
            document.setBusinessType("FILE_UPLOAD"); // 设置为文件上传类型
            document.setIssuingAuthority("SYSTEM"); // 系统自动创建
            document.setIssuingDate(LocalDate.now());
            document.setEffectiveDate(LocalDate.now());
            document.setControlMode("UNIFIED");
            document.setStatus(1);
            document.setDeleted(0);
            
            // 设置 Dify 相关字段
            // 根据 Dify API 响应格式调整字段映射
            if (difyResult.containsKey("id")) {
                document.setDifyFileId((String) difyResult.get("id"));
            } else if (difyResult.containsKey("document_id")) {
                document.setDifyFileId((String) difyResult.get("document_id"));
            }
            if (difyResult.containsKey("url")) {
                document.setDifyFileUrl((String) difyResult.get("url"));
            }
            document.setDifyProcessingStatus("pending"); // 初始状态设为待处理
            document.setDifyUploadTime(LocalDateTime.now());
            
            // 如果有其他 Dify 返回的信息，也可以保存
            if (difyResult.containsKey("size")) {
                document.setDifySegmentCount(((Integer) difyResult.get("size")).intValue());
            } else if (difyResult.containsKey("segment_count")) {
                document.setDifySegmentCount(((Integer) difyResult.get("segment_count")).intValue());
            }
            
            // 保存原始文件信息
            if (difyResult.containsKey("original_filename")) {
                document.setName((String) difyResult.get("original_filename"));
            }
            if (difyResult.containsKey("file_size")) {
                Object fileSizeObj = difyResult.get("file_size");
                if (fileSizeObj instanceof Long) {
                    document.setDifyIndexingLatency(BigDecimal.valueOf(((Long) fileSizeObj).doubleValue()/1024/1024)); // 转换为MB
                } else if (fileSizeObj instanceof Integer) {
                    document.setDifyIndexingLatency(BigDecimal.valueOf(((Integer) fileSizeObj).doubleValue()/1024/1024)); // 转换为MB
                }
            }
            
            this.save(document);
            
            // 将文档ID关联到文件记录
            entity.setDocumentId(document.getId());
            documentFileMapper.updateById(entity);
            
            vo.setDocumentId(document.getId()); // 设置文档ID
        }

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
        executorService.submit(() -> {
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
        });
    }

    /**
     * 更新文档的 Dify 处理状态
     */
    private void updateDifyProcessingStatus(Long documentId, Integer summaryStatus, Integer vectorizationStatus) {
        Document updateDoc = new Document();
        updateDoc.setId(documentId);
        updateDoc.setSummaryStatus(summaryStatus);
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
    
    /**
     * 异步上传文件到 Dify 知识流水线
     */
    private void uploadFileToDifyPipelineAsync(Long fileId, Long documentId) {
        // 使用线程池异步处理，避免阻塞主流程
        executorService.submit(() -> {
            try {
                // 获取文件信息
                DocumentFile file = documentFileMapper.selectById(fileId);
                if (file == null) {
                    log.error("文件不存在，ID: {}", fileId);
                    return;
                }
                
                // 从文件存储服务获取文件内容
                java.io.InputStream inputStream = fileStorageService.download(file.getFilePath());
                
                // 将输入流转换为字节数组
                byte[] fileBytes;
                try (inputStream; java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream()) {
                    int nRead;
                    byte[] data = new byte[1024];
                    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    fileBytes = buffer.toByteArray();
                }
                
                // 创建 MultipartFile 对象
                org.apache.commons.fileupload.FileItem fileItem = new org.apache.commons.fileupload.disk.DiskFileItem(
                        "file", "application/octet-stream", false, file.getFileName(), fileBytes.length, null);
                java.io.OutputStream os = fileItem.getOutputStream();
                os.write(fileBytes);
                os.close();
                
                org.springframework.web.multipart.commons.CommonsMultipartFile multipartFile = 
                    new org.springframework.web.multipart.commons.CommonsMultipartFile(fileItem);
                
                // 上传文件到 Dify 知识流水线
                java.util.Map<String, Object> result = difyAIService.uploadPipelineFile(multipartFile);
                
                if (!result.containsKey("error")) {
                    // 更新文档的 Dify 流水线相关字段
                    Document updateDoc = new Document();
                    updateDoc.setId(documentId);
                    
                    // 根据 Dify API 响应格式调整字段映射
                    if (result.containsKey("id")) {
                        updateDoc.setDifyFileId((String) result.get("id"));
                    } else if (result.containsKey("document_id")) {
                        updateDoc.setDifyFileId((String) result.get("document_id"));
                    }
                    
                    if (result.containsKey("url")) {
                        updateDoc.setDifyFileUrl((String) result.get("url"));
                    }
                    
                    // 设置初始解析状态为待解析
                    updateDoc.setDifyPipelineStatus(0); // 0-待解析
                    
                    if (result.containsKey("original_filename")) {
                        updateDoc.setName((String) result.get("original_filename"));
                    }
                    
                    if (result.containsKey("file_size")) {
                        Object fileSizeObj = result.get("file_size");
                        if (fileSizeObj instanceof Long) {
                            updateDoc.setDifyIndexingLatency(new java.math.BigDecimal(((Long) fileSizeObj).doubleValue()/1024/1024)); // 转换为MB
                        } else if (fileSizeObj instanceof Integer) {
                            updateDoc.setDifyIndexingLatency(new java.math.BigDecimal(((Integer) fileSizeObj).doubleValue()/1024/1024)); // 转换为MB
                        }
                    }
                    
                    this.updateById(updateDoc);
                    
                    log.info("文件上传到 Dify 知识流水线成功，文档ID: {}, 文件名: {}", documentId, file.getFileName());
                } else {
                    log.error("文件上传到 Dify 知识流水线失败，文档ID: {}，错误信息: {}", documentId, result.get("error"));
                }
            } catch (Exception e) {
                log.error("异步上传文件到 Dify 知识流水线时发生异常，文件ID: {}", fileId, e);
            }
        });
    }
    
    @Override
    public void runDifyPipeline(Long id) {
        Document document = this.getById(id);
        if (document == null) {
            throw new com.permission.exception.BusinessException("文档不存在");
        }

        // 检查文档是否有 Dify 文件 ID
        if (document.getDifyFileId() == null || document.getDifyFileId().isEmpty()) {
            throw new com.permission.exception.BusinessException("文档尚未上传到 Dify 知识流水线，无法运行解析");
        }

        // 异步运行 Dify 知识流水线
        runDifyPipelineAsync(id);
    }
    
    /**
     * 异步运行 Dify 知识流水线
     */
    private void runDifyPipelineAsync(Long documentId) {
        // 使用线程池异步处理，避免阻塞主流程
        executorService.submit(() -> {
            try {
                // 更新文档解析状态为解析中
                updateDifyPipelineStatus(documentId, 1, "解析中"); // 1-解析中
                
                Document document = this.getById(documentId);
                if (document == null) {
                    log.error("文档不存在，ID: {}", documentId);
                    updateDifyPipelineStatus(documentId, 3, "文档不存在"); // 3-解析失败
                    return;
                }

                // 调用 Dify 服务运行知识流水线
                java.util.Map<String, Object> result = difyAIService.runPipeline(
                    datasetId, document.getDifyFileId());

                if (!result.containsKey("error")) {
                    // 更新文档的 Dify 流水线解析结果
                    updateDifyPipelineStatus(documentId, 2, "解析成功"); // 2-解析成功
                    log.info("Dify 知识流水线运行成功，文档ID: {}", documentId);
                } else {
                    String errorMsg = (String) result.get("error");
                    updateDifyPipelineStatus(documentId, 3, "解析失败: " + errorMsg); // 3-解析失败
                    log.error("Dify 知识流水线运行失败，文档ID: {}，错误信息: {}", documentId, errorMsg);
                }
            } catch (Exception e) {
                log.error("异步运行 Dify 知识流水线时发生异常，文档ID: {}", documentId, e);
                updateDifyPipelineStatus(documentId, 3, "解析异常: " + e.getMessage()); // 3-解析失败
            }
        });
    }
    
    /**
     * 更新文档的 Dify 流水线解析状态
     */
    private void updateDifyPipelineStatus(Long documentId, Integer status, String result) {
        Document updateDoc = new Document();
        updateDoc.setId(documentId);
        updateDoc.setDifyPipelineStatus(status);
        updateDoc.setDifyPipelineResult(result);
        this.updateById(updateDoc);
    }
    
    /**
     * 销毁方法，关闭线程池
     */
    @Override
    public void destroy() throws Exception {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
