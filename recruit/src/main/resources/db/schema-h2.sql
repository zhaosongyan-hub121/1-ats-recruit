-- H2 内存数据库初始化脚本（与 MySQL 版本数据一致）
-- Spring Boot 启动时自动执行，每次重启重建数据，方便演示

DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS screen_result;
DROP TABLE IF EXISTS screen_rule;
DROP TABLE IF EXISTS application;
DROP TABLE IF EXISTS candidate;
DROP TABLE IF EXISTS job_position;
DROP TABLE IF EXISTS company;
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
  company_id BIGINT                DEFAULT NULL,
  avatar     VARCHAR(255)          DEFAULT NULL,
  created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE (username)
);

-- ----------------------------
-- 公司表
-- ----------------------------
DROP TABLE IF EXISTS company;
CREATE TABLE company (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  name        VARCHAR(100) NOT NULL,
  logo        VARCHAR(10)           DEFAULT NULL,
  logo_color  VARCHAR(20)           DEFAULT NULL,
  industry    VARCHAR(50)           DEFAULT NULL,
  description CLOB                  DEFAULT NULL,
  location    VARCHAR(100)          DEFAULT NULL,
  website     VARCHAR(255)          DEFAULT NULL,
  size        VARCHAR(30)           DEFAULT NULL,
  created_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- ----------------------------
-- 职位表
-- ----------------------------
CREATE TABLE job_position (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  title           VARCHAR(100) NOT NULL,
  company_name    VARCHAR(100)          DEFAULT NULL,
  company_id      BIGINT                DEFAULT NULL,
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
-- 演示数据：公司
-- ----------------------------
INSERT INTO company(name, logo, logo_color, industry, description, location, website, size) VALUES
('腾讯',     '腾', '#0052D9', '互联网', '中国领先的互联网增值服务提供商，业务涵盖社交、游戏、金融、云计算等领域', '深圳', 'https://www.tencent.com', '10000人以上'),
('阿里巴巴', '阿', '#FF6A00', '互联网', '全球领先的电子商务及科技公司，业务涵盖电商、云计算、数字媒体等', '杭州', 'https://www.alibaba.com', '10000人以上'),
('字节跳动', '字', '#3C7EFF', '互联网', '全球化的内容平台公司，产品包括抖音、TikTok、今日头条等', '北京', 'https://www.bytedance.com', '10000人以上'),
('美团',     '美', '#FFD100', '互联网', '中国领先的生活服务电子商务平台，提供外卖、到店、酒店等服务', '北京', 'https://www.meituan.com', '10000人以上'),
('百度',     '百', '#2319DC', '互联网', '全球最大的中文搜索引擎，AI 技术领先的科技公司', '北京', 'https://www.baidu.com', '10000人以上'),
('京东',     '京', '#E1251B', '互联网', '中国领先的技术驱动型电商和基础设施服务提供商', '北京', 'https://www.jd.com', '10000人以上');

-- ----------------------------
-- 初始化账号（密码统一为 admin123）
-- admin / admin123
-- hr_tencent / admin123
-- hr_ali / admin123
-- hr_bytedance / admin123
-- hr_meituan / admin123
-- hr_baidu / admin123
-- hr_jd / admin123
-- 5个 CANDIDATE 角色账号
-- ----------------------------
INSERT INTO sys_user(username, password, real_name, email, phone, role, company, company_id) VALUES
('admin',        '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '系统管理员', 'a****@***********', NULL, 'ADMIN',    NULL,    NULL),
('hr_tencent',   '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '张HR',   'z***@tencent.com',   '13800000001', 'HR',       '腾讯',     1),
('hr_ali',       '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '李HR',   'l*@alibaba.com',    '13800000002', 'HR',       '阿里巴巴', 2),
('hr_bytedance', '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '王HR',   'w***@bytedance.com', '13800000003', 'HR',       '字节跳动', 3),
('hr_meituan',   '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '赵HR',   'z***@meituan.com',   '13800000004', 'HR',       '美团',     4),
('hr_baidu',     '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '孙HR',   's**@baidu.com',      '13800000005', 'HR',       '百度',     5),
('hr_jd',        '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '周HR',   'z**@jd.com',         '13800000006', 'HR',       '京东',     6),
('c_chen',       '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '陈求职', 'c***@example.com',   '13900000001', 'CANDIDATE', NULL,    NULL),
('c_liu',        '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '刘求职', 'l**@example.com',    '13900000002', 'CANDIDATE', NULL,    NULL),
('c_yang',       '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '杨求职', 'y***@example.com',   '13900000003', 'CANDIDATE', NULL,    NULL),
('c_huang',      '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '黄求职', 'h***@example.com',   '13900000004', 'CANDIDATE', NULL,    NULL),
('c_wu',         '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '吴求职', 'w**@example.com',    '13900000005', 'CANDIDATE', NULL,    NULL);

-- ----------------------------
-- 演示岗位数据（22个，覆盖三大公司社招/校招/实习）
-- ----------------------------
INSERT INTO job_position(title, company_name, company_id, company_logo, department, description, requirements, status, category, location, salary, education, experience, publish_user_id) VALUES
-- === 腾讯 社招 ===
('Java 后端开发工程师（微信事业群）', '腾讯', 1, '腾', '技术工程事业群', '负责微信支付核心系统后端研发，参与高并发分布式系统设计与性能优化', '3年以上Java开发经验；精通SpringBoot、MyBatis、Redis、MySQL；熟悉分布式架构，有高并发系统经验', 'OPEN', 'SOCIAL', '深圳', '25-45K·16薪', '本科', '3-5年', 2),
('高级前端开发工程师（腾讯云）', '腾讯', 1, '腾', '云与智慧产业事业群', '负责腾讯云控制台前端架构设计和核心模块开发', '5年以上前端经验；精通Vue/React；有大型中后台系统或云产品经验；具备性能优化实战经验', 'OPEN', 'SOCIAL', '深圳', '30-50K·16薪', '本科', '5-10年', 2),
('产品经理（QQ音乐）', '腾讯', 1, '腾', 'QQ音乐产品部', '负责QQ音乐社区功能规划和迭代，推动产品从0到1', '3年以上互联网产品经验；对音乐/内容社区有深度理解；有成功产品案例优先', 'OPEN', 'SOCIAL', '广州', '20-35K·16薪', '本科', '3-5年', 2),
('算法工程师（推荐方向）', '腾讯', 1, '腾', 'AI Lab', '参与大规模推荐算法研发，服务于腾讯视频、新闻等亿级用户产品', '硕士及以上学历；熟悉推荐系统常用算法（协同过滤、深度学习等）；有TensorFlow/PyTorch实战经验', 'OPEN', 'SOCIAL', '北京', '35-60K·16薪', '硕士', '3-5年', 2),
-- === 阿里巴巴 社招 ===
('Java 高级开发工程师（淘宝）', '阿里巴巴', 2, '阿', '淘宝技术部', '参与淘宝核心交易链路开发，打造高可用电商平台', '4年以上Java开发经验；精通JVM调优、分布式中间件（RocketMQ/Dubbo）；有电商或大流量场景经验', 'OPEN', 'SOCIAL', '杭州', '30-50K·16薪', '本科', '5-10年', 3),
('数据分析师（蚂蚁集团）', '阿里巴巴', 2, '阿', '蚂蚁集团数据智能部', '建设支付风控数据分析体系，输出数据洞察支持业务决策', '熟练使用SQL/Hive/Spark；有数据分析建模经验；金融科技背景优先', 'OPEN', 'SOCIAL', '杭州', '25-40K·16薪', '本科', '3-5年', 3),
('前端开发工程师（钉钉）', '阿里巴巴', 2, '阿', '钉钉事业部', '负责钉钉协同办公产品前端研发，提升亿级用户体验', '3年以上前端经验；精通React；有复杂交互系统开发经验；了解Electron优先', 'OPEN', 'SOCIAL', '杭州', '25-40K·16薪', '本科', '3-5年', 3),
('云原生架构师（阿里云）', '阿里巴巴', 2, '阿', '阿里云智能', '为客户提供云原生架构咨询和解决方案设计', '8年以上后端经验；精通Kubernetes/Docker/微服务架构；有TOB解决方案经验优先', 'OPEN', 'SOCIAL', '上海', '45-80K·16薪', '本科', '10年以上', 3),
-- === 字节跳动 社招 ===
('后端开发工程师（抖音）', '字节跳动', 3, '字', '抖音服务端', '负责抖音推荐Feed、直播等核心场景服务端开发', '3年以上Go/Java/Python后端经验；扎实的计算机基础；有高并发系统经验', 'OPEN', 'SOCIAL', '北京', '25-50K·15薪', '本科', '3-5年', 4),
('客户端开发工程师（iOS/Android）', '字节跳动', 3, '字', '抖音客户端', '负责抖音iOS/Android端功能开发和性能优化', '3年以上移动端开发经验；精通Objective-C/Swift或Java/Kotlin；有音视频相关经验优先', 'OPEN', 'SOCIAL', '北京', '25-45K·15薪', '本科', '3-5年', 4),
('产品经理（TikTok）', '字节跳动', 3, '字', 'TikTok产品部', '负责TikTok国际化产品功能规划，面向全球用户', '3年以上产品经验；英语可作为工作语言；有海外产品经验优先', 'OPEN', 'SOCIAL', '上海', '25-45K·15薪', '本科', '3-5年', 4),
-- === 美团 社招 ===
('Java 后端开发工程师（外卖事业部）', '美团', 4, '美', '外卖技术部', '负责外卖订单系统后端研发，支撑千万级日订单量', '3年以上Java开发经验；熟悉高并发系统设计；有电商或O2O经验优先', 'OPEN', 'SOCIAL', '北京', '25-45K·16薪', '本科', '3-5年', 5),
('算法工程师（调度优化方向）', '美团', 4, '美', '调度算法部', '参与美团即时配送调度优化算法研发', '硕士及以上学历；熟悉运筹优化/机器学习；有相关领域论文或项目经验', 'OPEN', 'SOCIAL', '北京', '30-55K·16薪', '硕士', '3-5年', 5),
('产品经理（到店事业群）', '美团', 4, '美', '到店产品部', '负责到店餐饮、酒旅等业务产品规划与迭代', '3年以上互联网产品经验；对本地生活服务有深刻理解', 'OPEN', 'SOCIAL', '北京', '20-35K·16薪', '本科', '3-5年', 5),
-- === 百度 社招 ===
('Java 高级开发工程师（搜索部）', '百度', 5, '百', '搜索产品部', '参与百度搜索核心系统研发，优化搜索质量与性能', '4年以上Java经验；熟悉搜索引擎架构；有大规模分布式系统经验', 'OPEN', 'SOCIAL', '北京', '30-50K·16薪', '本科', '5-10年', 6),
('AI 算法工程师（文心一言团队）', '百度', 5, '百', '文心一言团队', '参与大语言模型预训练和对齐工作，推进AI技术落地', '硕士及以上学历；熟悉NLP/LLM；有大模型相关论文或项目经验', 'OPEN', 'SOCIAL', '北京', '35-60K·16薪', '硕士', '3-5年', 6),
('数据分析师（地图事业群）', '百度', 5, '百', '地图数据部', '建设导航和地图业务数据分析体系', '熟练使用SQL/Python；有空间数据分析经验优先；统计/数学相关专业', 'OPEN', 'SOCIAL', '北京', '25-40K·16薪', '本科', '3-5年', 6),
-- === 京东 社招 ===
('Java 后端开发工程师（零售事业部）', '京东', 6, '京', '零售技术部', '参与京东零售核心交易系统研发', '3年以上Java经验；熟悉电商交易链路；有大型分布式系统经验', 'OPEN', 'SOCIAL', '北京', '25-45K·16薪', '本科', '3-5年', 7),
('物流算法工程师（京东物流）', '京东', 6, '京', '物流技术部', '参与智能物流调度、路径规划等算法研发', '硕士及以上学历；熟悉运筹优化/机器学习；有物流或供应链算法经验', 'OPEN', 'SOCIAL', '北京', '30-50K·16薪', '硕士', '3-5年', 7),
('前端开发工程师（京东科技）', '京东', 6, '京', '京东科技', '负责京东金融科技产品前端研发', '3年以上前端经验；精通React/Vue；有大型中后台系统开发经验', 'OPEN', 'SOCIAL', '北京', '25-40K·16薪', '本科', '3-5年', 7),
-- === 校招 ===
('2027届校招 - Java开发工程师', '腾讯', 1, '腾', '校招-技术岗', '面向2027届毕业生，参与微信/QQ/腾讯云等核心产品后端开发', '2027届计算机相关专业本科及以上；Java/Go/C++基础扎实；有ACM/项目经验优先', 'OPEN', 'CAMPUS', '深圳', '18-28K·16薪', '本科', '应届生', 2),
('2027届校招 - 前端开发工程师', '阿里巴巴', 2, '阿', '校招-技术岗', '面向2027届毕业生，参与淘宝/天猫/支付宝等产品前端研发', '2027届本科及以上；熟悉HTML/CSS/JavaScript；了解Vue/React；有个人作品优先', 'OPEN', 'CAMPUS', '杭州', '18-28K·16薪', '本科', '应届生', 3),
('2027届校招 - 算法工程师', '字节跳动', 3, '字', '校招-算法岗', '面向2027届毕业生，参与推荐/NLP/CV算法研发', '2027届硕士及以上计算机/数学相关专业；熟悉机器学习基础算法；有顶会论文/Kaggle/ACM经验优先', 'OPEN', 'CAMPUS', '北京', '25-40K·15薪', '硕士', '应届生', 4),
('2027届校招 - 产品经理培训生', '腾讯', 1, '腾', '校招-产品岗', '面向2027届毕业生，全流程参与产品设计与迭代，接受系统培训', '2027届本科及以上；对互联网产品有热情；逻辑清晰，有学生工作/项目经历优先', 'OPEN', 'CAMPUS', '深圳', '15-22K·16薪', '本科', '应届生', 2),
('2027届校招 - 数据分析师', '阿里巴巴', 2, '阿', '校招-数据岗', '面向2027届毕业生，参与业务数据分析和数据产品建设', '2027届本科及以上；熟练使用SQL/Python；有数据分析/数学建模比赛经验优先', 'OPEN', 'CAMPUS', '杭州', '18-25K·16薪', '本科', '应届生', 3),
('2027届校招 - UI/UX设计师', '字节跳动', 3, '字', '校招-设计岗', '面向2027届毕业生，参与产品界面设计和用户体验优化', '2027届设计/人机交互相关专业；熟练使用Figma/Sketch；有作品集', 'OPEN', 'CAMPUS', '北京', '15-22K·15薪', '本科', '应届生', 4),
-- === 实习 ===
('Java后端开发实习生', '腾讯', 1, '腾', '技术部', '参与后端日常开发、单元测试编写、技术文档整理', '在校本科/研究生，每周实习4天以上，可连续3个月；熟悉Java基础；了解SpringBoot', 'OPEN', 'INTERN', '深圳', '400-600/天', '本科', '实习生', 2),
('前端开发实习生', '阿里巴巴', 2, '阿', '淘宝前端', '参与淘宝页面开发，使用React构建用户界面', '在校本科/研究生，每周实习4天以上；熟悉HTML/CSS/JS；了解React', 'OPEN', 'INTERN', '杭州', '400-600/天', '本科', '实习生', 3),
('算法实习生（NLP方向）', '字节跳动', 3, '字', 'AI Lab', '参与大语言模型相关研发，协助数据处理和实验', '硕士在读计算机相关专业；熟悉PyTorch；有NLP/LLM相关项目经验', 'OPEN', 'INTERN', '北京', '500-800/天', '硕士', '实习生', 4),
('产品经理实习生', '腾讯', 1, '腾', 'QQ音乐产品部', '协助产品经理进行需求调研、数据分析和竞品分析', '在校本科/研究生，每周实习3天以上；对互联网产品有浓厚兴趣', 'OPEN', 'INTERN', '广州', '200-300/天', '本科', '实习生', 2),
('HR实习生', '字节跳动', 3, '字', '人力资源部', '协助招聘全流程工作：简历筛选、面试安排、候选人沟通', '在校本科/研究生，人力资源/心理学相关专业优先；工作细心、沟通能力强', 'OPEN', 'INTERN', '北京', '200-300/天', '本科', '实习生', 4),
('UI设计实习生', '阿里巴巴', 2, '阿', '设计部', '参与移动端产品UI设计、运营活动页面设计', '设计相关专业在校生；熟练使用Figma；有作品集优先', 'OPEN', 'INTERN', '杭州', '200-400/天', '本科', '实习生', 3);

-- 筛选规则
INSERT INTO screen_rule(name, rule_type, target_field, expected_values, match_mode, weight, enabled, position_id) VALUES
('核心技能匹配', 'SKILL', 'skills', 'Java,SpringBoot,MySQL', 'ANY', 30, 1, 1),
('Redis 加分项', 'SKILL', 'skills', 'Redis', 'ANY', 10, 1, 1),
('经验年限', 'EXPERIENCE', 'experience_years', '3', 'MIN', 20, 1, 1),
('简历关键词', 'KEYWORD', 'resume_text', 'Spring,后端', 'ANY', 20, 1, 1);

-- ----------------------------
-- 演示数据：候选人
-- ----------------------------
INSERT INTO candidate(user_id, name, email, phone, skills, experience_years, resume_text, education_level, school) VALUES
(8,  '陈求职', 'c***@example.com', '13900000001', 'Java,SpringBoot,MySQL,Redis', 3, '三年Java后端开发经验，参与过大型电商系统开发，熟悉微服务架构和分布式缓存', '硕士', '清华大学'),
(9,  '刘求职', 'l**@example.com',   '13900000002', 'Python,SQL,数据分析,Hive', 2, '两年数据分析经验，熟悉数据仓库建设和BI报表开发，参与过支付风控数据分析项目', '本科', '北京大学'),
(10, '杨求职', 'y***@example.com',  '13900000003', 'React,TypeScript,Vue,Webpack', 4, '四年前端开发经验，主导过大型中后台系统架构设计，熟悉性能优化和工程化', '本科', '浙江大学'),
(11, '黄求职', 'h***@example.com',  '13900000004', '算法,推荐系统,Python,TensorFlow,PyTorch', 5, '五年算法研发经验，参与过亿级用户产品的推荐系统搭建，熟悉深度学习和推荐算法', '博士', '上海交通大学'),
(12, '吴求职', 'w**@example.com',   '13900000005', '产品,用户运营,数据分析,需求管理', 1, '一年产品经验，参与过互联网C端产品从0到1，熟悉用户研究和数据分析方法', '本科', '复旦大学');

-- ----------------------------
-- 演示数据：投递记录
-- ----------------------------
INSERT INTO application(candidate_id, user_id, position_id, status, current_round, cover_letter, hr_remark, status_log) VALUES
(1, 8,  1,  'SUBMITTED',       1, '热爱腾讯产品，希望加入微信支付团队，参与亿级用户产品开发', NULL, '[{"status":"SUBMITTED","time":"2026-08-01 10:00:00"}]'),
(2, 9,  6,  'SCREENING_PASS',  1, '对蚂蚁集团的风控数据体系建设很感兴趣，具备扎实的SQL和数据分析能力', '初筛通过，进入面试流程', '[{"status":"SUBMITTED","time":"2026-08-01 10:00:00"},{"status":"SCREENING_PASS","time":"2026-08-02 09:00:00","remark":"初筛通过"}]'),
(3, 10, 7,  'INTERVIEWING',    2, '熟悉React和大型前端架构，希望加入钉钉团队提升协同办公产品体验', '一面通过，安排技术二面', '[{"status":"SUBMITTED","time":"2026-08-01 10:00:00"},{"status":"SCREENING_PASS","time":"2026-08-02 09:00:00"},{"status":"INTERVIEWING","time":"2026-08-03 14:00:00","round":1},{"status":"INTERVIEWING","time":"2026-08-05 10:00:00","round":2}]'),
(4, 11, 4,  'OFFER',           3, '在推荐系统和深度学习方面有深入研究，希望加入腾讯AI Lab', '技术面和HR面均通过，发放Offer', '[{"status":"SUBMITTED","time":"2026-07-25 10:00:00"},{"status":"SCREENING_PASS","time":"2026-07-26 09:00:00"},{"status":"INTERVIEWING","time":"2026-07-28 14:00:00","round":1},{"status":"INTERVIEWING","time":"2026-07-30 10:00:00","round":2},{"status":"INTERVIEWED","time":"2026-08-01 16:00:00"},{"status":"OFFER","time":"2026-08-02 10:00:00"}]'),
(5, 12, 3,  'ACCEPTED',        2, '对QQ音乐社区产品有深刻理解，希望参与产品建设', '候选人已接受Offer，准备入职', '[{"status":"SUBMITTED","time":"2026-07-20 10:00:00"},{"status":"SCREENING_PASS","time":"2026-07-21 09:00:00"},{"status":"INTERVIEWING","time":"2026-07-23 14:00:00","round":1},{"status":"INTERVIEWING","time":"2026-07-25 10:00:00","round":2},{"status":"INTERVIEWED","time":"2026-07-27 16:00:00"},{"status":"OFFER","time":"2026-07-28 10:00:00"},{"status":"ACCEPTED","time":"2026-08-01 15:00:00"}]'),
(1, 8,  5,  'REJECTED',        1, '希望加入阿里巴巴淘宝团队', '与岗位要求不完全匹配，暂不通过', '[{"status":"SUBMITTED","time":"2026-08-03 10:00:00"},{"status":"SCREENING_PASS","time":"2026-08-04 09:00:00"},{"status":"INTERVIEWING","time":"2026-08-05 14:00:00","round":1},{"status":"REJECTED","time":"2026-08-07 10:00:00","remark":"与岗位要求不完全匹配"}]'),
(11, 4,  16, 'SUBMITTED',       1, '对文心一言大模型团队很感兴趣', NULL, '[{"status":"SUBMITTED","time":"2026-08-10 10:00:00"}]'),
(8,  1,  12, 'SCREENING_PASS',  1, '希望加入美团外卖技术团队', '初筛通过', '[{"status":"SUBMITTED","time":"2026-08-08 10:00:00"},{"status":"SCREENING_PASS","time":"2026-08-09 09:00:00","remark":"初筛通过"}]'),
(9,  2,  13, 'INTERVIEWING',    1, '对美团调度算法方向很感兴趣', '安排技术一面', '[{"status":"SUBMITTED","time":"2026-08-05 10:00:00"},{"status":"SCREENING_PASS","time":"2026-08-06 09:00:00"},{"status":"INTERVIEWING","time":"2026-08-08 14:00:00","round":1}]'),
(10, 3,  18, 'OFFER',           2, '希望加入京东零售技术部', '技术面通过，发放Offer', '[{"status":"SUBMITTED","time":"2026-07-28 10:00:00"},{"status":"SCREENING_PASS","time":"2026-07-29 09:00:00"},{"status":"INTERVIEWING","time":"2026-07-31 14:00:00","round":1},{"status":"INTERVIEWING","time":"2026-08-02 10:00:00","round":2},{"status":"INTERVIEWED","time":"2026-08-04 16:00:00"},{"status":"OFFER","time":"2026-08-05 10:00:00"}]');
