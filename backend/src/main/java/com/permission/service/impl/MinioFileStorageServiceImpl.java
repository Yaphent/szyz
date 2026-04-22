package com.permission.service.impl;

import com.permission.config.FileStorageProperties;
import com.permission.exception.BusinessException;
import com.permission.service.FileStorageService;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;

/**
 * MinIO 文件存储实现
 * 仅当配置 file-storage.type=minio 时生效
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "file-storage", name = "type", havingValue = "minio")
public class MinioFileStorageServiceImpl implements FileStorageService {

    private final FileStorageProperties properties;
    private MinioClient minioClient;

    public MinioFileStorageServiceImpl(FileStorageProperties properties) {
        this.properties = properties;
    }

    /**
     * 初始化 MinIO 客户端并确保 Bucket 存在
     */
    @PostConstruct
    public void init() {
        FileStorageProperties.Minio cfg = properties.getMinio();
        this.minioClient = MinioClient.builder()
                .endpoint(cfg.getEndpoint())
                .credentials(cfg.getAccessKey(), cfg.getSecretKey())
                .build();

        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(cfg.getBucket()).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(cfg.getBucket()).build());
                log.info("创建 MinIO Bucket: {}", cfg.getBucket());
            }
        } catch (Exception e) {
            log.error("MinIO 初始化失败", e);
        }
    }

    @Override
    public String upload(MultipartFile file, String objectName) {
        FileStorageProperties.Minio cfg = properties.getMinio();
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(cfg.getBucket())
                    .object(objectName)
                    .stream(in, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            log.info("MinIO 文件已上传: {}/{}", cfg.getBucket(), objectName);
            return cfg.getEndpoint() + "/" + cfg.getBucket() + "/" + objectName;
        } catch (Exception e) {
            log.error("MinIO 上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream download(String objectName) {
        FileStorageProperties.Minio cfg = properties.getMinio();
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(cfg.getBucket())
                    .object(objectName)
                    .build());
        } catch (ErrorResponseException e) {
            throw new BusinessException("文件不存在");
        } catch (Exception e) {
            log.error("MinIO 下载失败", e);
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(String objectName) {
        FileStorageProperties.Minio cfg = properties.getMinio();
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(cfg.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("MinIO 删除失败", e);
        }
    }

    @Override
    public String getStorageType() {
        return "minio";
    }
}
