package com.permission.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 文档-标签关联实体
 */
@Data
@TableName("t_document_tag")
public class DocumentTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文档ID */
    private Long documentId;

    /** 标签名 */
    private String tagName;
}
