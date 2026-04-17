package com.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.permission.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);
    
    /**
     * 分页查询用户（带数据权限）
     */
    IPage<User> selectUserPage(Page<User> page, 
                               @Param("username") String username,
                               @Param("mobile") String mobile,
                               @Param("status") Integer status,
                               @Param("deptId") Long deptId,
                               @Param("deptIds") String deptIds);
    
    /**
     * 获取用户管辖单位ID列表
     */
    @Select("SELECT dept_id FROM sys_user_dept WHERE user_id = #{userId}")
    java.util.List<Long> selectUserDeptIds(@Param("userId") Long userId);
    
    /**
     * 获取用户角色ID列表
     */
    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    java.util.List<Long> selectUserRoleIds(@Param("userId") Long userId);
    
    /**
     * 获取用户权限标识列表
     */
    @Select("SELECT DISTINCT m.perms FROM sys_user_role ur " +
            "JOIN sys_role_menu rm ON ur.role_id = rm.role_id " +
            "JOIN sys_menu m ON rm.menu_id = m.menu_id " +
            "WHERE ur.user_id = #{userId} AND m.perms IS NOT NULL AND m.perms != ''")
    java.util.List<String> selectUserPerms(@Param("userId") Long userId);
    
    /**
     * 获取用户菜单列表
     */
    @Select("SELECT m.* FROM sys_user_role ur " +
            "JOIN sys_role_menu rm ON ur.role_id = rm.role_id " +
            "JOIN sys_menu m ON rm.menu_id = m.menu_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 AND m.menu_type IN ('M', 'C') " +
            "ORDER BY m.sort ASC")
    java.util.List<com.permission.entity.Menu> selectUserMenus(@Param("userId") Long userId);

    /**
     * 获取用户菜单列表
     */
    @Select("SELECT m.* FROM sys_menu m where m.status = 1 AND m.menu_type IN ('M', 'C') " +
            "            ORDER BY m.sort ASC")
    java.util.List<com.permission.entity.Menu> selectAllMenus();
    
    /**
     * 检查是否为超级管理员
     */
    @Select("SELECT COUNT(*) > 0 FROM sys_user_role ur " +
            "JOIN sys_role r ON ur.role_id = r.role_id " +
            "WHERE ur.user_id = #{userId} AND r.role_code = 'super_admin'")
    boolean isSuperAdmin(@Param("userId") Long userId);
}
