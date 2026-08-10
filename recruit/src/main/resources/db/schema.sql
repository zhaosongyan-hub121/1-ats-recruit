-- =========================================================
-- recruit 招聘后端 数据库初始化脚本
-- 使用前请先创建数据库：CREATE DATABASE recruit DEFAULT CHARACTER SET utf8mb4;
-- =========================================================
USE recruit;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 系统用户表（管理员/面试官）
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `username`   VARCHAR(50)  NOT NULL COMMENT '登录账号',
  `password`   VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密后密码',
  `real_name`  VARCHAR(50)           DEFAULT NULL COMMENT '真实姓名',
  `role`       VARCHAR(20)  NOT NULL DEFAULT 'ADMIN' COMMENT 'ADMIN/HR/VIEWER',
  `created_at` DATETIME              DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- ----------------------------
-- 职位表（job_position 规避 MySQL 关键字 position）
-- ----------------------------
DROP TABLE IF EXISTS `job_position`;
CREATE TABLE `job_position` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `title`        VARCHAR(100) NOT NULL COMMENT '职位标题',
  `department`   VARCHAR(50)           DEFAULT NULL COMMENT '部门',
  `description`  TEXT                  DEFAULT NULL COMMENT '职位描述',
  `requirements` TEXT                  DEFAULT NULL COMMENT '任职要求',
  `status`       VARCHAR(20)  NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN/CLOSED',
  `created_at`   DATETIME              DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_title` (`title`),
  KEY `idx_department` (`department`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位';

-- ----------------------------
-- 候选人表
-- ----------------------------
DROP TABLE IF EXISTS `candidate`;
CREATE TABLE `candidate` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `name`             VARCHAR(50)  NOT NULL COMMENT '姓名',
  `email`            VARCHAR(100)          DEFAULT NULL,
  `phone`            VARCHAR(20)           DEFAULT NULL,
  `skills`           VARCHAR(500)          DEFAULT NULL COMMENT '逗号分隔技能列表，如 Java,Spring,MySQL',
  `experience_years` INT                   DEFAULT NULL COMMENT '工作年限',
  `resume_text`      TEXT                  DEFAULT NULL COMMENT '简历纯文本（用于关键词搜索）',
  `created_at`       DATETIME              DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`          TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候选人';

-- ----------------------------
-- 投递记录表
-- ----------------------------
DROP TABLE IF EXISTS `application`;
CREATE TABLE `application` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `candidate_id` BIGINT       NOT NULL,
  `position_id`  BIGINT       NOT NULL,
  `status`       VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/REVIEWED/ACCEPTED/REJECTED',
  `cover_letter` TEXT                  DEFAULT NULL COMMENT '求职信',
  `created_at`   DATETIME              DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_candidate` (`candidate_id`),
  KEY `idx_position` (`position_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投递记录';

-- ----------------------------
-- 初始化管理员账号
-- 用户名：admin  密码：admin123（BCrypt 加密）
-- ----------------------------
INSERT INTO `sys_user`(`username`, `password`, `real_name`, `role`)
VALUES ('admin', '$2a$10$G6TK1rmGjHLVNz0TO1E0IeONy/Du0.l4O00dv4hCi92DJUgV4y..a', '系统管理员', 'ADMIN');

-- HR 演示账号：hr / hr123456
INSERT INTO `sys_user`(`username`, `password`, `real_name`, `role`)
VALUES ('hr', '$2a$10$5hzJix20.M044Zx7cFOwTuAY6Ud3n8NRDOFwsjTrYKhQUq9lwOroe', 'HR 专员', 'HR');

-- 只读演示账号：viewer / viewer123
INSERT INTO `sys_user`(`username`, `password`, `real_name`, `role`)
VALUES ('viewer', '$2a$10$7pEEeVKQ29fNqg6pseP1OOTYA0zyaXeDItezH5G6I1J95ttllrbF.', '只读用户', 'VIEWER');

-- 演示数据：2 个职位 + 2 个候选人 + 1 个投递
INSERT INTO `job_position`(`title`, `department`, `description`, `requirements`, `status`) VALUES
('Java 后端工程师', '技术部', '负责招聘系统后端开发，参与系统设计、编码与上线', '3年以上 Java；熟悉 SpringBoot、MySQL', 'OPEN'),
('前端工程师', '技术部', '负责管理后台前端开发', '2年以上前端；熟悉 Vue 或 React', 'OPEN');

INSERT INTO `candidate`(`name`, `email`, `phone`, `skills`, `experience_years`, `resume_text`) VALUES
('张三', 'zhangsan@example.com', '13800000001', 'Java,SpringBoot,MySQL,Redis', 4, '5 年 Java 后端开发经验，熟悉 Spring 生态'),
('李四', 'lisi@example.com', '13800000002', 'Vue,JavaScript,HTML,CSS', 3, '3 年前端开发经验，熟悉 Vue 框架');

INSERT INTO `application`(`candidate_id`, `position_id`, `status`, `cover_letter`) VALUES
(1, 1, 'PENDING', '对贵司 Java 后端岗位非常感兴趣');

-- ----------------------------
-- 筛选规则表
-- ----------------------------
DROP TABLE IF EXISTS `screen_rule`;
CREATE TABLE `screen_rule` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `name`         VARCHAR(100) NOT NULL COMMENT '规则名称',
  `rule_type`    VARCHAR(30)  NOT NULL COMMENT 'KEYWORD/SKILL/EXPERIENCE',
  `target_field` VARCHAR(30)  NOT NULL COMMENT '简历字段：skills/resume_text/experience_years',
  `expected_values` VARCHAR(500) NOT NULL COMMENT '期望值，逗号分隔',
  `match_mode`   VARCHAR(20)  NOT NULL DEFAULT 'ANY' COMMENT 'ANY=匹配任一即可/ALL=必须全匹配',
  `weight`       INT          NOT NULL DEFAULT 10 COMMENT '匹配得分权重',
  `enabled`      TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用',
  `position_id`  BIGINT                DEFAULT NULL COMMENT '绑定职位（空=通用规则）',
  `created_at`   DATETIME              DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_position` (`position_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筛选规则';

-- ----------------------------
-- 筛选结果表
-- ----------------------------
DROP TABLE IF EXISTS `screen_result`;
CREATE TABLE `screen_result` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT,
  `application_id` BIGINT    NOT NULL,
  `total_score`   INT         NOT NULL DEFAULT 0 COMMENT '总得分',
  `max_score`     INT         NOT NULL DEFAULT 0 COMMENT '满分',
  `pass`          TINYINT     NOT NULL DEFAULT 0 COMMENT '是否通过',
  `rule_details`  TEXT                 DEFAULT NULL COMMENT '各规则匹配详情 JSON',
  `created_at`    DATETIME             DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_application` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='筛选结果';

-- 初始化筛选规则（Java 后端岗位通用规则）
INSERT INTO `screen_rule`(`name`, `rule_type`, `target_field`, `expected_values`, `match_mode`, `weight`, `enabled`, `position_id`) VALUES
('核心技能匹配', 'SKILL', 'skills', 'Java,SpringBoot,MySQL', 'ANY', 30, 1, 1),
('Redis 加分项', 'SKILL', 'skills', 'Redis', 'ANY', 10, 1, 1),
('经验年限', 'EXPERIENCE', 'experience_years', '3', 'MIN', 20, 1, 1),
('简历关键词', 'KEYWORD', 'resume_text', 'Spring,后端', 'ANY', 20, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
