package com.permission.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.permission.common.R;
import com.permission.config.JwtUtils;
import com.permission.entity.User;
import com.permission.service.MenuService;
import com.permission.service.SysConfigService;
import com.permission.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SysConfigService sysConfigService;

    @Value("${captcha.expiration}")
    private long captcha_expiration;

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public R getCaptcha() {
        // 检查是否启用验证码
        boolean captchaEnabled = sysConfigService.getBooleanValue("sys.login.captcha", true);
        if (!captchaEnabled) {
            return R.success(Map.of("captchaEnabled", false));
        }

        // 生成图形验证码
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 50);
        String code = captcha.getCode();
        String key = UUID.randomUUID().toString();

        // 存储验证码到Redis
        redisTemplate.opsForValue().set("captcha:" + key, code, captcha_expiration, TimeUnit.MILLISECONDS);

        Map<String, Object> result = new HashMap<>();
        result.put("captchaEnabled", true);
        result.put("captchaKey", key);
        // 返回base64编码的图片
        result.put("captchaImage", "data:image/png;base64," + captcha.getImageBase64());

        return R.success(result);
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public R login(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String username = params.get("username");
        String password = params.get("password");
        String captchaCode = params.get("captchaCode");
        String captchaKey = params.get("captchaKey");

        if (username == null || password == null) {
            return R.error(400, "用户名和密码不能为空");
        }

        // 检查是否启用验证码
        boolean captchaEnabled = sysConfigService.getBooleanValue("sys.login.captcha", true);

        // 验证验证码（如果提供了验证码且启用了验证码）
        if (captchaEnabled && captchaKey != null && captchaCode != null && !captchaKey.isEmpty()) {
            String cachedCode = redisTemplate.opsForValue().get("captcha:" + captchaKey);
            if (cachedCode == null) {
                return R.error(400, "验证码已过期");
            }
            if (!cachedCode.equalsIgnoreCase(captchaCode)) {
                return R.error(400, "验证码错误");
            }
            // 验证成功后删除验证码
            redisTemplate.delete("captcha:" + captchaKey);
        }

        // 查询用户
        User user = userService.getByUsername(username);
        if (user == null) {
            return R.error(401, "用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            return R.error(401, "账号已被禁用");
        }

        // 验证密码
        // if (!passwordEncoder.matches(password, user.getPassword())) {
        //     return R.error(401, "用户名或密码错误");
        // }

        // 检查是否为超级管理员
        boolean isSuperAdmin = userService.isSuperAdmin(user.getUserId());
        
        // 获取管辖单位列表
        List<Long> deptIds = userService.getUserDetails(user.getUserId()).getDeptIds();

        // 获取Token有效期（小时）
        int tokenExpireHours = sysConfigService.getIntValue("sys.token.expire", 24);

        // 生成Token
        String token = jwtUtils.generateToken(user.getUserId(), user.getUsername(), isSuperAdmin);

        // 将Token存入Redis
        redisTemplate.opsForValue().set("token:" + user.getUserId(), token, tokenExpireHours, TimeUnit.HOURS);

        // 返回系统配置信息
        Map<String, String> systemConfig = sysConfigService.getAllConfig();

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", Map.of(
                "userId", user.getUserId(),
                "username", user.getUsername(),
                "nickname", user.getNickname(),
                "avatar", user.getAvatar() != null ? user.getAvatar() : "",
                "deptId", user.getDeptId() != null ? user.getDeptId() : 0,
                "deptName", user.getDeptName() != null ? user.getDeptName() : ""
        ));
        result.put("deptIds", deptIds != null ? deptIds : List.of());
        result.put("systemConfig", systemConfig);

        return R.success(result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public R getUserInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return R.error(401, "未登录");
        }

        String tokenStr = token.substring(7);
        Long userId = jwtUtils.getUserId(tokenStr);

        User user = userService.getUserDetails(userId);
        if (user == null) {
            return R.error(404, "用户不存在");
        }

        List<String> perms = userService.getUserPerms(userId);

        return R.success(Map.of(
                "user", Map.of(
                        "userId", user.getUserId(),
                        "username", user.getUsername(),
                        "nickname", user.getNickname(),
                        "email", user.getEmail() != null ? user.getEmail() : "",
                        "mobile", user.getMobile() != null ? user.getMobile() : "",
                        "avatar", user.getAvatar() != null ? user.getAvatar() : "",
                        "deptId", user.getDeptId() != null ? user.getDeptId() : 0,
                        "deptName", user.getDeptName() != null ? user.getDeptName() : ""
                ),
                "perms", perms
        ));
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public R logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            String tokenStr = token.substring(7);
            try {
                Long userId = jwtUtils.getUserId(tokenStr);
                // 从Redis删除Token
                redisTemplate.delete("token:" + userId);
            } catch (Exception e) {
                // 忽略错误
            }
        }
        return R.success(null, "登出成功");
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public R refreshToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return R.error(401, "未登录");
        }

        String tokenStr = token.substring(7);
        Long userId = jwtUtils.getUserId(tokenStr);

        // 检查旧Token是否有效
        String oldToken = redisTemplate.opsForValue().get("token:" + userId);
        if (!tokenStr.equals(oldToken)) {
            return R.error(401, "Token已失效，请重新登录");
        }

        User user = userService.getById(userId);
        boolean isSuperAdmin = userService.isSuperAdmin(userId);

        // 生成新Token
        String newToken = jwtUtils.generateToken(userId, user.getUsername(), isSuperAdmin);

        // 更新Redis中的Token
        redisTemplate.opsForValue().set("token:" + userId, newToken, 24, java.util.concurrent.TimeUnit.HOURS);

        return R.success(Map.of("token", newToken), "Token已刷新");
    }
}
