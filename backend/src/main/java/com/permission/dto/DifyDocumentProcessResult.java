package com.permission.dto;

import lombok.Data;

/**
 * Dify 文档处理结果
 */
@Data
public class DifyDocumentProcessResult {

    /** 是否成功 */
    private Boolean success;

    /** 消息 */
    private String message;

    /** 文档在 Dify 知识库中的 ID */
    private String documentId;

    /** 摘要 */
    private String summary;

    /** 状态 */
    private String status;

    public static DifyDocumentProcessResult success(String documentId, String summary, String status) {
        DifyDocumentProcessResult result = new DifyDocumentProcessResult();
        result.setSuccess(true);
        result.setDocumentId(documentId);
        result.setSummary(summary);
        result.setStatus(status);
        result.setMessage("处理成功");
        return result;
    }

    public static DifyDocumentProcessResult failure(String message) {
        DifyDocumentProcessResult result = new DifyDocumentProcessResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
}