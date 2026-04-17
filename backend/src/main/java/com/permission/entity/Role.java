package com.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色实体
 */
@Data
@TableName("sys_role")
public class Role implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long roleId;
    
    private String roleName;
    
    private String roleCode;
    
    private Integer status;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private List<Menu> menus;
    
    @TableField(exist = false)
    private List<Long> menuIds;
}
