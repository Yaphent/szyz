package com.permission.controller;

import com.permission.common.PageResult;
import com.permission.common.R;
import com.permission.dto.DocumentDTO;
import com.permission.dto.DocumentQueryDTO;
import com.permission.dto.DocumentStatusDTO;
import com.permission.dto.DocumentVO;
import com.permission.dto.DifyDocumentProcessResult;
import com.permission.dto.FileUploadVO;
import com.permission.entity.DictTag;
import com.permission.entity.DocumentFile;
import com.permission.service.DictTagService;
import com.permission.service.DocumentService;
import com.permission.service.DifyAIService;
import com.permission.service.FileParseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 法规文档管理 Controller
 *
 * <p>对应前端 /document 模块，提供文档的 CRUD、批量操作、
 * 文件上传/下载/解析、标签字典等接口。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/document")
public class DocumentController {

    @Autowired
  private DocumentService documentService;

  @Autowired
  private FileParseService fileParseService;

  @Autowired
  private DictTagService dictTagService;

  @Autowired
  private DifyAIService difyAIService;

    // ================================================================
    // 查询
    // ================================================================

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public R<PageResult<DocumentVO>> page(DocumentQueryDTO query) {
        return R.success(documentService.pageQuery(query));
    }

    /**
     * 查询详情
     */
    @GetMapping("/{id}")
    public R<DocumentVO> detail(@PathVariable Long id) {
        return R.success(documentService.detail(id));
    }

    // ================================================================
    // 新增 / 更新
    // ================================================================

    /**
     * 新增文档
     */
    @PostMapping
    public R<Long> create(@Valid @RequestBody DocumentDTO dto) {
        Long id = documentService.create(dto);
        return R.success(id, "新增成功");
    }

    /**
     * 更新文档
     */
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody DocumentDTO dto) {
        documentService.update(id, dto);
        return R.success(null, "更新成功");
    }

    // ================================================================
    // 删除 / 状态
    // ================================================================

    /**
     * 单条删除（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        documentService.remove(id);
        return R.success(null, "删除成功");
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch")
    public R<Void> removeBatch(@RequestBody List<Long> ids) {
        documentService.removeBatch(ids);
        return R.success(null, "批量删除成功");
    }

    /**
     * 批量启用 / 停用
     */
    @PutMapping("/status")
    public R<Void> updateStatus(@Valid @RequestBody DocumentStatusDTO dto) {
        documentService.updateStatus(dto.getIds(), dto.getStatus());
        return R.success(null, "状态修改成功");
    }

    // ================================================================
    // 文件
    // ================================================================

    /**
     * 上传文件
     *
     * @param file     文件
     * @param category 1=主文件, 2=附件（默认 2）
     */
    @PostMapping("/upload")
    public R<FileUploadVO> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "category", defaultValue = "2") Integer category) {
        return R.success(documentService.uploadFile(file, category), "上传成功");
    }

    /**
   * 解析文件内容（同步上传并解析文本，不入库）
   */
  @PostMapping("/parse")
  public R<Map<String, Object>> parse(@RequestParam("file") MultipartFile file) {
      String content = fileParseService.parse(file);
      Map<String, Object> data = new HashMap<>(4);
      data.put("content", content);
      data.put("length", content == null ? 0 : content.length());
      return R.success(data, "解析成功");
  }

  /**
   * 智能解析文件内容（使用 Dify 服务进行高级解析）
   */
  @PostMapping("/smart-parse")
  public R<Map<String, Object>> smartParse(@RequestParam("file") MultipartFile file) {
      try {
          // 读取文件内容
          String content = new String(file.getBytes(), StandardCharsets.UTF_8);
          // 调用 Dify 服务进行智能解析
          DifyDocumentProcessResult result = difyAIService.processDocument(file.getOriginalFilename(), content);
          
          Map<String, Object> data = new HashMap<>(4);
          if (result.getSuccess()) {
              data.put("summary", result.getSummary());
              data.put("documentId", result.getDocumentId());
              data.put("status", result.getStatus());
              return R.success(data, "智能解析成功");
          } else {
              return R.error(500, result.getMessage());
          }
      } catch (Exception e) {
          log.error("智能解析文件失败", e);
          return R.error(500, "智能解析失败: " + e.getMessage());
      }
  }

    /**
     * 下载文件
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long fileId) {
        DocumentFile[] holder = new DocumentFile[1];
        InputStream in = documentService.downloadFile(fileId, holder);
        DocumentFile meta = holder[0];
        String fileName = meta != null && meta.getFileName() != null ? meta.getFileName() : ("file_" + fileId);

        String encoded;
        try {
            encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
        } catch (Exception e) {
            encoded = fileName;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }

    /**
     * 删除文件（同时删除服务器文件）
     */
    @DeleteMapping("/file/{fileId}")
    public R<Void> removeFile(@PathVariable Long fileId) {
        documentService.removeFile(fileId);
        return R.success(null, "删除成功");
    }

    /**
     * 获取文档的 Dify 处理状态
     */
    @GetMapping("/dify-status/{id}")
    public R<Map<String, Object>> getDifyStatus(@PathVariable Long id) {
        DocumentVO document = documentService.detail(id);
        if (document == null) {
            return R.error(404, "文档不存在");
        }

        Map<String, Object> statusInfo = new HashMap<>();
        statusInfo.put("summaryStatus", document.getSummaryStatus());
        statusInfo.put("vectorizationStatus", document.getVectorizationStatus());
        statusInfo.put("difyDocumentId", document.getDifyDocumentId());
        
        return R.success(statusInfo, "获取成功");
    }

    /**
     * 对文档进行向量化处理
     */
    @PostMapping("/vectorize/{id}")
    public R<Void> vectorizeDocument(@PathVariable Long id) {
        try {
            documentService.vectorizeDocument(id);
            return R.success(null, "文档向量化处理已启动");
        } catch (Exception e) {
            log.error("文档向量化处理失败，文档ID: {}", id, e);
            return R.error(500, "文档向量化处理失败: " + e.getMessage());
        }
    }

    /**
     * 删除文档的向量化数据
     */
    @DeleteMapping("/vectorize/{id}")
    public R<Void> deleteVectorizedDocument(@PathVariable Long id) {
        try {
            documentService.deleteVectorizedDocument(id);
            return R.success(null, "文档向量化数据已删除");
        } catch (Exception e) {
            log.error("删除文档向量化数据失败，文档ID: {}", id, e);
            return R.error(500, "删除文档向量化数据失败: " + e.getMessage());
        }
    }

    // ================================================================
    // 标签字典
    // ================================================================

    /**
     * 获取标签字典（系统预置 + 自定义）
     */
    @GetMapping("/dict/tags")
    public R<List<DictTag>> listTags() {
        return R.success(dictTagService.listAll());
    }

    /**
     * 新增自定义标签
     */
    @PostMapping("/dict/tags")
    public R<DictTag> addTag(@RequestBody Map<String, String> body) {
        String tagName = body == null ? null : body.get("tagName");
        return R.success(dictTagService.addCustom(tagName), "新增成功");
    }
}
