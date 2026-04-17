package com.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.permission.entity.UserDept;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户管辖单位Mapper
 */
@Mapper
public interface UserDeptMapper extends BaseMapper<UserDept> {
    
    /**
     * 删除用户管辖单位关联
     */
    @Delete("DELETE FROM sys_user_dept WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 批量插入用户管辖单位关联
     */
    @Insert("<script>" +
            "INSERT INTO sys_user_dept (user_id, dept_id) VALUES " +
            "<foreach collection='deptIds' item='deptId' separator=','>" +
            "(#{userId}, #{deptId})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("userId") Long userId, @Param("deptIds") java.util.List<Long> deptIds);

    /**
     * 批量插入用户管辖单位关联
     */
    @Select("select dept_id from sys_user_dept where user_id=#{userId}")
    List<Long> selectUserDeptIds(Long userId);}
