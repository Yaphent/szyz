package com.permission.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.permission.entity.SysConfig;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 */
public interface SysConfigService {
    
    /**
     * 分页查询
     */
    Page<SysConfig> getPage(int page, int pageSize, String configName, String configKey, String configType);
    
    /**
     * 获取全部参数
     */
    List<SysConfig> getAll();
    
    /**
     * 根据ID查询
     */
    SysConfig getById(Long configId);
    
    /**
     * 根据键名获取值
     */
    String getValue(String configKey);
    
    /**
     * 根据键名获取值（别名）
     */
    String getValueByKey(String configKey);
    
    /**
     * 根据键名获取布尔值
     */
    boolean getBooleanValue(String configKey, boolean defaultValue);
    
    /**
     * 根据键名获取整数值
     */
    int getIntValue(String configKey, int defaultValue);
    
    /**
     * 获取所有配置
     */
    Map<String, String> getAllConfig();
    
    /**
     * 新增参数
     */
    boolean save(SysConfig config);
    
    /**
     * 修改参数
     */
    boolean update(SysConfig config);
    
    /**
     * 删除参数
     */
    boolean delete(Long configId);
    
    /**
     * 刷新配置缓存
     */
    void refreshCache();
}
