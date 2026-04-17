# AGENTS.md - 企业级权限管理平台

## 项目概述

这是一个基于 Spring Boot + Vue 3 的企业级权限管理系统，实现了完整的 RBAC（Role-Based Access Control）权限控制模型。

## 技术栈

### 后端 (Java)
- Java 8+
- Spring Boot 2.7.18
- MyBatis-Plus 3.5.3
- MySQL 8.0
- JWT (jjwt 0.9.1)
- Redis (可选)

### 前端 (Vue 3)
- Vue 3.4+
- TypeScript 5
- Element Plus 2.5
- Pinia 2
- Vue Router 4
- Vite 7
- Tailwind CSS 4

## 目录结构

```
backend/                          # Java Spring Boot 后端
├── src/main/java/com/permission/
│   ├── PermissionPlatformApplication.java  # 主启动类
│   ├── config/                  # 配置类
│   │   ├── SecurityConfig.java     # 安全配置
│   │   ├── JwtUtils.java           # JWT工具
│   │   ├── CorsConfig.java         # 跨域配置
│   │   ├── WebMvcConfig.java       # WebMvc配置
│   │   ├── PermissionInterceptor.java  # 权限拦截器
│   │   └── MyMetaObjectHandler.java # 自动填充
│   ├── common/                  # 公共类
│   │   ├── R.java                 # 统一响应
│   │   └── PageResult.java        # 分页结果
│   ├── entity/                   # 实体类
│   │   ├── Dept.java              # 单位
│   │   ├── User.java              # 用户
│   │   ├── Role.java              # 角色
│   │   ├── Menu.java              # 菜单
│   │   └── *Relation.java         # 关联表
│   ├── mapper/                   # Mapper接口
│   ├── service/                  # 服务层
│   └── controller/               # 控制器
├── src/main/resources/
│   └── application.yml           # 配置文件
└── sql/
    └── init.sql                  # 数据库脚本

src/                             # Vue 3 前端
├── api/                         # API接口
│   ├── request.ts               # Axios封装
│   └── index.ts                 # 接口定义
├── store/                       # 状态管理
│   └── user.ts                  # 用户Store
├── router/                      # 路由配置
│   └── index.ts                 # 路由定义
└── views/                       # 页面组件
    ├── login/                   # 登录页
    ├── layout/                  # 布局组件
    ├── dashboard/               # 仪表盘
    └── system/                  # 系统管理
        ├── user/                # 用户管理
        ├── role/                # 角色管理
        ├── menu/                # 菜单管理
        └── dept/                # 单位管理
```

## 开发命令

### 后端
```bash
cd backend
# 创建数据库
mysql -u root -p < sql/init.sql
# 启动
mvn spring-boot:run
```

### 前端
```bash
pnpm install    # 安装依赖
pnpm dev        # 开发模式
pnpm build      # 生产构建
```

## 环境变量

### 前端 (.env)
```
VITE_API_URL=http://localhost:8080/api
```

### 后端 (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/permission_db
    username: root
    password: root123
jwt:
  secret: your-secret-key
  expiration: 86400000  # 24小时
```

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 超级管理员 |
| sys_admin | admin123 | 系统管理员 |
| user01 | admin123 | 普通用户 |

## 核心功能

1. **用户管理** - CRUD、状态切换、角色分配、管辖单位分配
2. **角色管理** - CRUD、菜单权限分配
3. **菜单管理** - 树形结构、目录/菜单/按钮/外链
4. **单位管理** - 树形结构、CRUD
5. **数据权限** - 基于管辖单位的数据过滤
6. **动态路由** - 根据用户权限动态生成菜单
7. **JWT认证** - 图形验证码、Token刷新

## API规范

### 响应格式
```json
{
  "code": 200,
  "msg": "success",
  "data": { ... }
}
```

### 错误码
- 200: 成功
- 400: 参数错误
- 401: 未认证
- 403: 无权限
- 404: 不存在
- 500: 服务器错误

## 安全注意事项

1. 所有需要认证的接口都需要在请求头中携带 `Authorization: Bearer <token>`
2. 超级管理员拥有所有权限，不受权限控制
3. 验证码有效期为5分钟
4. Token有效期为24小时
