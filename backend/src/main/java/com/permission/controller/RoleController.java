package com.permission.controller;

import com.permission.common.R;
import com.permission.entity.Role;
import com.permission.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色控制器
 */
@RestController
@RequestMapping("/api/role")
public class RoleController {
    
    @Autowired
    private RoleService roleService;
    
    /**
     * 分页查询
     */
    @GetMapping("/page")
    public R getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String role_name,
            @RequestParam(required = false) String role_code,
            @RequestParam(required = false) Integer status) {
        
        return R.success(roleService.getPage(page, pageSize, role_name, role_code, status));
    }
    
    /**
     * 获取全部角色
     */
    @GetMapping("/all")
    public R<List<Role>> getAll() {
        return R.success(roleService.getAll());
    }
    
    /**
     * 获取角色详情
     */
    @GetMapping("/{roleId}")
    public R<Role> getById(@PathVariable Long roleId) {
        Role role = roleService.getRoleDetails(roleId);
        if (role == null) {
            return R.error(404, "角色不存在");
        }
        return R.success(role);
    }
    
    /**
     * 新增角色
     */
    @PostMapping
    public R<Long> create(@RequestBody Role role) {
        if (role.getRoleName() == null || role.getRoleCode() == null) {
            return R.error(400, "角色名称和编码不能为空");
        }
        
        Role existing = roleService.getByRoleCode(role.getRoleCode());
        if (existing != null) {
            return R.error(400, "角色编码已存在");
        }
        
        roleService.save(role);
        return R.success(role.getRoleId(), "创建成功");
    }
    
    /**
     * 修改角色
     */
    @PutMapping
    public R update(@RequestBody Role role) {
        if (role.getRoleId() == null) {
            return R.error(400, "角色ID不能为空");
        }
        if (role.getRoleName() == null || role.getRoleCode() == null) {
            return R.error(400, "角色名称和编码不能为空");
        }
        
        roleService.updateById(role);
        return R.success(null, "修改成功");
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{roleId}")
    public R delete(@PathVariable Long roleId) {
        if (roleId == 1) {
            return R.error(400, "不能删除超级管理员角色");
        }
        if (roleService.hasUsers(roleId)) {
            return R.error(400, "角色已关联用户，不允许删除");
        }
        roleService.removeById(roleId);
        return R.success(null, "删除成功");
    }
    
    /**
     * 分配菜单权限
     */
    @PostMapping("/assign-menus")
    public R assignMenus(@RequestBody Map<String, Object> params) {
        Long roleId = ((Number) params.get("role_id")).longValue();
        @SuppressWarnings("unchecked")
        List<Long> menuIds = ((List<Number>) params.get("menu_ids"))
                .stream().map(Number::longValue).collect(Collectors.toList());
        
        roleService.assignMenus(roleId, menuIds);
        return R.success(null, "菜单权限分配成功");
    }
}
