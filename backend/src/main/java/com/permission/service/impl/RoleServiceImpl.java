package com.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.permission.common.PageResult;
import com.permission.entity.Menu;
import com.permission.entity.Role;
import com.permission.entity.RoleMenu;
import com.permission.mapper.RoleMapper;
import com.permission.mapper.RoleMenuMapper;
import com.permission.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    
    @Autowired
    private RoleMenuMapper roleMenuMapper;
    
    @Override
    public PageResult<Role> getPage(int page, int pageSize, String roleName, String roleCode, Integer status) {
        Page<Role> pageParam = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (roleName != null && !roleName.isEmpty()) {
            wrapper.like(Role::getRoleName, roleName);
        }
        if (roleCode != null && !roleCode.isEmpty()) {
            wrapper.like(Role::getRoleCode, roleCode);
        }
        if (status != null) {
            wrapper.eq(Role::getStatus, status);
        }
        
        wrapper.orderByDesc(Role::getCreateTime);
        
        Page<Role> result = this.page(pageParam, wrapper);
        
        return new PageResult<>(result.getRecords(), result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }
    
    @Override
    public List<Role> getAll() {
        return this.lambdaQuery().eq(Role::getStatus, 1).orderByAsc(Role::getCreateTime).list();
    }
    
    @Override
    public Role getRoleDetails(Long roleId) {
        Role role = this.getById(roleId);
        if (role != null) {
            List<Menu> menus = baseMapper.selectRoleMenus(roleId);
            List<Long> menuIds = menus.stream().map(Menu::getMenuId).collect(Collectors.toList());
            role.setMenus(menus);
            role.setMenuIds(menuIds);
        }
        return role;
    }
    
    @Override
    public Role getByRoleCode(String roleCode) {
        return baseMapper.selectByRoleCode(roleCode);
    }
    
    @Override
    public boolean hasUsers(Long roleId) {
        return baseMapper.hasUsers(roleId);
    }
    
    @Override
    @Transactional
    public boolean assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.deleteByRoleId(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            roleMenuMapper.batchInsert(roleId, menuIds);
        }
        return true;
    }
    
    @Override
    public long getStatistics() {
        return this.lambdaQuery().eq(Role::getStatus, 1).count();
    }
    
    @Override
    @Transactional
    public boolean save(Role role) {
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        return super.save(role);
    }
}
