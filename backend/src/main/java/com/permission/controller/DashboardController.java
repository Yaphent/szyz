package com.permission.controller;

import com.permission.common.R;
import com.permission.service.DeptService;
import com.permission.service.RoleService;
import com.permission.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘控制器
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DeptService deptService;
    
    @Autowired
    private RoleService roleService;
    
    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public R<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUser", userService.getStatistics());
        statistics.put("totalDept", deptService.count());
        statistics.put("totalRole", roleService.getStatistics());
        return R.success(statistics);
    }
}
