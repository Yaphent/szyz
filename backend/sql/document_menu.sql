-- ================================================
-- 法规文档管理 - 菜单 & 权限数据初始化
-- 适用：企业级权限管理平台 smartplatform
-- 依赖：init.sql 已执行（sys_menu / sys_role / sys_role_menu 已创建）
-- 说明：
--   1. 一级菜单使用 menu_id=7（避开已有 2~6、100 系列）
--   2. 按钮/权限使用 7xx 段（702~710）
--   3. 详情页不作为单独菜单，通过按钮权限控制「查看/新增/编辑」
--   4. 可重复执行：已采用 DELETE + INSERT 模式，保证幂等
-- ================================================

-- 关闭外键检查，避免 DELETE 顺序问题
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------
-- 1. 清理本模块旧数据（幂等）
-- ----------------------------------------
DELETE FROM sys_role_menu WHERE menu_id IN (7, 702, 703, 704, 705, 706, 707, 708, 709, 710, 711, 712, 713, 714);
DELETE FROM sys_menu      WHERE menu_id IN (7, 702, 703, 704, 705, 706, 707, 708, 709, 710, 711, 712, 713, 714);

-- ----------------------------------------
-- 2. 新增菜单数据
--    结构说明：
--      menu_id | parent_id | menu_type | name         | path              | component                   | perms                 | url                         | icon
--      7         0           C           法规文档       /document           document/DocumentList         doc:document:list       /api/document/page            Document
--      702       7           F           查看文档       -                   -                             doc:document:view       /api/document/**              -
--      703       7           F           新增文档       -                   -                             doc:document:add        /api/document (POST)          -
--      704       7           F           编辑文档       -                   -                             doc:document:edit       /api/document/** (PUT)        -
--      705       7           F           删除文档       -                   -                             doc:document:delete     /api/document/** (DELETE)     -
--      706       7           F           批量删除       -                   -                             doc:document:batchDelete /api/document/batch (DELETE)  -
--      707       7           F           启用/停用     -                   -                             doc:document:status     /api/document/status (PUT)    -
--      708       7           F           上传文件       -                   -                             doc:document:upload     /api/document/upload          -
--      709       7           F           解析文件       -                   -                             doc:document:parse      /api/document/parse           -
--      710       7           F           管理标签       -                   -                             doc:document:tag        /api/document/dict/tags       -
-- ----------------------------------------
INSERT INTO sys_menu (menu_id, parent_id, menu_type, name, path, component, perms, url, icon, sort, is_frame, status) VALUES
-- 一级菜单：法规文档
(7,   0, 'C', '法规文档', '/document', 'document/DocumentList', 'doc:document:list', '/api/document/page', 'Document', 7, 0, 1),

-- 按钮权限（属于 menu_id=7 下，不在左侧菜单渲染，仅做权限拦截与前端按钮控制）
(702, 7, 'F', '查看文档',     NULL, NULL, 'doc:document:view',        '/api/document/*',                '', 1,  0, 1),
(703, 7, 'F', '新增文档',     NULL, NULL, 'doc:document:add',         '/api/document',                  '', 2,  0, 1),
(704, 7, 'F', '编辑文档',     NULL, NULL, 'doc:document:edit',        '/api/document/*',                '', 3,  0, 1),
(705, 7, 'F', '删除文档',     NULL, NULL, 'doc:document:delete',      '/api/document/*',                '', 4,  0, 1),
(706, 7, 'F', '批量删除',     NULL, NULL, 'doc:document:batchDelete', '/api/document/batch',            '', 5,  0, 1),
(707, 7, 'F', '启用/停用',    NULL, NULL, 'doc:document:status',      '/api/document/status',           '', 6,  0, 1),
(708, 7, 'F', '上传文件',     NULL, NULL, 'doc:document:upload',      '/api/document/upload',           '', 7,  0, 1),
(709, 7, 'F', '解析文件',     NULL, NULL, 'doc:document:parse',       '/api/document/parse',            '', 8,  0, 1),
(710, 7, 'F', '智能解析',     NULL, NULL, 'doc:document:smartParse',  '/api/document/smart-parse',      '', 9,  0, 1),
(711, 7, 'F', '向量化',       NULL, NULL, 'doc:document:vectorize',   '/api/document/vectorize/*',      '', 10, 0, 1),
(712, 7, 'F', '删除向量化',   NULL, NULL, 'doc:document:deleteVectorize', '/api/document/vectorize/*',    '', 11, 0, 1),
(713, 7, 'F', '管理标签',     NULL, NULL, 'doc:document:tag',         '/api/document/dict/tags',        '', 12, 0, 1),
(714, 7, 'F', 'Dify状态',     NULL, NULL, 'doc:document:difyStatus',  '/api/document/dify-status/*',    '', 13, 0, 1);

-- ----------------------------------------
-- 3. 角色 - 菜单 关联初始化
-- ----------------------------------------

-- 3.1 超级管理员（role_id=1）：拥有全部新菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 7),
(1, 702), (1, 703), (1, 704), (1, 705),
(1, 706), (1, 707), (1, 708), (1, 709), (1, 710), (1, 711), (1, 712), (1, 713), (1, 714);

-- 3.2 系统管理员（role_id=2）：拥有全部新菜单（业务模块默认由系统管理员维护）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(2, 7),
(2, 702), (2, 703), (2, 704), (2, 705),
(2, 706), (2, 707), (2, 708), (2, 709), (2, 710), (2, 711), (2, 712), (2, 713), (2, 714);

-- 3.3 普通用户（role_id=3）：只有查看与下载权限（无新增/编辑/删除）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(3, 7),
(3, 702), (3, 714);

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- ================================================
-- 执行完毕后，登录使用超级管理员账号即可在左侧菜单看到「法规文档」
-- ================================================
