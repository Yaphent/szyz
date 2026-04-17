package com.permission.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.permission.entity.SysConfig;

/**
 * 系统参数服务接口
 */
public interface SysConfigService {
    
    /**
     * 分页查询
     */
    Page<SysConfig> getPage(int page, int pageSize, String configName, String configKey, String configType);
    
    /**
     * 获取全部参数
     */
    java.util.List<SysConfig> getAll();
    
    /**
     * 根据ID查询
     */
    SysConfig getById(Long configId);
    
    /**
     * 根据键名查询值
     */
    String getValueByKey(String configKey);
    
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
     * 刷新缓存
     */
    void refreshCache();
}
