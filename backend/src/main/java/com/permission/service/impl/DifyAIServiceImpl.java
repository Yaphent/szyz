package com.permission.service.impl;

import com.permission.dto.DifyDocumentProcessResult;
import com.permission.service.DifyAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Dify AI 服务实现
 * 
 * <p>负责与 Dify 服务进行交互，实现文档摘要抽取和向量化存储功能。</p>
 */
@Slf4j
@Service
public class DifyAIServiceImpl implements DifyAIService {

    @Value("${dify.api.url:http://localhost:5001/v1}")
    private String difyApiUrl;

    @Value("${dify.api.key:}")
    private String apiKey;

    @Value("${dify.dataset.id:}")
    private String datasetId;

    private final RestTemplate restTemplate;

    public DifyAIServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public Map<String, Object> runPipeline(String datasetId, String documentId) {
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("dataset_id", datasetId);
            requestBody.put("document_id", documentId);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 发送请求到 Dify API
            String url = difyApiUrl + "/datasets/" + datasetId + "/pipeline/run";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null) {
                    log.info("成功运行 Dify 知识流水线，数据集ID: {}, 文档ID: {}", datasetId, documentId);
                    return responseBody;
                } else {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("error", "响应体为空");
                    return errorResult;
                }
            } else {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", "运行流水线失败，响应状态码: " + response.getStatusCode());
                errorResult.put("message", response.getBody());
                return errorResult;
            }
        } catch (Exception e) {
            log.error("运行 Dify 知识流水线失败，数据集ID: {}, 文档ID: {}", datasetId, documentId, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "运行流水线失败: " + e.getMessage());
            return errorResult;
        }
    }

    @Override
    public DifyDocumentProcessResult processDocument(String fileName, String content) {
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("dataset_id", datasetId);
            requestBody.put("original_document_id", fileName); // 使用文件名作为原始文档ID
            
            Map<String, Object> doc_info = new HashMap<>();
            doc_info.put("name", fileName);
            doc_info.put("text", content);
            doc_info.put("process_rule_id", "default"); // 使用默认处理规则
            requestBody.put("doc_form", "text_model"); // 使用文本模型
            requestBody.put("doc_language", "zh"); // 中文
            requestBody.put("batch", "batch_1"); // 批次号
            requestBody.put("doc_type", "preprocessing"); // 预处理类型
            requestBody.put("doc_infos", new Object[]{doc_info});

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 发送请求到 Dify API
            String url = difyApiUrl + "/datasets/documents";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null) {
                    // 解析响应
                    String documentId = (String)responseBody.get("document_id");
                    String status = (String)responseBody.get("status");
                    
                    // 获取摘要（如果有的话）
                    String summary = extractSummary(content); // 临时实现，实际应从 Dify 获取
                    
                    return DifyDocumentProcessResult.success(documentId, summary, status);
                }
            }
            
            return DifyDocumentProcessResult.failure("处理失败，响应状态码: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("调用 Dify API 处理文档失败", e);
            return DifyDocumentProcessResult.failure("处理失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteDocumentFromKnowledgeBase(String documentId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            String url = difyApiUrl + "/datasets/" + datasetId + "/documents/" + documentId;
            ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, requestEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("删除 Dify 知识库中文档失败，文档ID: {}, 响应: {}", documentId, response.getBody());
            } else {
                log.info("成功删除 Dify 知识库中的文档，文档ID: {}", documentId);
            }
        } catch (Exception e) {
            log.error("删除 Dify 知识库中文档时发生异常，文档ID: {}", documentId, e);
        }
    }

    @Override
    public void updateDocumentInKnowledgeBase(String documentId, String fileName, String content) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // 构建更新请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", fileName);
            requestBody.put("text", content);
            requestBody.put("process_rule_id", "default");

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            String url = difyApiUrl + "/datasets/" + datasetId + "/documents/" + documentId;
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("成功更新 Dify 知识库中的文档，文档ID: {}", documentId);
            } else {
                log.error("更新 Dify 知识库中文档失败，文档ID: {}, 响应: {}", documentId, response.getBody());
            }
        } catch (Exception e) {
            log.error("更新 Dify 知识库中文档时发生异常，文档ID: {}", documentId, e);
        }
    }

    @Override
    public void disableDocumentInKnowledgeBase(String documentId) {
        // Dify API 中没有直接的禁用功能，我们通过删除文档来实现
        // 在实际应用中，可以根据 Dify 的具体 API 调整实现
        log.info("禁用 Dify 知识库中的文档，文档ID: {} (实际通过删除实现)", documentId);
        deleteDocumentFromKnowledgeBase(documentId);
    }

    @Override
    public void enableDocumentInKnowledgeBase(String documentId) {
        // Dify API 中没有直接的启用功能，我们通过重新上传文档来实现
        // 在实际应用中，可以根据 Dify 的具体 API 调整实现
        log.info("启用 Dify 知识库中的文档，文档ID: {} (需要重新上传实现)", documentId);
    }

    @Override
    public Map<String, Object> uploadPipelineFile(MultipartFile file) {
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 构建多部分请求体
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 发送请求到 Dify API
            String url = difyApiUrl + "/datasets/pipeline/file-upload";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null) {
                    log.info("成功上传文件到 Dify 知识流水线，文件名: {}", file.getOriginalFilename());
                    // 添加原始文件信息到响应中
                    responseBody.put("original_filename", file.getOriginalFilename());
                    responseBody.put("file_size", file.getSize());
                    return responseBody;
                } else {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("error", "响应体为空");
                    return errorResult;
                }
            } else {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", "上传失败，响应状态码: " + response.getStatusCode());
                errorResult.put("message", response.getBody());
                return errorResult;
            }
        } catch (Exception e) {
            log.error("上传文件到 Dify 知识流水线失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "上传失败: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 从内容中提取摘要（临时实现，实际应调用 Dify 摘要 API）
     */
    private String extractSummary(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        // 简单实现：取内容的前 200 个字符作为摘要
        return content.length() <= 200 ? content : content.substring(0, 200) + "...";
    }
}