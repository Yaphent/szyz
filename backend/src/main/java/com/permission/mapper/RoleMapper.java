package com.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.permission.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 角色Mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    
    /**
     * 根据角色编码查询
     */
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode}")
    Role selectByRoleCode(@Param("roleCode") String roleCode);
    
    /**
     * 检查角色是否有关联用户
     */
    @Select("SELECT COUNT(*) > 0 FROM sys_user_role WHERE role_id = #{roleId}")
    boolean hasUsers(@Param("roleId") Long roleId);
    
    /**
     * 获取角色菜单列表
     */
    @Select("SELECT m.* FROM sys_role_menu rm " +
            "JOIN sys_menu m ON rm.menu_id = m.menu_id " +
            "WHERE rm.role_id = #{roleId} ORDER BY m.sort ASC")
    java.util.List<com.permission.entity.Menu> selectRoleMenus(@Param("roleId") Long roleId);
}
