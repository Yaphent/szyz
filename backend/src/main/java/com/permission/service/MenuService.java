package com.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.permission.entity.Menu;
import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService extends IService<Menu> {
    
    /**
     * 获取菜单树
     */
    List<Menu> getTree();
    
    /**
     * 获取用户动态路由
     */
    List<Menu> getRoutes(Long userId);
    
    /**
     * 根据URL获取菜单
     */
    Menu getByUrl(String url);
    
    /**
     * 检查是否有子菜单
     */
    boolean hasChildren(Long menuId);
    
    /**
     * 检查是否有角色关联
     */
    boolean hasRoles(Long menuId);
}
