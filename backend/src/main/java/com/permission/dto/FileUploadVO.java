package com.permission.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传响应
 */
@Data
public class FileUploadVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文件ID(t_document_file 主键) */
    private Long fileId;

    /** 原始文件名 */
    private String fileName;

    /** 文件URL */
    private String fileUrl;

    /** 文件大小 */
    private Long fileSize;

    /** 文件类型 */
    private String fileType;

    /** 存储类型 */
    private String storageType;

    /** 文档ID(t_document 主键) */
    private Long documentId;
}
