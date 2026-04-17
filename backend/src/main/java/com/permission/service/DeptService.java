package com.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.permission.entity.Dept;
import java.util.List;

/**
 * 单位服务接口
 */
public interface DeptService extends IService<Dept> {
    
    /**
     * 获取单位树
     */
    List<Dept> getTree();
    
    /**
     * 根据ID获取单位详情
     */
    Dept getById(Long deptId);
    
    /**
     * 检查是否有子单位
     */
    boolean hasChildren(Long deptId);
    
    /**
     * 检查是否有关联用户
     */
    boolean hasUsers(Long deptId);
    
    /**
     * 获取用户管辖的所有单位ID
     */
    List<Long> getUserDeptIds(Long userId, boolean isSuperAdmin);
}
