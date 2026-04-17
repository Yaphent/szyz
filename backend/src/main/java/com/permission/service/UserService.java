package com.permission.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.permission.common.PageResult;
import com.permission.entity.User;
import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 根据用户名查询
     */
    User getByUsername(String username);
    
    /**
     * 分页查询
     */
    PageResult<User> getPage(int page, int pageSize, String username, String mobile, Integer status, Long deptId, List<Long> deptIds);
    
    /**
     * 获取用户详情（含角色和管辖单位）
     */
    User getUserDetails(Long userId);
    
    /**
     * 修改密码
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 重置密码
     */
    boolean resetPassword(Long userId, String newPassword);
    
    /**
     * 更新头像
     */
    boolean updateAvatar(Long userId, String avatarUrl);
    
    /**
     * 分配角色
     */
    boolean assignRoles(Long userId, List<Long> roleIds);
    
    /**
     * 分配管辖单位
     */
    boolean assignDepts(Long userId, List<Long> deptIds);
    
    /**
     * 获取用户权限标识列表
     */
    List<String> getUserPerms(Long userId);
    
    /**
     * 获取用户菜单列表
     */
    List<com.permission.entity.Menu> getUserMenus(Long userId);
    
    /**
     * 检查是否为超级管理员
     */
    boolean isSuperAdmin(Long userId);
    
    /**
     * 获取用户统计
     */
    long getStatistics();
}
