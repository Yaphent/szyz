package com.permission.controller;

import com.permission.common.PageResult;
import com.permission.common.R;
import com.permission.entity.User;
import com.permission.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 分页查询
     */
    @GetMapping("/page")
    public R getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long deptId) {
        PageResult<User> page1 = userService.getPage(page, pageSize, username, mobile, status, deptId, null);
        
        return R.success(page1);
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{userId}")
    public R<User> getById(@PathVariable Long userId) {
        User user = userService.getUserDetails(userId);
        if (user == null) {
            return R.error(404, "用户不存在");
        }
        return R.success(user);
    }
    
    /**
     * 新增用户
     */
    @PostMapping
    public R<Long> create(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return R.error(400, "用户名不能为空");
        }
        
        User existing = userService.getByUsername(user.getUsername());
        if (existing != null) {
            return R.error(400, "用户名已存在");
        }
        
        userService.save(user);
        return R.success(user.getUserId(), "创建成功");
    }
    
    /**
     * 修改用户
     */
    @PutMapping
    public R update(@RequestBody User user) {
        if (user.getUserId() == null) {
            return R.error(400, "用户ID不能为空");
        }
        userService.updateById(user);
        return R.success(null, "修改成功");
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    public R delete(@PathVariable Long userId) {
        if (userId == 1) {
            return R.error(400, "不能删除超级管理员");
        }
        userService.removeById(userId);
        return R.success(null, "删除成功");
    }
    
    /**
     * 修改状态
     */
    @PutMapping("/status")
    public R updateStatus(@RequestBody Map<String, Object> params) {
        Long userId = ((Number) params.get("user_id")).longValue();
        Integer status = ((Number) params.get("status")).intValue();
        
        if (userId == 1 && status == 0) {
            return R.error(400, "不能禁用超级管理员");
        }
        
        User user = new User();
        user.setUserId(userId);
        user.setStatus(status);
        userService.updateById(user);
        
        return R.success(null, "状态更新成功");
    }
    
    /**
     * 分配角色
     */
    @PostMapping("/assign-roles")
    public R assignRoles(@RequestBody Map<String, Object> params) {
        Long userId = ((Number) params.get("user_id")).longValue();
        @SuppressWarnings("unchecked")
        List<Long> roleIds = ((List<Number>) params.get("role_ids"))
                .stream().map(Number::longValue).collect(Collectors.toList());
        
        userService.assignRoles(userId, roleIds);
        return R.success(null, "角色分配成功");
    }
    
    /**
     * 分配管辖单位
     */
    @PostMapping("/assign-depts")
    public R assignDepts(@RequestBody Map<String, Object> params) {
        Long userId = ((Number) params.get("user_id")).longValue();
        @SuppressWarnings("unchecked")
        List<Long> deptIds = ((List<Number>) params.get("dept_ids"))
                .stream().map(Number::longValue).collect(Collectors.toList());
        
        userService.assignDepts(userId, deptIds);
        return R.success(null, "管辖单位分配成功");
    }
    
    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    public R changePassword(@RequestBody Map<String, String> params) {
        // 从请求中获取用户ID（需要认证拦截器注入）
        Long userId = 1L; // 需要从Token中获取
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        try {
            userService.changePassword(userId, oldPassword, newPassword);
            return R.success(null, "密码修改成功");
        } catch (RuntimeException e) {
            return R.error(400, e.getMessage());
        }
    }
    
    /**
     * 重置密码
     */
    @PutMapping("/reset-password/{userId}")
    public R resetPassword(@PathVariable Long userId,
                          @RequestBody(required = false) Map<String, String> params) {
        String newPassword = params != null ? params.get("newPassword") : "admin123";
        userService.resetPassword(userId, newPassword);
        return R.success(null, "密码重置成功");
    }
    
    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public R<String> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        if (file.isEmpty()) {
            return R.error(400, "请选择图片文件");
        }
        
        // 保存文件
        String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String uploadPath = System.getProperty("user.dir") + "/uploads/avatar/";
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        try {
            file.transferTo(new File(uploadPath + filename));
            String avatarUrl = "/uploads/avatar/" + filename;
            
            // 更新用户头像
            userService.updateAvatar(1L, avatarUrl); // 需要从Token中获取用户ID
            
            return R.success(avatarUrl, "头像上传成功");
        } catch (IOException e) {
            return R.error(500, "上传失败");
        }
    }
}
