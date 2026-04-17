package com.permission.controller;

import com.permission.common.R;
import com.permission.entity.Dept;
import com.permission.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 单位控制器
 */
@RestController
@RequestMapping("/api/dept")
public class DeptController {
    
    @Autowired
    private DeptService deptService;
    
    /**
     * 获取单位树
     */
    @GetMapping("/tree")
    public R<List<Dept>> getTree() {
        return R.success(deptService.getTree());
    }
    
    /**
     * 获取单位详情
     */
    @GetMapping("/{deptId}")
    public R<Dept> getById(@PathVariable Long deptId) {
        Dept dept = deptService.getById(deptId);
        if (dept == null) {
            return R.error(404, "单位不存在");
        }
        return R.success(dept);
    }
    
    /**
     * 新增单位
     */
    @PostMapping
    public R<Long> create(@RequestBody Dept dept) {
        if (dept.getDeptName() == null || dept.getDeptName().isEmpty()) {
            return R.error(400, "单位名称不能为空");
        }
        deptService.save(dept);
        return R.success(dept.getDeptId(), "创建成功");
    }
    
    /**
     * 修改单位
     */
    @PutMapping
    public R update(@RequestBody Dept dept) {
        if (dept.getDeptId() == null) {
            return R.error(400, "单位ID不能为空");
        }
        if (dept.getDeptName() == null || dept.getDeptName().isEmpty()) {
            return R.error(400, "单位名称不能为空");
        }
        deptService.updateById(dept);
        return R.success(null, "修改成功");
    }
    
    /**
     * 删除单位
     */
    @DeleteMapping("/{deptId}")
    public R delete(@PathVariable Long deptId) {
        if (deptService.hasChildren(deptId)) {
            return R.error(400, "存在子单位，不允许删除");
        }
        if (deptService.hasUsers(deptId)) {
            return R.error(400, "存在关联用户，不允许删除");
        }
        deptService.removeById(deptId);
        return R.success(null, "删除成功");
    }
}
