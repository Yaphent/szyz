package com.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 法规文档主实体
 */
@Data
@TableName("t_document")
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文档名称 */
    private String name;

    /** 文号前缀,如"中发" */
    private String policyPrefix;

    /** 文号年份,如"〔2017〕" */
    private String policyYear;

    /** 文号编号,如"33" */
    private String policyNo;

    /** 业务分类(字典code) */
    private String businessType;

    /** 关键字，多个用英文逗号分隔 */
    private String keywords;

    /** 发文机关 */
    private String issuingAuthority;

    /** 发文日期 */
    private LocalDate issuingDate;

    /** 实施日期 */
    private LocalDate effectiveDate;

    /** 失效日期 */
    private LocalDate expiryDate;

    /** 法规层次 */
    private String lawLevel;

    /** 时效性:valid/invalid/pending */
    private String timeliness;

    /** 摘要 */
    private String summary;

    /** 管控模式: UNIFIED-统建 SELF-自建 CUSTOM-自定义范围统建 */
    private String controlMode;

    /** 主文件ID */
    private Long mainFileId;

    /** 状态:1启用 0停用 */
    private Integer status;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    /**
     * Dify 知识库中的文档 ID
     */
    @TableField(value = "dify_document_id")
    private String difyDocumentId;

    /**
     * 摘要状态 (0-待处理, 1-处理中, 2-已完成, 3-失败)
     */
    @TableField(value = "summary_status")
    private Integer summaryStatus;

    /**
     * 向量化状态 (0-待处理, 1-处理中, 2-已完成, 3-失败)
     */
    @TableField(value = "vectorization_status")
    private Integer vectorizationStatus;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新人 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
