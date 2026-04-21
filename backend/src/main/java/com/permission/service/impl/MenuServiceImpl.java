package com.permission.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.permission.entity.Menu;
import com.permission.mapper.MenuMapper;
import com.permission.service.MenuService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {
    
    @Override
    public List<Menu> getTree() {
        List<Menu> allMenus = this.list();
        return buildTree(allMenus, 0L);
    }
    
    /**
     * 构建树形结构
     */
    private List<Menu> buildTree(List<Menu> allMenus, Long parentId) {
        return allMenus.stream()
                .filter(menu -> menu.getParentId().equals(parentId))
                .peek(menu -> {
                    List<Menu> children = buildTree(allMenus, menu.getMenuId());
                    if (!children.isEmpty()) {
                        menu.setChildren(children);
                    }
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Menu> getRoutes(Long userId, boolean isSuperAdmin) {
        List<Menu> menus;
        if (isSuperAdmin) {
            // 超级管理员获取所有菜单
            menus = baseMapper.selectAllRoutes();
        } else {
            // 普通用户根据角色获取菜单
            menus = baseMapper.selectRoutesByUserId(userId);
        }
        return buildTree(menus, 0L);
    }
    
    @Override
    public Menu getByUrl(String url) {
        return baseMapper.selectByUrl(url);
    }
    
    @Override
    public boolean hasChildren(Long menuId) {
        return baseMapper.hasChildren(menuId);
    }
    
    @Override
    public boolean hasRoles(Long menuId) {
        return baseMapper.hasRoles(menuId);
    }
    
    @Override
    public boolean save(Menu menu) {
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        if (menu.getSort() == null) {
            menu.setSort(0);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }
        if (menu.getIsFrame() == null) {
            menu.setIsFrame(0);
        }
        return super.save(menu);
    }
}
