-- ================================================
-- 企业级权限管理平台 - 数据库清理脚本
-- 执行此脚本后再执行 init.sql 即可重新初始化
-- ================================================

USE permission_db;

-- 关闭外键检查（确保可以删除有外键关联的表）
SET FOREIGN_KEY_CHECKS = 0;

-- 删除关联表
DROP TABLE IF EXISTS sys_user_dept;
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user_role;

-- 删除业务表
DROP TABLE IF EXISTS sys_config;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_dept;

-- 删除文档管理相关表
DROP TABLE IF EXISTS t_document;
DROP TABLE IF EXISTS t_document_file;
DROP TABLE IF EXISTS t_document_tag;
DROP TABLE IF EXISTS t_dict_tag;

-- 重新开启外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 可选：删除数据库（谨慎使用）
-- DROP DATABASE IF EXISTS permission_db;
