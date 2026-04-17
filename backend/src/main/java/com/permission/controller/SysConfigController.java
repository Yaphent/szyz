package com.permission.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.permission.common.R;
import com.permission.entity.SysConfig;
import com.permission.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统参数控制器
 */
@RestController
@RequestMapping("/api/config")
public class SysConfigController {
    
    @Autowired
    private SysConfigService sysConfigService;
    
    /**
     * 分页查询
     */
    @GetMapping("/page")
    public R<Page<SysConfig>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String config_name,
            @RequestParam(required = false) String config_key,
            @RequestParam(required = false) String config_type) {
        
        return R.success(sysConfigService.getPage(page, pageSize, config_name, config_key, config_type));
    }
    
    /**
     * 获取全部参数
     */
    @GetMapping("/list")
    public R<List<SysConfig>> getAll() {
        return R.success(sysConfigService.getAll());
    }
    
    /**
     * 获取参数详情
     */
    @GetMapping("/{configId}")
    public R<SysConfig> getById(@PathVariable Long configId) {
        SysConfig config = sysConfigService.getById(configId);
        if (config == null) {
            return R.error(404, "参数不存在");
        }
        return R.success(config);
    }
    
    /**
     * 根据键名获取值
     */
    @GetMapping("/value/{configKey}")
    public R<String> getValueByKey(@PathVariable String configKey) {
        return R.success(sysConfigService.getValueByKey(configKey));
    }
    
    /**
     * 新增参数
     */
    @PostMapping
    public R<Long> create(@RequestBody SysConfig config) {
        if (config.getConfigName() == null || config.getConfigKey() == null) {
            return R.error(400, "参数名称和键名不能为空");
        }
        
        try {
            sysConfigService.save(config);
            return R.success(config.getConfigId(), "创建成功");
        } catch (RuntimeException e) {
            return R.error(400, e.getMessage());
        }
    }
    
    /**
     * 修改参数
     */
    @PutMapping
    public R update(@RequestBody SysConfig config) {
        if (config.getConfigId() == null) {
            return R.error(400, "参数ID不能为空");
        }
        
        try {
            sysConfigService.update(config);
            return R.success(null, "修改成功");
        } catch (RuntimeException e) {
            return R.error(400, e.getMessage());
        }
    }
    
    /**
     * 删除参数
     */
    @DeleteMapping("/{configId}")
    public R delete(@PathVariable Long configId) {
        sysConfigService.delete(configId);
        return R.success(null, "删除成功");
    }
    
    /**
     * 刷新缓存
     */
    @PostMapping("/refresh-cache")
    public R refreshCache() {
        sysConfigService.refreshCache();
        return R.success(null, "缓存刷新成功");
    }
}
