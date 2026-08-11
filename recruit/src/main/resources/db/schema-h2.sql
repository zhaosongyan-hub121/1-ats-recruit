-- H2 内存数据库初始化脚本（与 MySQL 版本数据一致）
-- Spring Boot 启动时自动执行，每次重启重建数据，方便演示

DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS screen_result;
DROP TABLE IF EXISTS screen_rule;
DROP TABLE IF EXISTS application;
DROP TABLE IF EXISTS candidate;
DROP TABLE IF EXISTS job_position;
DROP TABLE IF EXISTS sys_user;

-- ----------------------------
-- 系统用户表
-- ----------------------------
CREATE TABLE sys_user (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  username   VARCHAR(50)  NOT NULL,
  password   VARCHAR(100) NOT NULL,
  real_name  VARCHAR(50)           DEFAULT NULL,
  email      VARCHAR(100)          DEFAULT NULL,
  phone      VARCHAR(20)           DEFAULT NULL,
  role       VARCHAR(20)  NOT NULL DEFAULT 'CANDIDATE',
  company    VARCHAR(100)          DEFAULT NULL,
  avatar     VARCHAR(255)          DEFAULT NULL,
  created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE (username)
);

-- ----------------------------
-- 职位表
-- ----------------------------
CREATE TABLE job_position (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  title           VARCHAR(100) NOT NULL,
  company_name    VARCHAR(100)          DEFAULT NULL,
  company_logo    VARCHAR(10)           DEFAULT NULL,
  department      VARCHAR(50)           DEFAULT NULL,
  description     CLOB                  DEFAULT NULL,
  requirements    CLOB                  DEFAULT NULL,
  status          VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
  category        VARCHAR(20)  NOT NULL DEFAULT 'SOCIAL',
  location        VARCHAR(50)           DEFAULT NULL,
  salary          VARCHAR(30)           DEFAULT NULL,
  education       VARCHAR(30)           DEFAULT NULL,
  experience      VARCHAR(30)           DEFAULT NULL,
  publish_user_id BIGINT                DEFAULT NULL,
  created_at      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted         TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- ----------------------------
-- 候选人表
-- ----------------------------
CREATE TABLE candidate (
  id               BIGINT       NOT NULL AUTO_INCREMENT,
  user_id          BIGINT                DEFAULT NULL,
  name             VARCHAR(50)  NOT NULL,
  email            VARCHAR(100)          DEFAULT NULL,
  phone            VARCHAR(20)           DEFAULT NULL,
  skills           VARCHAR(500)          DEFAULT NULL,
  experience_years INT                   DEFAULT NULL,
  resume_text      CLOB                  DEFAULT NULL,
  education_level  VARCHAR(30)           DEFAULT NULL,
  school           VARCHAR(100)          DEFAULT NULL,
  created_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted          TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- ----------------------------
-- 投递记录表
-- ----------------------------
CREATE TABLE application (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  candidate_id  BIGINT       NOT NULL,
  user_id       BIGINT                DEFAULT NULL,
  position_id   BIGINT       NOT NULL,
  status        VARCHAR(30)  NOT NULL DEFAULT 'SUBMITTED',
  current_round INT          NOT NULL DEFAULT 1,
  cover_letter  CLOB                  DEFAULT NULL,
  hr_remark     CLOB                  DEFAULT NULL,
  status_log    CLOB                  DEFAULT NULL,
  created_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- ----------------------------
-- 职位收藏表
-- ----------------------------
CREATE TABLE favorite (
  id          BIGINT   NOT NULL AUTO_INCREMENT,
  user_id     BIGINT   NOT NULL,
  position_id BIGINT   NOT NULL,
  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (user_id, position_id)
);

-- ----------------------------
-- 筛选规则表
-- ----------------------------
CREATE TABLE screen_rule (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  name            VARCHAR(100) NOT NULL,
  rule_type       VARCHAR(30)  NOT NULL,
  target_field    VARCHAR(30)  NOT NULL,
  expected_values VARCHAR(500) NOT NULL,
  match_mode      VARCHAR(20)  NOT NULL DEFAULT 'ANY',
  weight          INT          NOT NULL DEFAULT 10,
  enabled         TINYINT      NOT NULL DEFAULT 1,
  position_id     BIGINT                DEFAULT NULL,
  created_at      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted         TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- ----------------------------
-- 筛选结果表
-- ----------------------------
CREATE TABLE screen_result (
  id             BIGINT      NOT NULL AUTO_INCREMENT,
  application_id BIGINT      NOT NULL,
  total_score    INT         NOT NULL DEFAULT 0,
  max_score      INT         NOT NULL DEFAULT 0,
  pass           TINYINT     NOT NULL DEFAULT 0,
  rule_details   CLOB                 DEFAULT NULL,
  created_at     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted        TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- ----------------------------
-- 初始化账号（密码统一为 admin123）
-- admin / admin123
-- hr_tencent / admin123
-- hr_ali / admin123
-- hr_bytedance / admin123
-- ----------------------------
INSERT INTO sys_user(username, password, real_name, email, phone, role, company) VALUES
('admin',        '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '系统管理员', 'a****@***********', NULL, 'ADMIN', NULL),
('hr_tencent',   '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '张HR',   'z***@tencent.com', '13800000001', 'HR', '腾讯'),
('hr_ali',       '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '李HR',   'l*@alibaba.com',  '13800000002', 'HR', '阿里巴巴'),
('hr_bytedance', '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '王HR',   'w***@bytedance.com', '13800000003', 'HR', '字节跳动');

-- ----------------------------
-- 演示岗位数据（22个，覆盖三大公司社招/校招/实习）
-- ----------------------------
INSERT INTO job_position(title, company_name, company_logo, department, description, requirements, status, category, location, salary, education, experience, publish_user_id) VALUES
-- === 腾讯 社招 ===
('Java 后端开发工程师（微信事业群）', '腾讯', '腾', '技术工程事业群', '负责微信支付核心系统后端研发，参与高并发分布式系统设计与性能优化', '3年以上Java开发经验；精通SpringBoot、MyBatis、Redis、MySQL；熟悉分布式架构，有高并发系统经验', 'OPEN', 'SOCIAL', '深圳', '25-45K·16薪', '本科', '3-5年', 2),
('高级前端开发工程师（腾讯云）', '腾讯', '腾', '云与智慧产业事业群', '负责腾讯云控制台前端架构设计和核心模块开发', '5年以上前端经验；精通Vue/React；有大型中后台系统或云产品经验；具备性能优化实战经验', 'OPEN', 'SOCIAL', '深圳', '30-50K·16薪', '本科', '5-10年', 2),
('产品经理（QQ音乐）', '腾讯', '腾', 'QQ音乐产品部', '负责QQ音乐社区功能规划和迭代，推动产品从0到1', '3年以上互联网产品经验；对音乐/内容社区有深度理解；有成功产品案例优先', 'OPEN', 'SOCIAL', '广州', '20-35K·16薪', '本科', '3-5年', 2),
('算法工程师（推荐方向）', '腾讯', '腾', 'AI Lab', '参与大规模推荐算法研发，服务于腾讯视频、新闻等亿级用户产品', '硕士及以上学历；熟悉推荐系统常用算法（协同过滤、深度学习等）；有TensorFlow/PyTorch实战经验', 'OPEN', 'SOCIAL', '北京', '35-60K·16薪', '硕士', '3-5年', 2),
-- === 阿里巴巴 社招 ===
('Java 高级开发工程师（淘宝）', '阿里巴巴', '阿', '淘宝技术部', '参与淘宝核心交易链路开发，打造高可用电商平台', '4年以上Java开发经验；精通JVM调优、分布式中间件（RocketMQ/Dubbo）；有电商或大流量场景经验', 'OPEN', 'SOCIAL', '杭州', '30-50K·16薪', '本科', '5-10年', 3),
('数据分析师（蚂蚁集团）', '阿里巴巴', '阿', '蚂蚁集团数据智能部', '建设支付风控数据分析体系，输出数据洞察支持业务决策', '熟练使用SQL/Hive/Spark；有数据分析建模经验；金融科技背景优先', 'OPEN', 'SOCIAL', '杭州', '25-40K·16薪', '本科', '3-5年', 3),
('前端开发工程师（钉钉）', '阿里巴巴', '阿', '钉钉事业部', '负责钉钉协同办公产品前端研发，提升亿级用户体验', '3年以上前端经验；精通React；有复杂交互系统开发经验；了解Electron优先', 'OPEN', 'SOCIAL', '杭州', '25-40K·16薪', '本科', '3-5年', 3),
('云原生架构师（阿里云）', '阿里巴巴', '阿', '阿里云智能', '为客户提供云原生架构咨询和解决方案设计', '8年以上后端经验；精通Kubernetes/Docker/微服务架构；有TOB解决方案经验优先', 'OPEN', 'SOCIAL', '上海', '45-80K·16薪', '本科', '10年以上', 3),
-- === 字节跳动 社招 ===
('后端开发工程师（抖音）', '字节跳动', '字', '抖音服务端', '负责抖音推荐Feed、直播等核心场景服务端开发', '3年以上Go/Java/Python后端经验；扎实的计算机基础；有高并发系统经验', 'OPEN', 'SOCIAL', '北京', '25-50K·15薪', '本科', '3-5年', 4),
('客户端开发工程师（iOS/Android）', '字节跳动', '字', '抖音客户端', '负责抖音iOS/Android端功能开发和性能优化', '3年以上移动端开发经验；精通Objective-C/Swift或Java/Kotlin；有音视频相关经验优先', 'OPEN', 'SOCIAL', '北京', '25-45K·15薪', '本科', '3-5年', 4),
('产品经理（TikTok）', '字节跳动', '字', 'TikTok产品部', '负责TikTok国际化产品功能规划，面向全球用户', '3年以上产品经验；英语可作为工作语言；有海外产品经验优先', 'OPEN', 'SOCIAL', '上海', '25-45K·15薪', '本科', '3-5年', 4),
-- === 校招 ===
('2027届校招 - Java开发工程师', '腾讯', '腾', '校招-技术岗', '面向2027届毕业生，参与微信/QQ/腾讯云等核心产品后端开发', '2027届计算机相关专业本科及以上；Java/Go/C++基础扎实；有ACM/项目经验优先', 'OPEN', 'CAMPUS', '深圳', '18-28K·16薪', '本科', '应届生', 2),
('2027届校招 - 前端开发工程师', '阿里巴巴', '阿', '校招-技术岗', '面向2027届毕业生，参与淘宝/天猫/支付宝等产品前端研发', '2027届本科及以上；熟悉HTML/CSS/JavaScript；了解Vue/React；有个人作品优先', 'OPEN', 'CAMPUS', '杭州', '18-28K·16薪', '本科', '应届生', 3),
('2027届校招 - 算法工程师', '字节跳动', '字', '校招-算法岗', '面向2027届毕业生，参与推荐/NLP/CV算法研发', '2027届硕士及以上计算机/数学相关专业；熟悉机器学习基础算法；有顶会论文/Kaggle/ACM经验优先', 'OPEN', 'CAMPUS', '北京', '25-40K·15薪', '硕士', '应届生', 4),
('2027届校招 - 产品经理培训生', '腾讯', '腾', '校招-产品岗', '面向2027届毕业生，全流程参与产品设计与迭代，接受系统培训', '2027届本科及以上；对互联网产品有热情；逻辑清晰，有学生工作/项目经历优先', 'OPEN', 'CAMPUS', '深圳', '15-22K·16薪', '本科', '应届生', 2),
('2027届校招 - 数据分析师', '阿里巴巴', '阿', '校招-数据岗', '面向2027届毕业生，参与业务数据分析和数据产品建设', '2027届本科及以上；熟练使用SQL/Python；有数据分析/数学建模比赛经验优先', 'OPEN', 'CAMPUS', '杭州', '18-25K·16薪', '本科', '应届生', 3),
('2027届校招 - UI/UX设计师', '字节跳动', '字', '校招-设计岗', '面向2027届毕业生，参与产品界面设计和用户体验优化', '2027届设计/人机交互相关专业；熟练使用Figma/Sketch；有作品集', 'OPEN', 'CAMPUS', '北京', '15-22K·15薪', '本科', '应届生', 4),
-- === 实习 ===
('Java后端开发实习生', '腾讯', '腾', '技术部', '参与后端日常开发、单元测试编写、技术文档整理', '在校本科/研究生，每周实习4天以上，可连续3个月；熟悉Java基础；了解SpringBoot', 'OPEN', 'INTERN', '深圳', '400-600/天', '本科', '实习生', 2),
('前端开发实习生', '阿里巴巴', '阿', '淘宝前端', '参与淘宝页面开发，使用React构建用户界面', '在校本科/研究生，每周实习4天以上；熟悉HTML/CSS/JS；了解React', 'OPEN', 'INTERN', '杭州', '400-600/天', '本科', '实习生', 3),
('算法实习生（NLP方向）', '字节跳动', '字', 'AI Lab', '参与大语言模型相关研发，协助数据处理和实验', '硕士在读计算机相关专业；熟悉PyTorch；有NLP/LLM相关项目经验', 'OPEN', 'INTERN', '北京', '500-800/天', '硕士', '实习生', 4),
('产品经理实习生', '腾讯', '腾', 'QQ音乐产品部', '协助产品经理进行需求调研、数据分析和竞品分析', '在校本科/研究生，每周实习3天以上；对互联网产品有浓厚兴趣', 'OPEN', 'INTERN', '广州', '200-300/天', '本科', '实习生', 2),
('HR实习生', '字节跳动', '字', '人力资源部', '协助招聘全流程工作：简历筛选、面试安排、候选人沟通', '在校本科/研究生，人力资源/心理学相关专业优先；工作细心、沟通能力强', 'OPEN', 'INTERN', '北京', '200-300/天', '本科', '实习生', 4),
('UI设计实习生', '阿里巴巴', '阿', '设计部', '参与移动端产品UI设计、运营活动页面设计', '设计相关专业在校生；熟练使用Figma；有作品集优先', 'OPEN', 'INTERN', '杭州', '200-400/天', '本科', '实习生', 3);

-- 筛选规则
INSERT INTO screen_rule(name, rule_type, target_field, expected_values, match_mode, weight, enabled, position_id) VALUES
('核心技能匹配', 'SKILL', 'skills', 'Java,SpringBoot,MySQL', 'ANY', 30, 1, 1),
('Redis 加分项', 'SKILL', 'skills', 'Redis', 'ANY', 10, 1, 1),
('经验年限', 'EXPERIENCE', 'experience_years', '3', 'MIN', 20, 1, 1),
('简历关键词', 'KEYWORD', 'resume_text', 'Spring,后端', 'ANY', 20, 1, 1);
