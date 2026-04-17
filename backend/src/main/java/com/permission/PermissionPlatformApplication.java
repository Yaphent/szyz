package com.permission;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.permission.mapper")
public class PermissionPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(PermissionPlatformApplication.class, args);
        System.out.println("================================================");
        System.out.println("  企业级权限管理平台已启动!");
        System.out.println("  访问地址: http://localhost:8080");
        System.out.println("  API文档: http://localhost:8080/swagger-ui.html");
        System.out.println("================================================");
    }
}
