package com.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.permission.common.PageResult;
import com.permission.entity.*;
import com.permission.mapper.*;
import com.permission.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private UserDeptMapper userDeptMapper;
    
    @Override
    public User getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }
    
    @Override
    public PageResult<User> getPage(int page, int pageSize, String username, String mobile, 
                                     Integer status, Long deptId, List<Long> deptIds) {
        Page<User> pageParam = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(User::getUsername, username);
        }
        if (mobile != null && !mobile.isEmpty()) {
            wrapper.like(User::getMobile, mobile);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (deptId != null) {
            wrapper.eq(User::getDeptId, deptId);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        
        Page<User> result = this.page(pageParam, wrapper);
        
        return new PageResult<>(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }
    
    @Override
    public User getUserDetails(Long userId) {
        User user = this.getById(userId);
        if (user != null) {
            user.setPassword(null);
            
            // 获取角色列表
            List<Long> roleIds = baseMapper.selectUserRoleIds(userId);
            user.setRoleIds(roleIds);
            
            // 获取管辖单位列表
            List<Long> deptIds = baseMapper.selectUserDeptIds(userId);
            user.setDeptIds(deptIds);
        }
        return user;
    }
    
    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            return false;
        }
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        return this.updateById(user);
    }
    
    @Override
    public boolean resetPassword(Long userId, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            return false;
        }
        
        user.setPassword(passwordEncoder.encode(newPassword != null ? newPassword : "admin123"));
        return this.updateById(user);
    }
    
    @Override
    public boolean updateAvatar(Long userId, String avatarUrl) {
        User user = new User();
        user.setUserId(userId);
        user.setAvatar(avatarUrl);
        return this.updateById(user);
    }
    
    @Override
    @Transactional
    public boolean assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            userRoleMapper.batchInsert(userId, roleIds);
        }
        return true;
    }
    
    @Override
    @Transactional
    public boolean assignDepts(Long userId, List<Long> deptIds) {
        userDeptMapper.deleteByUserId(userId);
        if (deptIds != null && !deptIds.isEmpty()) {
            userDeptMapper.batchInsert(userId, deptIds);
        }
        return true;
    }
    
    @Override
    public List<String> getUserPerms(Long userId) {
        return baseMapper.selectUserPerms(userId);
    }
    
    @Override
    public List<Menu> getUserMenus(Long userId) {
        if (isSuperAdmin(userId)) {
            return baseMapper.selectAllMenus();
        }
        return baseMapper.selectUserMenus(userId);
    }
    
    @Override
    public boolean isSuperAdmin(Long userId) {
        return baseMapper.isSuperAdmin(userId);
    }
    
    @Override
    public long getStatistics() {
        return this.count();
    }
    
    @Override
    @Transactional
    public boolean save(User user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("admin123");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        return super.save(user);
    }
    
    @Override
    @Transactional
    public boolean updateById(User user) {
        // 不更新密码字段
        user.setPassword(null);
        return super.updateById(user);
    }
}
