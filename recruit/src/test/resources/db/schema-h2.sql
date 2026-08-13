-- H2 内存数据库测试用建表脚本（MySQL 兼容模式）
-- 不含 USE/SET NAMES/ENGINE 等 MySQL 专有语法

DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS screen_result;
DROP TABLE IF EXISTS screen_rule;
DROP TABLE IF EXISTS application;
DROP TABLE IF EXISTS candidate;
DROP TABLE IF EXISTS job_position;
DROP TABLE IF EXISTS company;
DROP TABLE IF EXISTS sys_user;

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
CREATE TABLE company (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  name        VARCHAR(100) NOT NULL,
  logo        VARCHAR(10)           DEFAULT NULL,
  logo_color  VARCHAR(20)           DEFAULT NULL,
  industry    VARCHAR(50)           DEFAULT NULL,
  description TEXT                  DEFAULT NULL,
  location    VARCHAR(100)          DEFAULT NULL,
  website     VARCHAR(255)          DEFAULT NULL,
  size        VARCHAR(30)           DEFAULT NULL,
  created_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE TABLE job_position (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  title           VARCHAR(100) NOT NULL,
  company_name    VARCHAR(100)          DEFAULT NULL,
  company_id      BIGINT                DEFAULT NULL,
  company_logo    VARCHAR(10)           DEFAULT NULL,
  department      VARCHAR(50)           DEFAULT NULL,
  description     TEXT                  DEFAULT NULL,
  requirements    TEXT                  DEFAULT NULL,
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

CREATE TABLE candidate (
  id               BIGINT       NOT NULL AUTO_INCREMENT,
  user_id          BIGINT                DEFAULT NULL,
  name             VARCHAR(50)  NOT NULL,
  email            VARCHAR(100)          DEFAULT NULL,
  phone            VARCHAR(20)           DEFAULT NULL,
  skills           VARCHAR(500)          DEFAULT NULL,
  experience_years INT                   DEFAULT NULL,
  resume_text      TEXT                  DEFAULT NULL,
  education_level  VARCHAR(30)           DEFAULT NULL,
  school           VARCHAR(100)          DEFAULT NULL,
  created_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted          TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE TABLE application (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  candidate_id  BIGINT       NOT NULL,
  user_id       BIGINT                DEFAULT NULL,
  position_id   BIGINT       NOT NULL,
  status        VARCHAR(30)  NOT NULL DEFAULT 'SUBMITTED',
  current_round INT          NOT NULL DEFAULT 1,
  cover_letter  TEXT                  DEFAULT NULL,
  hr_remark     TEXT                  DEFAULT NULL,
  status_log    TEXT                  DEFAULT NULL,
  created_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE TABLE favorite (
  id          BIGINT   NOT NULL AUTO_INCREMENT,
  user_id     BIGINT   NOT NULL,
  position_id BIGINT   NOT NULL,
  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE (user_id, position_id)
);

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

CREATE TABLE screen_result (
  id             BIGINT      NOT NULL AUTO_INCREMENT,
  application_id BIGINT      NOT NULL,
  total_score    INT         NOT NULL DEFAULT 0,
  max_score      INT         NOT NULL DEFAULT 0,
  pass           TINYINT     NOT NULL DEFAULT 0,
  rule_details   TEXT                 DEFAULT NULL,
  created_at     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted        TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- ----------------------------
-- 演示数据：公司
-- ----------------------------
INSERT INTO company(name, logo, logo_color, industry, description, location, website, size) VALUES
('腾讯',     '腾', '#0052D9', '互联网', '中国领先的互联网增值服务提供商', '深圳', 'https://www.tencent.com', '10000人以上'),
('阿里巴巴', '阿', '#FF6A00', '互联网', '全球领先的电子商务及科技公司', '杭州', 'https://www.alibaba.com', '10000人以上'),
('字节跳动', '字', '#3C7EFF', '互联网', '全球化的内容平台公司', '北京', 'https://www.bytedance.com', '10000人以上'),
('美团',     '美', '#FFD100', '互联网', '中国领先的生活服务电子商务平台', '北京', 'https://www.meituan.com', '10000人以上'),
('百度',     '百', '#2319DC', '互联网', '全球最大的中文搜索引擎', '北京', 'https://www.baidu.com', '10000人以上'),
('京东',     '京', '#E1251B', '互联网', '中国领先的技术驱动型电商', '北京', 'https://www.jd.com', '10000人以上');

-- 预置数据：账号密码统一为 admin123 (BCrypt hash)
INSERT INTO sys_user(username, password, real_name, email, role, company, company_id) VALUES
('admin',        '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '系统管理员', 'a****@***********', 'ADMIN',    NULL,    NULL),
('hr_tencent',   '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '张HR',   'z***@tencent.com', 'HR',       '腾讯',     1),
('hr_ali',       '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '李HR',   'l*@alibaba.com',  'HR',       '阿里巴巴', 2),
('hr_bytedance', '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '王HR',   'w***@bytedance.com', 'HR',     '字节跳动', 3),
('hr_meituan',   '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '赵HR',   'z***@meituan.com', 'HR',       '美团',     4),
('hr_baidu',     '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '孙HR',   's**@baidu.com',    'HR',       '百度',     5),
('hr_jd',        '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '周HR',   'z**@jd.com',       'HR',       '京东',     6),
('c_chen',       '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '陈求职', 'c***@example.com', 'CANDIDATE', NULL,    NULL),
('c_liu',        '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '刘求职', 'l**@example.com',  'CANDIDATE', NULL,    NULL),
('c_yang',       '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '杨求职', 'y***@example.com', 'CANDIDATE', NULL,    NULL),
('c_huang',      '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '黄求职', 'h***@example.com', 'CANDIDATE', NULL,    NULL),
('c_wu',         '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '吴求职', 'w**@example.com',  'CANDIDATE', NULL,    NULL);

-- 演示岗位数据
INSERT INTO job_position(title, company_name, company_id, company_logo, department, description, requirements, status, category, location, salary, education, experience, publish_user_id) VALUES
('Java 后端开发工程师', '腾讯', 1, '腾', '技术部', '负责后端核心系统开发', '3年以上Java经验', 'OPEN', 'SOCIAL', '深圳', '25-45K·16薪', '本科', '3-5年', 2),
('高级前端开发工程师', '阿里巴巴', 2, '阿', '淘宝技术部', '负责淘宝前端开发', '5年前端经验，精通React', 'OPEN', 'SOCIAL', '杭州', '30-50K·16薪', '本科', '5-10年', 3),
('后端开发工程师（抖音）', '字节跳动', 3, '字', '抖音服务端', '负责抖音推荐Feed后端', '3年以上Go/Java经验', 'OPEN', 'SOCIAL', '北京', '25-50K·15薪', '本科', '3-5年', 4),
('Java 后端开发工程师（外卖）', '美团', 4, '美', '外卖技术部', '负责外卖订单系统后端', '3年以上Java经验', 'OPEN', 'SOCIAL', '北京', '25-45K·16薪', '本科', '3-5年', 5),
('AI 算法工程师', '百度', 5, '百', '文心一言团队', '参与大语言模型研发', '硕士及以上，熟悉NLP', 'OPEN', 'SOCIAL', '北京', '35-60K·16薪', '硕士', '3-5年', 6),
('Java 后端开发工程师（零售）', '京东', 6, '京', '零售技术部', '参与京东交易系统研发', '3年以上Java经验', 'OPEN', 'SOCIAL', '北京', '25-45K·16薪', '本科', '3-5年', 7),
('2027届校招 - Java开发工程师', '腾讯', 1, '腾', '校招-技术岗', '面向2027届毕业生', '2027届本科及以上', 'OPEN', 'CAMPUS', '深圳', '18-28K·16薪', '本科', '应届生', 2),
('Java后端开发实习生', '腾讯', 1, '腾', '技术部', '参与后端日常开发', '在校学生，熟悉Java', 'OPEN', 'INTERN', '深圳', '400-600/天', '本科', '实习生', 2);

-- ----------------------------
-- 演示数据：候选人
-- ----------------------------
INSERT INTO candidate(user_id, name, email, phone, skills, experience_years, resume_text, education_level, school) VALUES
(8,  '陈求职', 'c***@example.com', '13900000001', 'Java,SpringBoot,MySQL', 3, '三年Java后端开发经验', '硕士', '清华大学'),
(9,  '刘求职', 'l**@example.com',   '13900000002', 'Python,SQL,数据分析', 2, '两年数据分析经验', '本科', '北京大学'),
(10, '杨求职', 'y***@example.com',  '13900000003', 'React,TypeScript,Vue', 4, '四年前端开发经验', '本科', '浙江大学'),
(11, '黄求职', 'h***@example.com',  '13900000004', '算法,推荐系统,Python', 5, '五年算法研发经验', '博士', '上海交通大学'),
(12, '吴求职', 'w**@example.com',   '13900000005', '产品,用户运营', 1, '一年产品经验', '本科', '复旦大学');

-- ----------------------------
-- 演示数据：投递记录
-- ----------------------------
INSERT INTO application(candidate_id, user_id, position_id, status, current_round, cover_letter, hr_remark, status_log) VALUES
(1, 8,  1, 'SUBMITTED',      1, '热爱腾讯产品，希望加入微信支付团队', NULL, '[{"status":"SUBMITTED","time":"2026-08-01 10:00:00"}]'),
(2, 9,  2, 'SCREENING_PASS',  1, '对淘宝前端开发感兴趣', '初筛通过', '[{"status":"SUBMITTED","time":"2026-08-01 10:00:00"},{"status":"SCREENING_PASS","time":"2026-08-02 09:00:00"}]'),
(3, 10, 3, 'INTERVIEWING',    2, '希望加入抖音团队', '一面通过', '[{"status":"SUBMITTED","time":"2026-08-01 10:00:00"},{"status":"SCREENING_PASS","time":"2026-08-02 09:00:00"},{"status":"INTERVIEWING","time":"2026-08-03 14:00:00","round":1},{"status":"INTERVIEWING","time":"2026-08-05 10:00:00","round":2}]'),
(4, 11, 5, 'OFFER',           3, '对文心一言大模型感兴趣', '发放Offer', '[{"status":"SUBMITTED","time":"2026-07-25 10:00:00"},{"status":"SCREENING_PASS","time":"2026-07-26 09:00:00"},{"status":"INTERVIEWING","time":"2026-07-28 14:00:00","round":1},{"status":"INTERVIEWED","time":"2026-08-01 16:00:00"},{"status":"OFFER","time":"2026-08-02 10:00:00"}]'),
(5, 12, 1, 'ACCEPTED',        2, '希望加入腾讯产品团队', '已接受Offer', '[{"status":"SUBMITTED","time":"2026-07-20 10:00:00"},{"status":"SCREENING_PASS","time":"2026-07-21 09:00:00"},{"status":"INTERVIEWING","time":"2026-07-23 14:00:00","round":1},{"status":"INTERVIEWED","time":"2026-07-27 16:00:00"},{"status":"OFFER","time":"2026-07-28 10:00:00"},{"status":"ACCEPTED","time":"2026-08-01 15:00:00"}]'),
(1, 8,  4, 'REJECTED',        1, '希望加入美团', '暂不通过', '[{"status":"SUBMITTED","time":"2026-08-03 10:00:00"},{"status":"REJECTED","time":"2026-08-07 10:00:00"}]'),
(1, 8,  6, 'SUBMITTED',       1, '希望加入京东零售团队', NULL, '[{"status":"SUBMITTED","time":"2026-08-10 10:00:00"}]');
