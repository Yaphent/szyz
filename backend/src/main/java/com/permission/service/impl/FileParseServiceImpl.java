package com.permission.service.impl;

import com.permission.exception.BusinessException;
import com.permission.service.FileParseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;

import java.io.InputStream;

/**
 * 基于 Apache Tika 的文件解析实现
 */
@Slf4j
@Service
public class FileParseServiceImpl implements FileParseService {

    /** 最大解析字符数 */
    private static final int MAX_LENGTH = 10_000;

    @Override
    public String parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件为空，无法解析");
        }
        try (InputStream in = file.getInputStream()) {
            // BodyContentHandler 传入 -1 表示无限制，这里自行截断
            ContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(in, handler, metadata, new ParseContext());
            String content = handler.toString();
            if (content == null) {
                return "";
            }
            // 去除多余空白并截断
            content = content.replaceAll("[\\t\\x0B\\f]", " ").trim();
            if (content.length() > MAX_LENGTH) {
                content = content.substring(0, MAX_LENGTH) + "...";
            }
            return content;
        } catch (Exception e) {
            log.error("文件解析失败: {}", file.getOriginalFilename(), e);
            throw new BusinessException("文件解析失败: " + e.getMessage());
        }
    }
}
