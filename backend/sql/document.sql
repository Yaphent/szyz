-- ============================================================
-- 法规文档管理模块 建表脚本
-- 适用于 MySQL 8.0
-- 字符集 utf8mb4, 引擎 InnoDB
-- ============================================================

USE permission_db;

-- ------------------------------------------------------------
-- 1. 文档主表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_document;
CREATE TABLE t_document (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT    COMMENT '主键ID',
    name              VARCHAR(200) NOT NULL                COMMENT '文档名称',
    policy_prefix     VARCHAR(50)  DEFAULT NULL            COMMENT '文号前缀,如"中发"',
    policy_year       VARCHAR(10)  DEFAULT NULL            COMMENT '文号年份,如"〔2017〕"',
    policy_no         VARCHAR(50)  DEFAULT NULL            COMMENT '文号编号,如"33"',
    business_type     VARCHAR(50)  NOT NULL                COMMENT '业务分类(字典code)',
    keywords          VARCHAR(500) DEFAULT NULL            COMMENT '关键字，多个用英文逗号分隔',
    issuing_authority VARCHAR(100) NOT NULL                COMMENT '发文机关',
    issuing_date      DATE         NOT NULL                COMMENT '发文日期',
    effective_date    DATE         NOT NULL                COMMENT '实施日期',
    expiry_date       DATE         DEFAULT NULL            COMMENT '失效日期',
    law_level         VARCHAR(50)  DEFAULT NULL            COMMENT '法规层次',
    timeliness        VARCHAR(20)  DEFAULT NULL            COMMENT '时效性(valid/invalid/pending)',
    summary           TEXT         DEFAULT NULL            COMMENT '摘要',
    control_mode      VARCHAR(30)  NOT NULL DEFAULT 'UNIFIED' COMMENT '管控模式:UNIFIED统建 SELF自建 CUSTOM自定义范围统建',
    main_file_id      BIGINT       DEFAULT NULL            COMMENT '主文件ID(法规文件)',
    status            TINYINT      DEFAULT 1               COMMENT '状态:1启用 0停用',
    deleted           TINYINT      DEFAULT 0               COMMENT '逻辑删除:0未删 1已删',
    -- Dify 相关字段
    dify_document_id  VARCHAR(100) DEFAULT NULL            COMMENT 'Dify知识库中的文档ID',
    summary_status    TINYINT      DEFAULT 0               COMMENT '摘要状态:0待处理 1处理中 2已完成 3失败',
    vectorization_status TINYINT   DEFAULT 0               COMMENT '向量化状态:0待处理 1处理中 2已完成 3失败',
    create_by         VARCHAR(50)  DEFAULT NULL            COMMENT '创建人',
    create_time       DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by         VARCHAR(50)  DEFAULT NULL            COMMENT '更新人',
    update_time       DATETIME     DEFAULT NULL            COMMENT '更新时间',
    KEY idx_name (name),
    KEY idx_policy_no (policy_no),
    KEY idx_business_type (business_type),
    KEY idx_issuing_date (issuing_date),
    KEY idx_status (status),
    KEY idx_dify_document_id (dify_document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='法规文档主表';

-- ------------------------------------------------------------
-- 2. 文档文件关联表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_document_file;
CREATE TABLE t_document_file (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    document_id   BIGINT       DEFAULT NULL         COMMENT '文档ID(上传时可能为空,保存时回填)',
    file_name     VARCHAR(255) NOT NULL             COMMENT '原始文件名',
    file_path     VARCHAR(500) NOT NULL             COMMENT '文件存储路径或对象key',
    file_url      VARCHAR(500) DEFAULT NULL         COMMENT '可访问URL',
    file_size     BIGINT       DEFAULT 0            COMMENT '文件大小(字节)',
    file_type     VARCHAR(20)  DEFAULT NULL         COMMENT '文件类型(扩展名)',
    file_category TINYINT      DEFAULT 2            COMMENT '1主文件 2附件',
    storage_type  VARCHAR(20)  DEFAULT 'local'      COMMENT '存储类型:local/minio',
    upload_time   DATETIME     DEFAULT NULL         COMMENT '上传时间',
    KEY idx_document_id (document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档文件表';

-- ------------------------------------------------------------
-- 3. 文档标签关联表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_document_tag;
CREATE TABLE t_document_tag (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    document_id BIGINT      NOT NULL              COMMENT '文档ID',
    tag_name    VARCHAR(50) NOT NULL              COMMENT '标签名',
    KEY idx_document_id (document_id),
    KEY idx_tag_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档-标签关联表';

-- ------------------------------------------------------------
-- 4. 标签字典表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS t_dict_tag;
CREATE TABLE t_dict_tag (
    id        BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tag_name  VARCHAR(50) NOT NULL              COMMENT '标签名',
    is_system TINYINT     DEFAULT 0             COMMENT '1系统预置 0自定义',
    sort_no   INT         DEFAULT 0             COMMENT '排序',
    UNIQUE KEY uk_tag_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签字典表';

-- ------------------------------------------------------------
-- 初始化预置标签
-- ------------------------------------------------------------
INSERT INTO t_dict_tag (tag_name, is_system, sort_no) VALUES
('重点支出',       1, 1),
('脱贫攻坚',       1, 2),
('生态环保',       1, 3),
('基础建设',       1, 4),
('重大项目投资',   1, 5),
('国有土地使用权出让', 1, 6),
('民生工程',       1, 7),
('三公经费',       1, 8),
('政府采购',       1, 9);
