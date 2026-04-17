package com.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 单位实体
 */
@Data
@TableName("sys_dept")
public class Dept implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long deptId;
    
    private Long parentId;
    
    private String deptName;
    
    private String deptCode;
    
    private String leader;
    
    private String phone;
    
    private Integer sort;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 子部门列表（非数据库字段）
    @TableField(exist = false)
    private java.util.List<Dept> children;
}
