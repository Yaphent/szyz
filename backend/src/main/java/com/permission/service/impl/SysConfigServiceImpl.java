package com.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.permission.entity.SysConfig;
import com.permission.mapper.SysConfigMapper;
import com.permission.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 系统参数服务实现
 */
@Service
public class SysConfigServiceImpl implements SysConfigService {
    
    private static final String CACHE_KEY_PREFIX = "sys:config:";
    private static final long CACHE_EXPIRE_HOURS = 24;
    
    @Autowired
    private SysConfigMapper sysConfigMapper;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Override
    public Page<SysConfig> getPage(int page, int pageSize, String configName, String configKey, String configType) {
        Page<SysConfig> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(configName)) {
            wrapper.like(SysConfig::getConfigName, configName);
        }
        if (StringUtils.hasText(configKey)) {
            wrapper.like(SysConfig::getConfigKey, configKey);
        }
        if (StringUtils.hasText(configType)) {
            wrapper.eq(SysConfig::getConfigType, configType);
        }
        
        wrapper.orderByDesc(SysConfig::getCreateTime);
        return sysConfigMapper.selectPage(pageInfo, wrapper);
    }
    
    @Override
    public List<SysConfig> getAll() {
        return sysConfigMapper.selectList(null);
    }
    
    @Override
    public SysConfig getById(Long configId) {
        return sysConfigMapper.selectById(configId);
    }
    
    @Override
    public String getValueByKey(String configKey) {
        // 先从缓存获取
        String cacheKey = CACHE_KEY_PREFIX + configKey;
        String cachedValue = redisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            return cachedValue;
        }
        
        // 从数据库查询
        String value = sysConfigMapper.selectValueByKey(configKey);
        if (value != null) {
            // 写入缓存
            redisTemplate.opsForValue().set(cacheKey, value, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }
        return value;
    }
    
    @Override
    public boolean save(SysConfig config) {
        // 检查键名唯一性
        SysConfig existing = sysConfigMapper.selectByConfigKey(config.getConfigKey());
        if (existing != null) {
            throw new RuntimeException("参数键名已存在");
        }
        return sysConfigMapper.insert(config) > 0;
    }
    
    @Override
    public boolean update(SysConfig config) {
        // 检查键名唯一性（排除自身）
        SysConfig existing = sysConfigMapper.selectByConfigKey(config.getConfigKey());
        if (existing != null && !existing.getConfigId().equals(config.getConfigId())) {
            throw new RuntimeException("参数键名已存在");
        }
        
        // 清除缓存
        clearCache(config.getConfigKey());
        return sysConfigMapper.updateById(config) > 0;
    }
    
    @Override
    public boolean delete(Long configId) {
        SysConfig config = sysConfigMapper.selectById(configId);
        if (config != null) {
            clearCache(config.getConfigKey());
        }
        return sysConfigMapper.deleteById(configId) > 0;
    }
    
    @Override
    public void refreshCache() {
        // 清除所有参数缓存
        List<SysConfig> configs = sysConfigMapper.selectList(null);
        for (SysConfig config : configs) {
            clearCache(config.getConfigKey());
        }
    }
    
    /**
     * 清除缓存
     */
    private void clearCache(String configKey) {
        String cacheKey = CACHE_KEY_PREFIX + configKey;
        redisTemplate.delete(cacheKey);
    }
}
