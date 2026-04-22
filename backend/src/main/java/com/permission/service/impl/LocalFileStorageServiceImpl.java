package com.permission.service.impl;

import com.permission.config.FileStorageProperties;
import com.permission.exception.BusinessException;
import com.permission.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地文件存储实现
 * 仅当配置 file-storage.type=local（默认）时生效
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "file-storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageServiceImpl implements FileStorageService {

    private final FileStorageProperties properties;

    public LocalFileStorageServiceImpl(FileStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public String upload(MultipartFile file, String objectName) {
        try {
            Path target = Paths.get(properties.getLocal().getRootPath(), objectName).toAbsolutePath();
            Files.createDirectories(target.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("本地文件已存储: {}", target);
            // 返回可访问 URL
            return properties.getDomain() + properties.getLocal().getUrlPrefix() + "/" + objectName.replace("\\", "/");
        } catch (IOException e) {
            log.error("本地文件存储失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream download(String objectName) {
        try {
            Path target = Paths.get(properties.getLocal().getRootPath(), objectName).toAbsolutePath();
            if (!Files.exists(target)) {
                throw new BusinessException("文件不存在");
            }
            return new BufferedInputStream(new FileInputStream(target.toFile()));
        } catch (FileNotFoundException e) {
            throw new BusinessException("文件不存在");
        }
    }

    @Override
    public void delete(String objectName) {
        try {
            Path target = Paths.get(properties.getLocal().getRootPath(), objectName).toAbsolutePath();
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.error("删除文件失败", e);
        }
    }

    @Override
    public String getStorageType() {
        return "local";
    }
}
