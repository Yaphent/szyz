package com.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体
 */
@Data
@TableName("sys_user")
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long userId;
    
    private String username;
    
    private String nickname;
    
    private String password;
    
    private String email;
    
    private String mobile;
    
    private Integer status;
    
    private Long deptId;
    
    private LocalDateTime lastLoginTime;
    
    private String avatar;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private String deptName;
    
    @TableField(exist = false)
    private List<Role> roles;
    
    @TableField(exist = false)
    private List<Dept> depts;
    
    @TableField(exist = false)
    private List<Long> roleIds;
    
    @TableField(exist = false)
    private List<Long> deptIds;
}
