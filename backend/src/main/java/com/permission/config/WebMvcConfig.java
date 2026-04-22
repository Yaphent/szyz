package com.permission.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * WebMvc配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                // 文档静态资源、下载、解析等允许匿名访问时排除拦截
                .excludePathPatterns(
                        "/api/document/static/**",
                        "/api/document/download/**"
                );
    }

    /**
     * 将本地文件存储目录映射为可访问的静态资源路径
     * 仅在 file-storage.type=local 时有效
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if ("local".equalsIgnoreCase(fileStorageProperties.getType())) {
            String rootPath = Paths.get(fileStorageProperties.getLocal().getRootPath())
                    .toAbsolutePath().toString().replace("\\", "/");
            String urlPrefix = fileStorageProperties.getLocal().getUrlPrefix();
            if (!urlPrefix.endsWith("/")) {
                urlPrefix = urlPrefix + "/";
            }
            registry.addResourceHandler(urlPrefix + "**")
                    .addResourceLocations("file:" + rootPath + "/");
        }
    }
}
