# 企业级权限管理平台

基于 Spring Boot + Vue 3 的企业级权限管理系统，实现了完整的 RBAC 权限控制模型。

## 项目结构

```
.
├── backend/                    # Java Spring Boot 后端
│   ├── src/main/java/com/permission/
│   │   ├── config/           # 配置类
│   │   ├── common/           # 公共类
│   │   ├── entity/           # 实体类
│   │   ├── mapper/           # MyBatis Mapper
│   │   ├── service/          # 服务层
│   │   └── controller/       # 控制器
│   ├── src/main/resources/   # 配置文件
│   └── sql/                  # 数据库脚本
├── frontend/                   # Vue 3 前端
│   ├── src/                  # 源代码
│   │   ├── api/              # API 接口
│   │   ├── store/            # 状态管理
│   │   ├── router/           # 路由配置
│   │   └── views/            # 页面组件
│   ├── server/               # Express 服务器
│   │   ├── routes/           # 路由定义
│   │   ├── server.ts         # 服务器入口
│   │   └── vite.ts           # Vite 集成
│   ├── scripts/              # 脚本文件
│   │   ├── dev.sh            # 开发启动脚本
│   │   ├── start.sh          # 生产启动脚本
│   │   └── ...               # 其他脚本
│   ├── index.html            # 入口HTML文件
│   ├── package.json          # 依赖配置
│   └── vite.config.ts        # 构建配置
└── README.md
```

## 技术栈

### 后端
- **Java 8+**
- **Spring Boot 2.7.18**
- **MyBatis-Plus 3.5.3**
- **MySQL 8.0**
- **JWT (jjwt 0.9.1)**
- **Redis (可选)**
- **Lombok**

### 前端
- **Vue 3.4+**
- **TypeScript 5**
- **Element Plus 2.5**
- **Pinia 2**
- **Vue Router 4**
- **Axios**
- **Tailwind CSS 4**
- **Vite 7**

## 功能特性

### 核心功能
- 用户管理（CRUD、状态切换、角色分配、管辖单位分配）
- 角色管理（CRUD、菜单权限分配）
- 菜单管理（树形结构、目录/菜单/按钮/外链）
- 单位管理（树形结构、CRUD）
- 数据权限（基于管辖单位的数据过滤）
- 动态路由（根据用户权限动态生成菜单）
- JWT认证（图形验证码、Token刷新）

### 安全特性
- JWT Token 认证
- 动态菜单权限控制
- 接口级别权限校验
- 按钮级权限指令 `v-perms`

## 快速开始

### 1. 后端启动

```bash
# 进入后端目录
cd backend

# 创建数据库
mysql -u root -p
CREATE DATABASE permission_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;

# 执行SQL脚本
mysql -u root -p permission_db < sql/init.sql

# 修改配置文件（可选）
# 编辑 src/main/resources/application.yml 修改数据库连接

# 启动后端
mvn spring-boot:run
# 或打包后运行
mvn clean package
java -jar target/permission-platform-1.0.0.jar
```

后端启动后访问: http://localhost:8080

### 2. 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
pnpm install

# 开发模式
pnpm dev

# 生产构建
pnpm build
```

前端启动后访问: http://localhost:5000

### 3. 配置代理

前端开发环境已配置代理，将 `/api` 请求转发到 `http://localhost:8080`。

或修改 `.env` 文件：
```
VITE_API_URL=http://localhost:8080/api
```

## 默认账号

| 用户名 | 密码 | 角色 | 描述 |
|--------|------|------|------|
| admin | admin123 | 超级管理员 | 拥有所有权限 |
| sys_admin | admin123 | 系统管理员 | 系统管理权限 |
| user01 | admin123 | 普通用户 | 基础权限 |

## API 接口

### 认证接口

| 方法 | URL | 说明 |
|------|-----|------|
| GET | /api/captcha | 获取验证码 |
| POST | /api/auth/login | 用户登录 |
| GET | /api/auth/info | 获取用户信息 |
| POST | /api/auth/logout | 登出 |

### 单位管理

| 方法 | URL | 说明 |
|------|-----|------|
| GET | /api/dept/tree | 获取单位树 |
| GET | /api/dept/{id} | 获取单位详情 |
| POST | /api/dept | 新增单位 |
| PUT | /api/dept | 修改单位 |
| DELETE | /api/dept/{id} | 删除单位 |

### 用户管理

| 方法 | URL | 说明 |
|------|-----|------|
| GET | /api/user/page | 分页查询 |
| GET | /api/user/{id} | 用户详情 |
| POST | /api/user | 新增用户 |
| PUT | /api/user | 修改用户 |
| DELETE | /api/user/{id} | 删除用户 |
| POST | /api/user/assign-roles | 分配角色 |
| POST | /api/user/assign-depts | 分配管辖单位 |
| PUT | /api/user/reset-password/{id} | 重置密码 |

### 角色管理

| 方法 | URL | 说明 |
|------|-----|------|
| GET | /api/role/page | 分页查询 |
| GET | /api/role/all | 全部角色 |
| GET | /api/role/{id} | 角色详情 |
| POST | /api/role | 新增角色 |
| PUT | /api/role | 修改角色 |
| DELETE | /api/role/{id} | 删除角色 |
| POST | /api/role/assign-menus | 分配菜单 |

### 菜单管理

| 方法 | URL | 说明 |
|------|-----|------|
| GET | /api/menu/tree | 菜单树 |
| GET | /api/menu/route | 用户路由 |
| GET | /api/menu/{id} | 菜单详情 |
| POST | /api/menu | 新增菜单 |
| PUT | /api/menu | 修改菜单 |
| DELETE | /api/menu/{id} | 删除菜单 |

### 仪表盘

| 方法 | URL | 说明 |
|------|-----|------|
| GET | /api/dashboard/statistics | 统计数据 |

## 数据库设计

### 表结构

- `sys_dept` - 单位表
- `sys_user` - 用户表
- `sys_role` - 角色表
- `sys_menu` - 菜单表
- `sys_user_role` - 用户角色关联表
- `sys_role_menu` - 角色菜单关联表
- `sys_user_dept` - 用户管辖单位关联表

### 菜单类型

| 类型 | 说明 |
|------|------|
| M | 目录 |
| C | 菜单 |
| F | 按钮（权限点） |
| L | 外链 |

## 前端权限控制

### v-perms 指令

```vue
<!-- 单一权限 -->
<el-button v-perms="['sys:user:add']">新增</el-button>

<!-- 多个权限（满足其一） -->
<el-button v-perms="['sys:user:edit', 'sys:user:delete']">操作</el-button>
```

### 路由守卫

路由会根据用户权限自动过滤无权限页面。

## 项目预览

前端页面已部署到 http://${COZE_PROJECT_DOMAIN_DEFAULT}

## License

MIT
