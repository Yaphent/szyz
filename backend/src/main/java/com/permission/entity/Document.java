package com.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 法规文档主实体
 */
/**
 * 
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
     * Dify 知识流水线中的文件 ID
     */
    @TableField(value = "dify_file_id")
    private String difyFileId;

    /**
     * Dify 处理状态 (pending-待处理, processing-处理中, completed-完成, failed-失败)
     */
    @TableField(value = "dify_processing_status")
    private String difyProcessingStatus;

    /**
     * Dify 中文件的访问 URL
     */
    @TableField(value = "dify_file_url")
    private String difyFileUrl;

    /**
     * Dify 文件上传时间
     */
    @TableField(value = "dify_upload_time")
    private LocalDateTime difyUploadTime;

    /**
     * Dify 流水线处理结果
     */
    @TableField(value = "dify_pipeline_result")
    private String difyPipelineResult;

    /**
     * Dify 分段数量
     */
    @TableField(value = "dify_segment_count")
    private Integer difySegmentCount;

    /**
     * Dify 索引延迟(秒)
     */
    @TableField(value = "dify_indexing_latency")
    private BigDecimal difyIndexingLatency;

    /**
     * Dify 解析后的内容 URL
     */
    @TableField(value = "dify_parsed_content_url")
    private String difyParsedContentUrl;
    
    /**
     * Dify 流水线解析状态 (0-待解析, 1-解析中, 2-解析成功, 3-解析失败)
     */
    @TableField(value = "dify_pipeline_status")
    private Integer difyPipelineStatus;

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
