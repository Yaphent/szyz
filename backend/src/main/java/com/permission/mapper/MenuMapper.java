package com.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.permission.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 菜单Mapper
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {
    
    /**
     * 根据URL查询菜单
     */
    @Select("SELECT * FROM sys_menu WHERE url = #{url} AND status = 1")
    Menu selectByUrl(@Param("url") String url);
    
    /**
     * 检查菜单是否有子菜单
     */
    @Select("SELECT COUNT(*) > 0 FROM sys_menu WHERE parent_id = #{menuId}")
    boolean hasChildren(@Param("menuId") Long menuId);
    
    /**
     * 检查菜单是否有角色关联
     */
    @Select("SELECT COUNT(*) > 0 FROM sys_role_menu WHERE menu_id = #{menuId}")
    boolean hasRoles(@Param("menuId") Long menuId);
    
    /**
     * 获取用户动态路由（根据角色过滤）
     */
    @Select("SELECT DISTINCT m.* FROM sys_user_role ur " +
            "JOIN sys_role_menu rm ON ur.role_id = rm.role_id " +
            "JOIN sys_menu m ON rm.menu_id = m.menu_id " +
            "WHERE ur.user_id = #{userId} AND m.status = 1 AND m.menu_type IN ('M', 'C') " +
            "ORDER BY m.sort ASC")
    java.util.List<Menu> selectRoutesByUserId(@Param("userId") Long userId);
    
    /**
     * 获取所有菜单用于超级管理员
     */
    @Select("SELECT * FROM sys_menu WHERE status = 1 AND menu_type IN ('M', 'C') ORDER BY sort ASC")
    java.util.List<Menu> selectAllRoutes();
}
