-- H2 内存数据库测试用建表脚本（MySQL 兼容模式）
-- 不含 USE/SET NAMES/ENGINE 等 MySQL 专有语法

DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS screen_result;
DROP TABLE IF EXISTS screen_rule;
DROP TABLE IF EXISTS application;
DROP TABLE IF EXISTS candidate;
DROP TABLE IF EXISTS job_position;
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
  avatar     VARCHAR(255)          DEFAULT NULL,
  created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE (username)
);

CREATE TABLE job_position (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  title           VARCHAR(100) NOT NULL,
  company_name    VARCHAR(100)          DEFAULT NULL,
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

-- 预置数据：账号密码统一为 admin123 (BCrypt hash)
INSERT INTO sys_user(username, password, real_name, email, role, company) VALUES
('admin',        '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '系统管理员', 'a****@***********', 'ADMIN', NULL),
('hr_tencent',   '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '张HR',   'z***@tencent.com', 'HR', '腾讯'),
('hr_ali',       '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '李HR',   'l*@alibaba.com',  'HR', '阿里巴巴'),
('hr_bytedance', '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '王HR',   'w***@bytedance.com', 'HR', '字节跳动');

-- 演示岗位数据
INSERT INTO job_position(title, company_name, company_logo, department, description, requirements, status, category, location, salary, education, experience, publish_user_id) VALUES
('Java 后端开发工程师', '腾讯', '腾', '技术部', '负责后端核心系统开发', '3年以上Java经验', 'OPEN', 'SOCIAL', '深圳', '25-45K·16薪', '本科', '3-5年', 2),
('高级前端开发工程师', '阿里巴巴', '阿', '淘宝技术部', '负责淘宝前端开发', '5年前端经验，精通React', 'OPEN', 'SOCIAL', '杭州', '30-50K·16薪', '本科', '5-10年', 3),
('后端开发工程师（抖音）', '字节跳动', '字', '抖音服务端', '负责抖音推荐Feed后端', '3年以上Go/Java经验', 'OPEN', 'SOCIAL', '北京', '25-50K·15薪', '本科', '3-5年', 4),
('2027届校招 - Java开发工程师', '腾讯', '腾', '校招-技术岗', '面向2027届毕业生', '2027届本科及以上', 'OPEN', 'CAMPUS', '深圳', '18-28K·16薪', '本科', '应届生', 2),
('Java后端开发实习生', '腾讯', '腾', '技术部', '参与后端日常开发', '在校学生，熟悉Java', 'OPEN', 'INTERN', '深圳', '400-600/天', '本科', '实习生', 2);
