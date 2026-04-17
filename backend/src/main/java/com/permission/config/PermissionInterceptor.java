package com.permission.config;

import com.permission.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 权限拦截器
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 公开接口
    private static final String[] PUBLIC_PATHS = {
            "/api/auth/captcha",
            "/api/auth/login",
            "/api/dashboard/statistics",
            "/health"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        // 检查是否为公开接口
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }

        // 获取Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未认证，请先登录\"}");
            return false;
        }

        String token = authHeader.substring(7);

        // 验证Token
        if (!jwtUtils.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"Token无效\"}");
            return false;
        }

        // 检查Token是否过期
        if (jwtUtils.isTokenExpired(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"Token已过期\"}");
            return false;
        }

        // 从Token中获取用户ID
        Long userId = jwtUtils.getUserId(token);

        // 从Redis检查Token是否有效（可选，用于单点登录控制）
        String storedToken = redisTemplate.opsForValue().get("token:" + userId);
        if (storedToken != null && !token.equals(storedToken)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"账号已在其他设备登录\"}");
            return false;
        }

        // 超级管理员拥有所有权限
        if (jwtUtils.isSuperAdmin(token)) {
            request.setAttribute("userId", userId);
            return true;
        }

        // 获取用户权限列表
        List<String> userPerms = userService.getUserPerms(userId);

        // 根据请求方法生成需要的权限标识
        String method = request.getMethod().toUpperCase();
        String requiredPerm = generateRequiredPerm(path, method);

        // 如果没有匹配到具体权限，也放行
        if (requiredPerm == null || requiredPerm.isEmpty()) {
            request.setAttribute("userId", userId);
            return true;
        }

        // 检查用户是否有该权限
        if (userPerms.contains(requiredPerm)) {
            request.setAttribute("userId", userId);
            return true;
        }

        // 无权限
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"msg\":\"没有权限访问该资源\"}");
        return false;
    }

    /**
     * 根据请求路径和方法生成权限标识
     */
    private String generateRequiredPerm(String path, String method) {
        // 移除/api前缀
        if (path.startsWith("/api")) {
            path = path.substring(4);
        }

        String[] parts = path.split("/");
        if (parts.length < 2) {
            return null;
        }

        // 获取资源名称
        String resource = parts[1];

        // 生成权限标识
        String action;
        switch (method) {
            case "GET":
                action = "list";
                break;
            case "POST":
                action = "add";
                break;
            case "PUT":
                action = "edit";
                break;
            case "DELETE":
                action = "delete";
                break;
            default:
                action = "list";
        }

        return "sys:" + resource + ":" + action;
    }
}
