package com.permission.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;

import javax.annotation.PostConstruct;

/**
 * 数据库初始化配置
 * 用于在应用启动时检查并创建缺失的数据库字段
 */
@Configuration
@Slf4j
public class DatabaseInitConfig {

    @Autowired(required = false) // 使用required=false以防JdbcTemplate不可用
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initDatabaseSchema() {
        if (jdbcTemplate == null) {
            log.warn("JdbcTemplate 不可用，跳过数据库表结构初始化");
            return;
        }
        
        try {
            // 检查并添加 Dify 相关字段
            addColumnIfNotExists("t_document", "dify_file_id", "VARCHAR(100) DEFAULT NULL COMMENT 'Dify知识流水线中的文件ID'");
            addColumnIfNotExists("t_document", "dify_processing_status", "VARCHAR(20) DEFAULT 'pending' COMMENT 'Dify处理状态:pending待处理,processing处理中,completed完成,failed失败'");
            addColumnIfNotExists("t_document", "dify_file_url", "VARCHAR(500) DEFAULT NULL COMMENT 'Dify中文件的访问URL'");
            addColumnIfNotExists("t_document", "dify_upload_time", "DATETIME DEFAULT NULL COMMENT 'Dify文件上传时间'");
            addColumnIfNotExists("t_document", "dify_pipeline_result", "TEXT DEFAULT NULL COMMENT 'Dify流水线处理结果'");
            addColumnIfNotExists("t_document", "dify_segment_count", "INT DEFAULT 0 COMMENT 'Dify分段数量'");
            addColumnIfNotExists("t_document", "dify_indexing_latency", "DECIMAL(10,3) DEFAULT 0.000 COMMENT 'Dify索引延迟(秒)'");
            addColumnIfNotExists("t_document", "dify_parsed_content_url", "VARCHAR(500) DEFAULT NULL COMMENT 'Dify解析后的内容URL'");
            
            log.info("数据库表结构初始化完成");
        } catch (Exception e) {
            log.error("数据库表结构初始化失败", e);
        }
    }

    /**
     * 如果列不存在则添加列
     */
    private void addColumnIfNotExists(String tableName, String columnName, String columnDefinition) {
        if (jdbcTemplate == null) {
            return;
        }
        
        try {
            // 检查列是否存在
            String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE table_name = ? AND column_name = ? AND table_schema = DATABASE()";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, tableName, columnName);
            
            if (count != null && count == 0) {
                // 列不存在，添加列
                String alterSql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition;
                jdbcTemplate.execute(alterSql);
                log.info("已添加列: {}.{}", tableName, columnName);
            } else {
                log.debug("列已存在: {}.{}", tableName, columnName);
            }
        } catch (DataAccessException e) {
            log.warn("数据库访问错误，可能需要手动执行SQL: {}.{} - {}", tableName, columnName, e.getMessage());
        } catch (Exception e) {
            log.warn("检查/添加列时出错: {}.{} - {}", tableName, columnName, e.getMessage());
        }
    }
}