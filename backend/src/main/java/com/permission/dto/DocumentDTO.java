package com.permission.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 文档新增/修改请求 DTO
 */
@Data
public class DocumentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID（更新时必填） */
    private Long id;

    /** 文档名称 */
    @NotBlank(message = "文档名称不能为空")
    @Size(max = 200, message = "文档名称长度不能超过200字符")
    private String name;

    /** 文号前缀 */
    private String policyPrefix;

    /** 文号年份 */
    private String policyYear;

    /** 文号编号 */
    private String policyNo;

    /** 业务分类 */
    @NotBlank(message = "业务分类不能为空")
    private String businessType;

    /** 关键字 */
    private String keywords;

    /** 发文机关 */
    @NotBlank(message = "发文机关不能为空")
    private String issuingAuthority;

    /** 发文日期 */
    @NotNull(message = "发文日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issuingDate;

    /** 实施日期 */
    @NotNull(message = "实施日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    /** 失效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    /** 法规层次 */
    private String lawLevel;

    /** 时效性 */
    private String timeliness;

    /** 摘要 */
    private String summary;

    /** 管控模式 */
    @NotBlank(message = "管控模式不能为空")
    private String controlMode;

    /** 主文件ID */
    private Long mainFileId;



    /** 标签列表 */
    private List<String> tags;
}
