package com.permission.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 文档分页查询参数
 */
@Data
public class DocumentQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文档名称(模糊) */
    private String name;

    /** 政策文号(模糊) */
    private String policyNo;

    /** 业务分类 */
    private String businessType;

    /** 状态 */
    private Integer status;

    /** 发文日期起 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 发文日期止 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;
}
