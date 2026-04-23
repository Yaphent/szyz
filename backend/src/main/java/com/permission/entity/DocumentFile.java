package com.permission.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文档文件实体
 */
@Data
@TableName("t_document_file")
public class DocumentFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文档ID(上传时可能为空，保存时回填) */
    private Long documentId;

    /** 原始文件名 */
    private String fileName;

    /** 文件存储路径或对象key */
    private String filePath;

    /** 可访问URL */
    private String fileUrl;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件扩展名 */
    private String fileType;

    /** 1主文件（当前系统只使用主文件类型） */
    private Integer fileCategory;

    /** 存储类型:local/minio */
    private String storageType;

    /** 上传时间 */
    private LocalDateTime uploadTime;
}
