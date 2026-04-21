package com.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单实体
 */
@Data
@TableName("sys_menu")
public class Menu implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long menuId;
    
    private Long parentId;
    
    /**
     * 菜单类型: M-目录, C-菜单, F-按钮, L-外链
     */
    private String menuType;
    
    private String name;
    
    /**
     * 路由路径或外链URL
     */
    private String path;
    
    /**
     * 组件路径
     */
    private String component;
    
    /**
     * 权限标识
     */
    private String perms;
    
    /**
     * 后端接口路径，用于权限拦截
     */
    private String url;
    
    private String icon;
    
    private Integer sort;
    
    /**
     * 外链打开方式: 0-新标签页打开, 1-嵌入式打开(iframe)
     */
    private Integer isFrame;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 子菜单列表（非数据库字段）
    @TableField(exist = false)
    private List<Menu> children;
}
