package com.permission.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件内容解析服务
 * 基于 Apache Tika，支持 PDF/Word/Excel/HTML/TXT/Markdown 等多种格式
 */
public interface FileParseService {

    /**
     * 解析文件文本内容（带长度限制）
     */
    String parse(MultipartFile file);
}
