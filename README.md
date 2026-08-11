# ATS 招聘管理系统

智能招聘管理平台，支持职位管理、候选人库、简历投递、智能筛选引擎，提供管理端 + 求职者端双端界面。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 2.7.18（适配 JDK 8）|
| ORM | MyBatis-Plus | 3.5.3.1 |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis（Spring Data Redis） | 6+ |
| 模板引擎 | Thymeleaf | 3.0.15 |
| 鉴权 | JWT (auth0 java-jwt) | 4.4.0 |
| API 文档 | Springdoc OpenAPI | 1.7.0 |
| 工具库 | Hutool / Lombok | 5.8.22 / 1.18.30 |
| 容器化 | Docker + docker-compose | - |
| CI/CD | GitHub Actions | - |
| 测试 | JUnit 5 + Mockito + H2 | - |

## 功能模块

### 管理端（需登录）
- **看板 Dashboard**：数据统计 + Chart.js 可视化（职位分布饼图、通过率柱状图）
- **职位管理**：CRUD + 分页 + 关键词搜索
- **候选人库**：CRUD + 分页 + 详情页
- **投递记录**：状态流转（PENDING → REVIEWED → ACCEPTED/REJECTED）+ 筛选结果查看
- **筛选规则**：3 种规则类型（KEYWORD / SKILL / EXPERIENCE）+ 3 种匹配模式（ANY / ALL / MIN）
- **用户管理**：CRUD + 角色区分（ADMIN / HR / VIEWER）+ 密码重置

### 求职者端（匿名访问）
- 职位列表浏览
- 职位详情查看
- 匿名投递简历

### 智能筛选引擎
- 技能匹配（ANY/ALL 模式）
- 关键词匹配（简历文本扫描）
- 经验年限匹配（MIN 模式）
- 加权评分算法 + 通过/不通过判定
- 规则明细展示（匹配项/未匹配项 + 得分）

## 快速开始

### 环境要求
- JDK 1.8
- Maven 3.6+
- MySQL 8.0
- Redis 6+（可选，未启动时自动降级）

### 本地运行

```bash
# 1. 初始化数据库
mysql -u root -p < recruit/src/main/resources/db/schema.sql

# 2. 编译运行
cd recruit
mvn spring-boot:run

# 3. 访问
# 管理端：http://localhost:8080/login  (admin / admin123)
# 求职者端：http://localhost:8080/portal
# API 文档：http://localhost:8080/swagger-ui.html
```

### Docker 部署

```bash
cd recruit
docker-compose up -d
# 自动启动 MySQL + Redis + 应用，首次启动自动建表
```

### 环境变量配置

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| MYSQL_HOST | localhost | MySQL 主机 |
| MYSQL_PORT | 3306 | MySQL 端口 |
| MYSQL_DATABASE | recruit | 数据库名 |
| MYSQL_USERNAME | root | 数据库用户名 |
| MYSQL_PASSWORD | 1234 | 数据库密码 |
| REDIS_HOST | localhost | Redis 主机 |
| REDIS_PORT | 6379 | Redis 端口 |
| JWT_SECRET | recruit-secret-key-... | JWT 签名密钥 |
| JWT_EXPIRE_HOURS | 8 | Token 有效期（小时）|

## 项目结构

```
recruit/
├── src/main/java/com/recruit/
│   ├── config/          # WebMvc、Redis、MyBatisPlus、OpenAPI 配置
│   ├── controller/     # 11 个控制器（管理端 API + 求职者端 API + 页面路由）
│   ├── service/        # 7 个服务（含智能筛选引擎）
│   ├── mapper/         # 6 个 MyBatis-Plus Mapper
│   ├── entity/         # 6 个实体
│   ├── dto/            # 请求/响应 DTO
│   ├── security/       # JWT 拦截器、页面鉴权拦截器
│   └── common/         # 统一响应 R、全局异常处理
├── src/main/resources/
│   ├── application.yml
│   ├── db/schema.sql   # 建表 + 预置数据
│   ├── static/          # 前端公共 JS
│   └── templates/      # 11 个 Thymeleaf 模板
├── src/test/            # 单元测试（29 条）
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## 测试

```bash
cd recruit
mvn test
# 29 条单元测试全绿
```

## API 文档

启动后访问 http://localhost:8080/swagger-ui.html 查看完整 API 文档。

## 预置账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | ADMIN |
| hr | hr123456 | HR |
| viewer | viewer123 | VIEWER |

## License

MIT
