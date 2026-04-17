package com.permission.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.permission.entity.Dept;
import com.permission.mapper.DeptMapper;
import com.permission.mapper.UserDeptMapper;
import com.permission.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 单位服务实现
 */
@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {
    @Autowired
    private UserDeptMapper userDeptMapper;

    @Override
    public List<Dept> getTree() {
        List<Dept> allDepts = this.list();
        return buildTree(allDepts, 0L);
    }
    
    /**
     * 构建树形结构
     */
    private List<Dept> buildTree(List<Dept> allDepts, Long parentId) {
        return allDepts.stream()
                .filter(dept -> dept.getParentId().equals(parentId))
                .peek(dept -> {
                    List<Dept> children = buildTree(allDepts, dept.getDeptId());
                    if (!children.isEmpty()) {
                        dept.setChildren(children);
                    }
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public Dept getById(Long deptId) {
        return this.getById(deptId);
    }
    
    @Override
    public boolean hasChildren(Long deptId) {
        return this.lambdaQuery().eq(Dept::getParentId, deptId).count() > 0;
    }
    
    @Override
    public boolean hasUsers(Long deptId) {
        return false; // 需要关联查询，简化为false
    }
    
    @Override
    public List<Long> getUserDeptIds(Long userId, boolean isSuperAdmin) {
        if (isSuperAdmin) {
            // 超级管理员返回所有单位
            return this.list().stream()
                    .map(Dept::getDeptId)
                    .collect(Collectors.toList());
        }
        return userDeptMapper.selectUserDeptIds(userId);
    }
    
    @Transactional
    @Override
    public boolean save(Dept dept) {
        if (dept.getParentId() == null) {
            dept.setParentId(0L);
        }
        if (dept.getSort() == null) {
            dept.setSort(0);
        }
        if (dept.getStatus() == null) {
            dept.setStatus(1);
        }
        return super.save(dept);
    }
    
    @Transactional
    @Override
    public boolean updateById(Dept dept) {
        return super.updateById(dept);
    }
}
