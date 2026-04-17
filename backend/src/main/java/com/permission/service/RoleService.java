package com.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.permission.common.PageResult;
import com.permission.entity.Role;
import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<Role> {
    
    /**
     * 分页查询
     */
    PageResult<Role> getPage(int page, int pageSize, String roleName, String roleCode, Integer status);
    
    /**
     * 获取全部角色
     */
    List<Role> getAll();
    
    /**
     * 获取角色详情（含菜单）
     */
    Role getRoleDetails(Long roleId);
    
    /**
     * 根据角色编码查询
     */
    Role getByRoleCode(String roleCode);
    
    /**
     * 检查是否有用户关联
     */
    boolean hasUsers(Long roleId);
    
    /**
     * 分配菜单权限
     */
    boolean assignMenus(Long roleId, List<Long> menuIds);
    
    /**
     * 获取角色统计
     */
    long getStatistics();
}
