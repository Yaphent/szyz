package com.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.permission.entity.RoleMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色菜单Mapper
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {
    
    /**
     * 删除角色菜单关联
     */
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 批量插入角色菜单关联
     */
    @Insert("<script>" +
            "INSERT INTO sys_role_menu (role_id, menu_id) VALUES " +
            "<foreach collection='menuIds' item='menuId' separator=','>" +
            "(#{roleId}, #{menuId})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("roleId") Long roleId, @Param("menuIds") java.util.List<Long> menuIds);
}
