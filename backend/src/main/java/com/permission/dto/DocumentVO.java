package com.permission.dto;

import com.permission.entity.DocumentFile;
import lombok.Data;

import java.time.LocalDate;
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

    /** 附件列表 */
    private List<DocumentFile> attachments;

    /** 标签列表 */
    private List<String> tags;

    /** Dify 知识库中的文档 ID */
    private String difyDocumentId;

    /** 摘要状态 (0-待处理, 1-处理中, 2-已完成, 3-失败) */
    private Integer summaryStatus;

    /** 向量化状态 (0-待处理, 1-处理中, 2-已完成, 3-失败) */
    private Integer vectorizationStatus;
}