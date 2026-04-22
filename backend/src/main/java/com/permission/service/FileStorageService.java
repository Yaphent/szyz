package com.permission.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件存储服务接口（本地/MinIO 统一抽象）
 */
public interface FileStorageService {

    /**
     * 上传文件
     *
     * @param file       文件
     * @param objectName 存储对象名(相对路径/对象key)
     * @return 可访问URL
     */
    String upload(MultipartFile file, String objectName);

    /**
     * 下载文件
     */
    InputStream download(String objectName);

    /**
     * 删除文件
     */
    void delete(String objectName);

    /**
     * 获取存储类型标识（local / minio）
     */
    String getStorageType();
}
