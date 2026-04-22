package com.permission.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 批量状态修改请求
 */
@Data
public class DocumentStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID列表 */
    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;

    /** 状态:1启用 0停用 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
