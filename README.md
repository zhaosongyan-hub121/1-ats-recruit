# ATS 智能招聘管理系统

> 一套基于 Spring Boot + Vue.js 构建的企业级智能招聘平台，支持多角色权限体系、智能简历筛选引擎、全闭环招聘业务流程。

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [功能模块](#功能模块)
- [角色权限体系](#角色权限体系)
- [数据库设计](#数据库设计)
- [核心亮点](#核心亮点)
- [快速开始](#快速开始)
- [演示账号](#演示账号)
- [项目结构](#项目结构)

---

## 项目概述

ATS（Applicant Tracking System）智能招聘管理系统，面向企业HR和求职者的双向招聘平台。系统实现了从企业入驻、岗位发布、简历投递、智能筛选到人工审核的完整招聘业务闭环，具备多角色权限隔离、智能匹配算法、响应式UI设计等特性。

### 业务流程

```
HR注册 → 填写企业信息 → 管理员审核 → 登录HR工作台
    ↓
发布岗位 → 配置筛选规则 → 岗位自动展示在求职者首页
    ↓
求职者浏览岗位 → 多条件筛选 → 查看详情 → 匿名投递简历
    ↓
系统智能打分筛选 → HR查看投递记录 → 人工审核 → 完成招聘初筛
```

---

## 技术栈

| 分类 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 后端框架 | Spring Boot | 2.7.18 | 兼容JDK8最终稳定版 |
| 数据库 | MySQL | 8.0 | 生产环境关系型数据库 |
| 缓存 | Redis | 6.0+ | 可选，支持自动降级 |
| ORM | MyBatis-Plus | 3.5.3.1 | 增强型MyBatis框架 |
| 鉴权 | JWT | 4.4.0 | 无状态Token认证 |
| 密码加密 | Hutool BCrypt | 5.8.22 | 安全密码哈希 |
| API文档 | SpringDoc | 1.7.0 | OpenAPI 3.0文档生成 |
| 模板引擎 | Thymeleaf | 3.0 | 服务端渲染 |
| 前端UI | 原生HTML/CSS/JS | - | 参考牛客网风格设计 |
| 单元测试 | JUnit 5 + Mockito | - | 全覆盖测试 |
| 内存数据库 | H2 | 2.1 | 测试环境 |

---

## 系统架构

```
┌─────────────────────────────────────────────────────┐
│                    前端层 (Thymeleaf)                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │ 求职者门户 │  │  HR工作台  │  │  管理员后台Dashboard │  │
│  └──────────┘  └──────────┘  └──────────────────┘  │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                  控制层 (Controller)                   │
│  AuthController | PositionController | Application  │
│  CompanyController | PortalApiController | ScreenRule │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                  业务层 (Service)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │ ScreeningEngine│  │ UserService  │  │ Position   │ │
│  │  智能筛选引擎  │  │  用户认证服务  │  │ 岗位管理    │ │
│  └──────────────┘  └──────────────┘  └────────────┘ │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                 数据层 (MyBatis-Plus)                  │
│  UserMapper | PositionMapper | ApplicationMapper    │
│  CompanyMapper | ScreenRuleMapper | ScreenResult     │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│              持久层 (MySQL / H2)                      │
└─────────────────────────────────────────────────────┘
```

---

## 功能模块

### 1. 求职者门户（/portal）
- **岗位浏览**：首页自动加载全部在招岗位，支持社招/校招/实习分类
- **多条件筛选**：薪资区间、工作地点、经验要求、学历、关键词组合查询
- **岗位详情**：查看岗位描述、任职要求、企业信息
- **简历投递**：匿名投递简历，支持求职信附件
- **个人中心**：查看投递记录、收藏岗位、简历管理

### 2. HR工作台（/dashboard）
- **我的岗位**：管理本企业发布的岗位，支持新增/编辑/上下架
- **简历投递**：查看本企业岗位的投递记录，支持状态流转审核
- **筛选规则**：为岗位配置智能筛选规则（技能/关键词/经验）
- **企业信息**：查看企业资料（仅管理员可编辑）
- **数据看板**：岗位投递量、通过率、岗位热度统计

### 3. 管理员后台
- **用户管理**：管理所有用户账号，重置密码、角色分配
- **企业管理**：审核企业入驻信息，管理企业资料
- **岗位管理**：查看全平台岗位，下架违规岗位
- **投递审核**：查看所有投递记录，处理异常数据
- **筛选规则**：管理全平台通用筛选规则

### 4. 智能筛选引擎
- **技能匹配**：基于候选人技能标签与岗位要求匹配
- **关键词匹配**：在简历全文中搜索指定关键词
- **经验匹配**：校验工作年限是否达标
- **评分算法**：权重累加评分，50%匹配率判定通过
- **实时展示**：筛选明细可视化展示匹配/未匹配项

---

## 角色权限体系

| 功能 | 访客 | 求职者(CANDIDATE) | HR | 管理员(ADMIN) |
|------|------|-------------------|-----|-------------|
| 浏览岗位 | ✅ | ✅ | ✅ | ✅ |
| 投递简历 | ✅ | ✅ | ❌ | ❌ |
| 发布岗位 | ❌ | ❌ | ✅(本企业) | ✅ |
| 审核投递 | ❌ | ❌ | ✅(本企业) | ✅ |
| 配置规则 | ❌ | ❌ | ✅(本岗位) | ✅ |
| 管理用户 | ❌ | ❌ | ❌ | ✅ |
| 管理企业 | ❌ | ❌ | ❌ | ✅ |
| 修改企业信息 | ❌ | ❌ | ❌ | ✅ |

### 安全机制
- **JWT无状态鉴权**：Token有效期24小时，支持角色和公司信息编码
- **角色一致性校验**：登录时校验前端选择角色与数据库角色匹配
- **数据隔离**：HR仅能查看本企业数据，通过companyId过滤
- **CSRF防护**：基于SameSite Cookie策略
- **密码加密**：BCrypt加盐哈希存储

---

## 数据库设计

### 核心表结构

```
sys_user (用户表)
├── id, username, password, role (ADMIN/HR/CANDIDATE)
├── company_id (关联企业)
├── email, phone, avatar

company (企业表)
├── id, name, logo, logo_color, industry
├── description, location, website, size

job_position (岗位表)
├── id, title, company_name, company_id, company_logo
├── department, description, requirements
├── status (OPEN/CLOSED), category (SOCIAL/CAMPUS/INTERN)
├── location, salary, education, experience
├── publish_user_id

candidate (候选人表)
├── id, user_id, name, email, phone
├── skills, experience_years, resume_text
├── education_level, school

application (投递记录表)
├── id, candidate_id, user_id, position_id
├── status (SUBMITTED→SCREENING_PASS→INTERVIEWING→OFFER→ACCEPTED)
├── current_round, cover_letter, hr_remark, status_log

screen_rule (筛选规则表)
├── id, name, rule_type (KEYWORD/SKILL/EXPERIENCE)
├── target_field, expected_values, match_mode (ANY/ALL/MIN)
├── weight, position_id

screen_result (筛选结果表)
├── id, application_id, total_score, max_score
├── pass (0/1), rule_details (JSON)
```

### 实体关系

```
User ──N:1── Company (HR绑定企业)
  │
  ├── 1:N ── Position (企业发布岗位)
  │              │
  │              └── 1:N ── Application (岗位收到投递)
  │                              │
  │                              └── 1:1 ── ScreenResult (筛选结果)
  │
  └── 1:1 ── Candidate (求职者档案)
               │
               └── 1:N ── Application (求职者投递记录)

Position ──1:N── ScreenRule (岗位配置筛选规则)
```

---

## 核心亮点

### 🔥 智能简历筛选引擎
- 支持技能标签、关键词、工作年限三种规则类型
- ANY/ALL/MIN三种匹配模式，灵活适配不同筛选需求
- 权重累加评分算法，50%匹配率自动判定通过
- 筛选明细JSON化存储，前端可视化展示

### 🔐 完整多角色权限体系
- 四级角色（访客/求职者/HR/管理员）权限隔离
- 登录角色一致性校验，防止跨角色越权
- HR数据按companyId隔离，确保企业数据私有
- JWT Token编码角色和公司信息，无状态鉴权

### 🏢 企业-岗位-HR层级管理
- 一个企业可绑定多个HR账号
- 岗位自动关联发布者所属企业
- HR仅能管理本企业岗位和投递
- 管理员统一管理所有企业和用户

### 🎨 人性化交互设计
- 参考牛客网风格的现代化UI
- 首页自动加载岗位，无需手动刷新
- 多条件组合筛选，支持薪资/地点/经验/学历
- 全局加载动画和操作反馈提示
- 响应式布局，适配多端访问

### ⚡ 工程化亮点
- Redis缓存自动降级（无Redis直接查库）
- H2内存数据库测试支持
- 39个单元测试全覆盖核心业务
- SpringDoc OpenAPI自动生成接口文档
- 配置外部化，敏感信息使用环境变量

---

## 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+（可选，测试用H2）
- Redis 6.0+（可选，无缓存自动降级）

### 1. 克隆项目

```bash
git clone https://github.com/zhaosongyan-hub121/1-ats-recruit.git
cd 1-ats-recruit
```

### 2. 配置数据库

修改 `recruit/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ats_recruit?useUnicode=true&characterEncoding=utf8mb4
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}
```

### 3. 启动应用

```bash
# 开发模式（使用H2内存数据库）
./mvnw.cmd -f recruit/pom.xml spring-boot:run

# 生产模式
./mvnw.cmd -f recruit/pom.xml spring-boot:run -Dspring-boot.run.profiles=prod
```

应用启动后访问：http://localhost:8080

### 4. 运行测试

```bash
./mvnw.cmd -f recruit/pom.xml test
```

### 5. 打包部署

```bash
./mvnw.cmd -f recruit/pom.xml clean package
java -jar recruit/target/recruit-0.0.1-SNAPSHOT.jar
```

---

## 演示账号

系统启动后自动初始化以下演示账号：

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 超级管理员，全平台权限 |
| HR(腾讯) | hr_tencent | hr123 | 腾讯HR账号 |
| HR(阿里) | hr_alibaba | hr123 | 阿里巴巴HR账号 |
| HR(字节) | hr_bytedance | hr123 | 字节跳动HR账号 |
| 求职者 | candidate01 | candidate123 | 普通求职者 |
| 求职者 | candidate02 | candidate123 | 求职者（有简历） |

### 示例数据
- 6家企业（腾讯、阿里、字节、美团、京东、百度）
- 30+招聘岗位（覆盖Java、前端、产品、运营等方向）
- 10+候选人档案（含技能标签和简历文本）
- 50+投递记录（不同状态流转）
- 多条智能筛选规则（技能匹配+关键词匹配+经验匹配）

---

## 项目结构

```
recruit/
├── src/main/java/com/recruit/
│   ├── controller/          # REST API控制层
│   │   ├── AuthController.java        # 登录注册
│   │   ├── PositionController.java    # 岗位管理
│   │   ├── ApplicationController.java # 投递管理
│   │   ├── CompanyController.java     # 企业管理
│   │   ├── PortalApiController.java   # 求职者门户API
│   │   └── ScreenRuleController.java  # 筛选规则
│   ├── service/             # 业务逻辑层
│   │   ├── ScreeningEngineService.java # 智能筛选引擎
│   │   ├── UserService.java           # 用户服务
│   │   ├── PositionService.java       # 岗位服务
│   │   └── ApplicationService.java    # 投递服务
│   ├── entity/             # 数据库实体
│   │   ├── User.java                  # 用户
│   │   ├── Company.java               # 企业
│   │   ├── Position.java              # 岗位
│   │   ├── Candidate.java             # 候选人
│   │   ├── Application.java           # 投递记录
│   │   └── ScreenRule.java            # 筛选规则
│   ├── mapper/             # MyBatis Mapper
│   ├── security/           # 安全认证
│   │   ├── JwtUtils.java              # JWT工具
│   │   ├── JwtInterceptor.java        # JWT拦截器
│   │   └── PageAuthInterceptor.java   # 页面权限拦截
│   ├── config/             # 配置类
│   │   ├── WebMvcConfig.java          # MVC配置
│   │   └── DataInitializer.java       # 示例数据初始化
│   ├── dto/                # 数据传输对象
│   ├── common/             # 公共组件
│   └── RecruiApplication.java # 启动类
├── src/main/resources/
│   ├── templates/          # Thymeleaf模板
│   │   ├── login.html                 # 登录页
│   │   ├── register.html              # 注册页
│   │   ├── dashboard.html             # HR/管理员后台
│   │   ├── portal/                    # 求职者门户
│   │   └── positions.html             # 岗位列表
│   ├── db/                 # 数据库脚本
│   │   ├── schema.sql                 # MySQL建表
│   │   └── schema-h2.sql             # H2建表（测试）
│   ├── static/             # 静态资源
│   └── application.yml     # 应用配置
└── src/test/               # 单元测试
```

---

## 许可证

MIT License © 2026 ATS Recruitment System
