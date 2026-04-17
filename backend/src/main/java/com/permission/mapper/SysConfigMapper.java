package com.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.permission.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统参数Mapper
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
    
    /**
     * 根据键名查询
     */
    @Select("SELECT * FROM sys_config WHERE config_key = #{configKey}")
    SysConfig selectByConfigKey(@Param("configKey") String configKey);
    
    /**
     * 根据键名查询启用的参数值
     */
    @Select("SELECT config_value FROM sys_config WHERE config_key = #{configKey} AND status = 1")
    String selectValueByKey(@Param("configKey") String configKey);
}
