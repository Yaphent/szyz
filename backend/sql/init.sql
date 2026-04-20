-- ================================================
-- 企业级权限管理平台 - 数据库初始化脚本
-- 数据库: MySQL 5.7+ / 8.0+
-- ================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS permission_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE permission_db;

-- ----------------------------------------
-- 1. 单位管理表 (sys_dept)
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS sys_dept (
                                        dept_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        parent_id BIGINT DEFAULT 0 COMMENT '父级ID，0为根',
                                        dept_name VARCHAR(100) NOT NULL COMMENT '单位名称',
    dept_code VARCHAR(50) COMMENT '单位编码',
    leader VARCHAR(50) COMMENT '负责人',
    phone VARCHAR(20) COMMENT '联系电话',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单位表';

-- ----------------------------------------
-- 2. 用户表 (sys_user)
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
                                        user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    nickname VARCHAR(50) COMMENT '昵称',
    password VARCHAR(255) NOT NULL COMMENT '密码(Bcrypt加密)',
    email VARCHAR(100) COMMENT '邮箱',
    mobile VARCHAR(20) COMMENT '手机号',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    dept_id BIGINT COMMENT '主归属单位ID',
    last_login_time DATETIME COMMENT '最后登录时间',
    avatar VARCHAR(255) COMMENT '头像URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (dept_id) REFERENCES sys_dept(dept_id) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------------------
-- 3. 角色表 (sys_role)
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS sys_role (
                                        role_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------------------
-- 4. 菜单表 (sys_menu)
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS sys_menu (
                                        menu_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        parent_id BIGINT DEFAULT 0 COMMENT '父级ID，0为根',
                                        menu_type VARCHAR(1) NOT NULL COMMENT '菜单类型：M目录，C菜单，F按钮，L外链',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path VARCHAR(255) COMMENT '路由路径或外链URL',
    component VARCHAR(255) COMMENT '组件路径',
    perms VARCHAR(100) COMMENT '权限标识',
    url VARCHAR(255) COMMENT '后端接口路径，用于权限拦截',
    icon VARCHAR(50) COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    is_frame TINYINT DEFAULT 0 COMMENT '是否外链：0否，1是',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- ----------------------------------------
-- 5. 用户-角色关联表 (sys_user_role)
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS sys_user_role (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                             user_id BIGINT NOT NULL,
                                             role_id BIGINT NOT NULL,
                                             create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                             FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES sys_role(role_id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_role (user_id, role_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ----------------------------------------
-- 6. 角色-菜单关联表 (sys_role_menu)
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS sys_role_menu (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                             role_id BIGINT NOT NULL,
                                             menu_id BIGINT NOT NULL,
                                             create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                             FOREIGN KEY (role_id) REFERENCES sys_role(role_id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES sys_menu(menu_id) ON DELETE CASCADE,
    UNIQUE KEY uk_role_menu (role_id, menu_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ----------------------------------------
-- 7. 用户-管辖单位关联表 (sys_user_dept)
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS sys_user_dept (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                             user_id BIGINT NOT NULL,
                                             dept_id BIGINT NOT NULL,
                                             create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                                             FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (dept_id) REFERENCES sys_dept(dept_id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_dept (user_id, dept_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户管辖单位关联表';

-- ----------------------------------------
-- 8. 索引优化
-- ----------------------------------------
CREATE INDEX idx_dept_parent ON sys_dept(parent_id);
CREATE INDEX idx_dept_status ON sys_dept(status);
CREATE INDEX idx_user_username ON sys_user(username);
CREATE INDEX idx_user_status ON sys_user(status);
CREATE INDEX idx_user_dept ON sys_user(dept_id);
CREATE INDEX idx_role_code ON sys_role(role_code);
CREATE INDEX idx_menu_parent ON sys_menu(parent_id);
CREATE INDEX idx_menu_type ON sys_menu(menu_type);
CREATE INDEX idx_menu_url ON sys_menu(url);

-- ================================================
-- 初始化数据
-- ================================================

-- ----------------------------------------
-- 1. 初始化单位数据
-- ----------------------------------------
INSERT INTO sys_dept (dept_id, parent_id, dept_name, dept_code, leader, phone, sort, status) VALUES
                                                                                                 (1, 0, '总公司', 'HQ', '系统管理员', '400-888-8888', 0, 1),
                                                                                                 (2, 1, '技术研发部', 'DEV', '张技术', '400-888-8001', 1, 1),
                                                                                                 (3, 1, '市场营销部', 'MKT', '李市场', '400-888-8002', 2, 1),
                                                                                                 (4, 1, '人力资源部', 'HR', '王人力', '400-888-8003', 3, 1),
                                                                                                 (5, 2, '前端开发组', 'DEV-FE', '前端主管', '400-888-8011', 1, 1),
                                                                                                 (6, 2, '后端开发组', 'DEV-BE', '后端主管', '400-888-8012', 2, 1),
                                                                                                 (7, 3, '品牌推广组', 'MKT-BRAND', '推广主管', '400-888-8021', 1, 1),
                                                                                                 (8, 3, '数字营销组', 'MKT-DIGITAL', '营销主管', '400-888-8022', 2, 1);

-- ----------------------------------------
-- 2. 初始化菜单数据
-- ----------------------------------------
INSERT INTO sys_menu (menu_id, parent_id, menu_type, name, path, component, perms, url, icon, sort, is_frame, status) VALUES
-- 一级菜单（去掉系统管理目录）
(2, 0, 'C', '用户管理', '/system/user', 'system/user/index', 'sys:user:list', '/api/user/page', 'User', 2, 0, 1),
(3, 0, 'C', '角色管理', '/system/role', 'system/role/index', 'sys:role:list', '/api/role/page', 'Wallet', 3, 0, 1),
(4, 0, 'C', '菜单管理', '/system/menu', 'system/menu/index', 'sys:menu:list', '/api/menu/tree', 'Menu', 4, 0, 1),
(5, 0, 'C', '单位管理', '/system/dept', 'system/dept/index', 'sys:dept:list', '/api/dept/tree', 'OfficeBuilding', 5, 0, 1),
(6, 0, 'C', '参数管理', '/system/config', 'system/config/index', 'sys:config:list', '/api/config/page', 'Tools', 6, 0, 1),
(100, 0, 'C', '仪表盘', '/dashboard', 'dashboard/index', 'sys:dashboard:view', '/api/dashboard/statistics', 'Odometer', 0, 0, 1),
-- 用户管理按钮
(202, 2, 'F', '新增用户', NULL, NULL, 'sys:user:add', NULL, '', 1, 0, 1),
(203, 2, 'F', '编辑用户', NULL, NULL, 'sys:user:edit', NULL, '', 2, 0, 1),
(204, 2, 'F', '删除用户', NULL, NULL, 'sys:user:delete', NULL, '', 3, 0, 1),
(205, 2, 'F', '分配角色', NULL, NULL, 'sys:user:assign', NULL, '', 4, 0, 1),
(206, 2, 'F', '分配单位', NULL, NULL, 'sys:user:assignDept', NULL, '', 5, 0, 1),
(207, 2, 'F', '重置密码', NULL, NULL, 'sys:user:resetPwd', NULL, '', 6, 0, 1),
-- 角色管理按钮
(302, 3, 'F', '新增角色', NULL, NULL, 'sys:role:add', NULL, '', 1, 0, 1),
(303, 3, 'F', '编辑角色', NULL, NULL, 'sys:role:edit', NULL, '', 2, 0, 1),
(304, 3, 'F', '删除角色', NULL, NULL, 'sys:role:delete', NULL, '', 3, 0, 1),
(305, 3, 'F', '分配菜单', NULL, NULL, 'sys:role:assignMenu', NULL, '', 4, 0, 1),
-- 菜单管理按钮
(402, 4, 'F', '新增菜单', NULL, NULL, 'sys:menu:add', NULL, '', 1, 0, 1),
(403, 4, 'F', '编辑菜单', NULL, NULL, 'sys:menu:edit', NULL, '', 2, 0, 1),
(404, 4, 'F', '删除菜单', NULL, NULL, 'sys:menu:delete', NULL, '', 3, 0, 1),
-- 单位管理按钮
(502, 5, 'F', '新增单位', NULL, NULL, 'sys:dept:add', NULL, '', 1, 0, 1),
(503, 5, 'F', '编辑单位', NULL, NULL, 'sys:dept:edit', NULL, '', 2, 0, 1),
(504, 5, 'F', '删除单位', NULL, NULL, 'sys:dept:delete', NULL, '', 3, 0, 1),
-- 参数管理按钮
(602, 6, 'F', '新增参数', NULL, NULL, 'sys:config:add', NULL, '', 1, 0, 1),
(603, 6, 'F', '编辑参数', NULL, NULL, 'sys:config:edit', NULL, '', 2, 0, 1),
(604, 6, 'F', '删除参数', NULL, NULL, 'sys:config:delete', NULL, '', 3, 0, 1);

-- ----------------------------------------
-- 3. 初始化角色数据
-- ----------------------------------------
INSERT INTO sys_role (role_id, role_name, role_code, status, remark) VALUES
                                                                         (1, '超级管理员', 'super_admin', 1, '拥有所有权限'),
                                                                         (2, '系统管理员', 'sys_admin', 1, '系统管理权限'),
                                                                         (3, '普通用户', 'user', 1, '普通用户权限');

-- ----------------------------------------
-- 4. 初始化用户数据
-- (密码均为: admin123, BCrypt加密)
-- ----------------------------------------
INSERT INTO sys_user (user_id, username, nickname, password, email, mobile, status, dept_id) VALUES
                                                                                                 (1, 'admin', '超级管理员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@example.com', '13800138000', 1, 1),
                                                                                                 (2, 'sys_admin', '系统管理员', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'sys@example.com', '13800138001', 1, 2),
                                                                                                 (3, 'user01', '测试用户', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'user@example.com', '13800138002', 1, 5);

-- ----------------------------------------
-- 5. 初始化用户-角色关联
-- ----------------------------------------
INSERT INTO sys_user_role (user_id, role_id) VALUES
                                                 (1, 1),  -- admin -> 超级管理员
                                                 (2, 2),  -- sys_admin -> 系统管理员
                                                 (3, 3);  -- user01 -> 普通用户

-- ----------------------------------------
-- 6. 初始化角色-菜单关联
-- ----------------------------------------
-- 超级管理员拥有所有菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu;

-- 系统管理员拥有部分菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
                                                 (2, 100), (2, 2), (2, 202), (2, 3), (2, 302), (2, 6), (2, 602);

-- 普通用户只有基础权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
    (3, 100);

-- ----------------------------------------
-- 7. 初始化用户-管辖单位关联
-- ----------------------------------------
INSERT INTO sys_user_dept (user_id, dept_id) VALUES
                                                 (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),  -- 超级管理员管理所有单位
                                                 (2, 2), (2, 5), (2, 6),  -- 系统管理员管理技术研发部及子部门
                                                 (3, 5);  -- 普通用户只管理前端开发组

-- ----------------------------------------
-- 8. 系统参数表 (sys_config)
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS sys_config (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_name VARCHAR(50) NOT NULL COMMENT '参数名称',
    config_key VARCHAR(100) NOT NULL COMMENT '参数键名',
    config_value VARCHAR(500) NOT NULL COMMENT '参数值',
    config_type CHAR(1) DEFAULT 'S' COMMENT '类型：S字符串，N数值，B布尔',
    remark VARCHAR(200) COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数表';

CREATE INDEX idx_config_key ON sys_config(config_key);
CREATE INDEX idx_config_type ON sys_config(config_type);
CREATE INDEX idx_config_status ON sys_config(status);

-- ----------------------------------------
-- 9. 初始化系统参数数据
-- ----------------------------------------
INSERT INTO sys_config (config_name, config_key, config_value, config_type, remark, status) VALUES
('系统名称', 'sys.system.name', '安阳智能监督平台', 'S', '系统显示名称', 1),
('系统Logo', 'sys.logo.url', '/logo.png', 'S', '系统Logo图片URL', 1),
('登录验证码', 'sys.login.captcha', 'true', 'B', '是否启用登录验证码', 1),
('单点登录', 'sys.login.sso', 'false', 'B', '是否启用单点登录', 1),
('Token有效期', 'sys.token.expire', '24', 'N', 'JWT Token有效期(小时)', 1),
('密码最小长度', 'sys.password.minLength', '6', 'N', '用户密码最小长度', 1),
('文件上传大小', 'sys.upload.maxSize', '10', 'N', '单文件上传大小限制(MB)', 1),
('允许文件类型', 'sys.upload.allowedTypes', 'jpg,jpeg,png,pdf,doc,docx,xls,xlsx', 'S', '允许上传的文件类型', 1),
('系统公告', 'sys.announcement', '欢迎使用安阳智能监督平台！', 'S', '系统公告内容', 1);
