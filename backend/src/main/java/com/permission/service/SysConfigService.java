package com.permission.service;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 */
public interface SysConfigService {
    
    /**
     * 根据键名获取值
     */
    String getValue(String configKey);
    
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
     * 刷新配置缓存
     */
    void refreshCache();
}
