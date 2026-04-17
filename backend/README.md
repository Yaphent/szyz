# 企业级权限管理平台 - 后端

## 技术栈

- **Java 8+**
- **Spring Boot 2.7.18**
- **MyBatis-Plus 3.5.3**
- **MySQL 8.0**
- **JWT (jjwt 0.9.1)**
- **Redis (可选)**
- **Lombok**
- **Hutool 工具库**

## 项目结构

```
src/main/java/com/permission/
├── PermissionPlatformApplication.java  # 主启动类
├── config/                             # 配置类
│   ├── SecurityConfig.java            # 安全配置
│   ├── JwtUtils.java                  # JWT工具类
│   ├── CorsConfig.java                # 跨域配置
│   ├── WebMvcConfig.java              # WebMvc配置
│   └── PermissionInterceptor.java     # 权限拦截器
├── common/                            # 公共类
│   ├── R.java                         # 统一响应
│   └── PageResult.java                # 分页结果
├── entity/                            # 实体类
│   ├── Dept.java                      # 单位
│   ├── User.java                      # 用户
│   ├── Role.java                      # 角色
│   ├── Menu.java                      # 菜单
│   └── *Relation.java                 # 关联表
├── mapper/                            # Mapper接口
├── service/                           # 服务层
└── controller/                        # 控制器层
```

## 快速开始

### 1. 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+ 或 8.0+

### 2. 数据库配置

创建数据库并执行SQL脚本：

```sql
CREATE DATABASE permission_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用 MySQL 客户端执行 backend/sql/init.sql
```

### 3. 修改配置文件

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/permission_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### 4. 启动服务

```bash
# 方式1: 使用Maven启动
mvn spring-boot:run

# 方式2: 打包后启动
mvn clean package
java -jar target/permission-platform-1.0.0.jar

# 方式3: 使用脚本启动
chmod +x start.sh
./start.sh
```

服务启动后访问: http://localhost:8080

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 超级管理员 |
| sys_admin | admin123 | 系统管理员 |
| user01 | admin123 | 普通用户 |

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
| PUT | /api/user/status | 修改状态 |
| POST | /api/user/assign-roles | 分配角色 |
| POST | /api/user/assign-depts | 分配管辖单位 |
| PUT | /api/user/change-password | 修改密码 |
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

## 响应格式

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... }
}
```

错误码：
- 200: 成功
- 400: 参数错误
- 401: 未认证
- 403: 无权限
- 404: 不存在
- 500: 服务器错误

## 注意事项

1. 所有需要认证的接口都需要在请求头中携带 `Authorization: Bearer <token>`
2. 超级管理员拥有所有权限，不受权限控制
3. 用户删除、角色删除等操作会检查关联数据
4. 验证码有效期为5分钟
