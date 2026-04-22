package com.permission.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file-storage")
public class FileStorageProperties {

    /** 存储类型 local / minio */
    private String type = "local";

    /** 访问基础URL(用于拼接文件访问路径) */
    private String domain = "http://localhost:8080";

    /** 本地存储配置 */
    private Local local = new Local();

    /** MinIO存储配置 */
    private Minio minio = new Minio();

    @Data
    public static class Local {
        /** 本地文件根目录 */
        private String rootPath = "./upload";
        /** HTTP 访问前缀 */
        private String urlPrefix = "/api/document/static";
    }

    @Data
    public static class Minio {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucket = "document";
    }
}
