-- H2 内存数据库测试用建表脚本（MySQL 兼容模式）
-- 不含 USE/SET NAMES/ENGINE 等 MySQL 专有语法

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  username   VARCHAR(50)  NOT NULL,
  password   VARCHAR(100) NOT NULL,
  real_name  VARCHAR(50)           DEFAULT NULL,
  role       VARCHAR(20)  NOT NULL DEFAULT 'ADMIN',
  created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE (username)
);

DROP TABLE IF EXISTS job_position;
CREATE TABLE job_position (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  title        VARCHAR(100) NOT NULL,
  department   VARCHAR(50)           DEFAULT NULL,
  description  TEXT                  DEFAULT NULL,
  requirements TEXT                  DEFAULT NULL,
  status       VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
  created_at   TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted      TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS candidate;
CREATE TABLE candidate (
  id               BIGINT       NOT NULL AUTO_INCREMENT,
  name             VARCHAR(50)  NOT NULL,
  email            VARCHAR(100)          DEFAULT NULL,
  phone            VARCHAR(20)           DEFAULT NULL,
  skills           VARCHAR(500)          DEFAULT NULL,
  experience_years INT                   DEFAULT NULL,
  resume_text      TEXT                  DEFAULT NULL,
  created_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted          TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS application;
CREATE TABLE application (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  candidate_id BIGINT       NOT NULL,
  position_id  BIGINT       NOT NULL,
  status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
  cover_letter TEXT                  DEFAULT NULL,
  created_at   TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
  deleted      TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS screen_rule;
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

DROP TABLE IF EXISTS screen_result;
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

-- 预置数据：3 个账号
INSERT INTO sys_user(username, password, real_name, role) VALUES
('admin', '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '系统管理员', 'ADMIN'),
('hr', '$2a$10$5hzJix20.M044Zx7cFOwTuAY6Ud3n8NRDOFwsjTrYKhQUq9lwOroe', 'HR 专员', 'HR'),
('viewer', '$2a$10$7pEEeVKQ29fNqg6pseP1OOTYA0zyaXeDItezH5G6I1J95ttllrbF.', '只读用户', 'VIEWER');

-- 预置数据：2 个职位
INSERT INTO job_position(title, department, description, requirements, status) VALUES
('Java 后端工程师', '技术部', '负责招聘系统后端开发，参与系统设计、编码与上线', '3年以上 Java；熟悉 SpringBoot、MySQL', 'OPEN'),
('前端工程师', '技术部', '负责管理后台前端开发', '2年以上前端；熟悉 Vue 或 React', 'OPEN');

-- 预置数据：2 个候选人
INSERT INTO candidate(name, email, phone, skills, experience_years, resume_text) VALUES
('张三', 'zhangsan@example.com', '13800000001', 'Java,SpringBoot,MySQL,Redis', 4, '5 年 Java 后端开发经验，熟悉 Spring 生态'),
('李四', 'lisi@example.com', '13800000002', 'Vue,JavaScript,HTML,CSS', 3, '3 年前端开发经验，熟悉 Vue 框架');

-- 预置数据：1 个投递记录
INSERT INTO application(candidate_id, position_id, status, cover_letter) VALUES
(1, 1, 'PENDING', '对贵司 Java 后端岗位非常感兴趣');

-- 预置数据：4 条筛选规则
INSERT INTO screen_rule(name, rule_type, target_field, expected_values, match_mode, weight, enabled, position_id) VALUES
('核心技能匹配', 'SKILL', 'skills', 'Java,SpringBoot,MySQL', 'ANY', 30, 1, 1),
('Redis 加分项', 'SKILL', 'skills', 'Redis', 'ANY', 10, 1, 1),
('经验年限', 'EXPERIENCE', 'experience_years', '3', 'MIN', 20, 1, 1),
('简历关键词', 'KEYWORD', 'resume_text', 'Spring,后端', 'ANY', 20, 1, 1);
