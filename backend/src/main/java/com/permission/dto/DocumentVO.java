package com.permission.dto;

import com.permission.entity.DocumentFile;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档详情视图对象
 */
@Data
public class DocumentVO {

    private Long id;

    /** 文档名称 */
    private String name;

    /** 文号前缀 */
    private String policyPrefix;

    /** 文号年份 */
    private String policyYear;

    /** 文号 */
    private String policyNo;

    /** 业务类型 */
    private String businessType;

    /** 关键词 */
    private String keywords;

    /** 发文机关 */
    private String issuingAuthority;

    /** 发文日期 */
    private LocalDate issuingDate;

    /** 实施日期 */
    private LocalDate effectiveDate;

    /** 失效日期 */
    private LocalDate expiryDate;

    /** 法规等级 */
    private String lawLevel;

    /** 时效性 */
    private String timeliness;

    /** 摘要 */
    private String summary;

    /** 管控方式 */
    private String controlMode;

    /** 主文件ID */
    private Long mainFileId;

    /** 状态(0停用 1启用) */
    private Integer status;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    private String createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    private String updateTime;

    /** 主文件信息 */
    private DocumentFile mainFile;



    /** 标签列表 */
    private List<String> tags;

    /** Dify 知识库中的文档 ID */
    private String difyDocumentId;

    /** 摘要状态 (0-待处理, 1-处理中, 2-已完成, 3-失败) */
    private Integer summaryStatus;



    /** Dify 知识流水线中的文件 ID */
    private String difyFileId;

    /** Dify 处理状态 (pending-待处理, processing-处理中, completed-完成, failed-失败) */
    private String difyProcessingStatus;

    /** Dify 中文件的访问 URL */
    private String difyFileUrl;

    /** Dify 文件上传时间 */
    private LocalDateTime difyUploadTime;

    /** Dify 分段数量 */
    private Integer difySegmentCount;

    /** Dify 索引延迟(秒) */
    private BigDecimal difyIndexingLatency;

    /** Dify 解析后的内容 URL */
    private String difyParsedContentUrl;
    
    /** Dify 流水线解析状态 (0-待解析, 1-解析中, 2-解析成功, 3-解析失败) */
    private Integer difyPipelineStatus;
    
    /** Dify 流水线解析结果 */
    private String difyPipelineResult;
}