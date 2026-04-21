package com.permission.controller;

import com.permission.common.R;
import com.permission.config.JwtUtils;
import com.permission.entity.Menu;
import com.permission.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 菜单控制器
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {
    
    @Autowired
    private MenuService menuService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    /**
     * 获取菜单树
     */
    @GetMapping("/tree")
    public R<List<Menu>> getTree() {
        return R.success(menuService.getTree());
    }
    
    /**
     * 获取用户动态路由
     */
    @GetMapping("/route")
    public R<List<Menu>> getRoute(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtils.getUserId(token);
        boolean isSuperAdmin = jwtUtils.isSuperAdmin(token);
        return R.success(menuService.getRoutes(userId, isSuperAdmin));
    }
    
    /**
     * 获取菜单详情
     */
    @GetMapping("/{menuId}")
    public R<Menu> getById(@PathVariable Long menuId) {
        Menu menu = menuService.getById(menuId);
        if (menu == null) {
            return R.error(404, "菜单不存在");
        }
        return R.success(menu);
    }
    
    /**
     * 新增菜单
     */
    @PostMapping
    public R<Long> create(@RequestBody Menu menu) {
        if (menu.getMenuType() == null || menu.getName() == null) {
            return R.error(400, "菜单类型和名称不能为空");
        }
        if ("F".equals(menu.getMenuType()) && (menu.getPerms() == null || menu.getPerms().isEmpty())) {
            return R.error(400, "按钮权限必须有权限标识");
        }
        menuService.save(menu);
        return R.success(menu.getMenuId(), "创建成功");
    }
    
    /**
     * 修改菜单
     */
    @PutMapping
    public R update(@RequestBody Menu menu) {
        if (menu.getMenuId() == null) {
            return R.error(400, "菜单ID不能为空");
        }
        if (menu.getMenuType() == null || menu.getName() == null) {
            return R.error(400, "菜单类型和名称不能为空");
        }
        if ("F".equals(menu.getMenuType()) && (menu.getPerms() == null || menu.getPerms().isEmpty())) {
            return R.error(400, "按钮权限必须有权限标识");
        }
        menuService.updateById(menu);
        return R.success(null, "修改成功");
    }
    
    /**
     * 删除菜单
     */
    @DeleteMapping("/{menuId}")
    public R delete(@PathVariable Long menuId) {
        if (menuService.hasChildren(menuId)) {
            return R.error(400, "存在子菜单，不允许删除");
        }
        if (menuService.hasRoles(menuId)) {
            return R.error(400, "菜单已关联角色，不允许删除");
        }
        menuService.removeById(menuId);
        return R.success(null, "删除成功");
    }
}
