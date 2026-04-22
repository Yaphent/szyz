package com.permission.service;

import com.permission.dto.DifyDocumentProcessResult;

/**
 * Dify AI 服务接口
 * 
 * <p>负责与 Dify 服务进行交互，实现文档摘要抽取和向量化存储功能。</p>
 */
public interface DifyAIService {

    /**
     * 上传文档到 Dify 并进行处理
     * 
     * @param fileName 文件名
     * @param content 文件内容
     * @return 处理结果
     */
    DifyDocumentProcessResult processDocument(String fileName, String content);

    /**
     * 从 Dify 知识库中删除文档
     * 
     * @param documentId 文档在 Dify 知识库中的 ID
     */
    void deleteDocumentFromKnowledgeBase(String documentId);

    /**
     * 更新 Dify 知识库中的文档
     * 
     * @param documentId 文档在 Dify 知识库中的 ID
     * @param fileName 文件名
     * @param content 文件内容
     */
    void updateDocumentInKnowledgeBase(String documentId, String fileName, String content);

    /**
     * 停用 Dify 知识库中的文档
     * 
     * @param documentId 文档在 Dify 知识库中的 ID
     */
    void disableDocumentInKnowledgeBase(String documentId);

    /**
     * 启用 Dify 知识库中的文档
     * 
     * @param documentId 文档在 Dify 知识库中的 ID
     */
    void enableDocumentInKnowledgeBase(String documentId);
}