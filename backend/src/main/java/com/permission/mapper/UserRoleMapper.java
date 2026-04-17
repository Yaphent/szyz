package com.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.permission.entity.UserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户角色Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    
    /**
     * 删除用户角色关联
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 批量插入用户角色关联
     */
    @Insert("<script>" +
            "INSERT INTO sys_user_role (user_id, role_id) VALUES " +
            "<foreach collection='roleIds' item='roleId' separator=','>" +
            "(#{userId}, #{roleId})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("userId") Long userId, @Param("roleIds") java.util.List<Long> roleIds);
}
