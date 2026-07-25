-- =============================================
-- Spider Media 数据库建表脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `spider_media` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `spider_media`;

-- =============================================
-- 用户表
-- =============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `user_name` VARCHAR(30) NOT NULL COMMENT '用户账号',
    `nick_name` VARCHAR(30) DEFAULT '' COMMENT '用户昵称',
    `email` VARCHAR(50) DEFAULT '' COMMENT '用户邮箱',
    `phonenumber` VARCHAR(11) DEFAULT '' COMMENT '手机号码',
    `avatar` VARCHAR(200) DEFAULT '' COMMENT '头像地址',
    `password` VARCHAR(100) DEFAULT '' COMMENT '密码',
    `status` CHAR(1) DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
    `role` VARCHAR(10) DEFAULT 'USER' COMMENT '角色（USER普通用户 ADMIN管理员）',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `idx_user_name` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 对标账号表
-- =============================================
DROP TABLE IF EXISTS `dc_target_account`;
CREATE TABLE `dc_target_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '账号ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `platform` VARCHAR(20) NOT NULL COMMENT '平台类型',
    `account_name` VARCHAR(100) NOT NULL COMMENT '账号名称',
    `account_id` VARCHAR(100) DEFAULT '' COMMENT '账号ID',
    `account_url` VARCHAR(500) DEFAULT '' COMMENT '账号链接',
    `group_name` VARCHAR(50) DEFAULT '' COMMENT '分组名称',
    `status` CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `description` TEXT COMMENT '描述',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_platform` (`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对标账号表';

-- =============================================
-- 采集文章表
-- =============================================
DROP TABLE IF EXISTS `dc_collected_article`;
CREATE TABLE `dc_collected_article` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_account_id` BIGINT NOT NULL COMMENT '对标账号ID',
    `platform` VARCHAR(20) NOT NULL COMMENT '平台类型',
    `title` VARCHAR(500) DEFAULT '' COMMENT '文章标题',
    `content` LONGTEXT COMMENT '文章内容',
    `summary` TEXT COMMENT '摘要',
    `url` VARCHAR(500) DEFAULT '' COMMENT '文章链接',
    `author` VARCHAR(100) DEFAULT '' COMMENT '作者',
    `view_count` INT DEFAULT 0 COMMENT '阅读量',
    `like_count` INT DEFAULT 0 COMMENT '点赞量',
    `comment_count` INT DEFAULT 0 COMMENT '评论量',
    `share_count` INT DEFAULT 0 COMMENT '分享量',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `collected_time` DATETIME DEFAULT NULL COMMENT '采集时间',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_target_account_id` (`target_account_id`),
    KEY `idx_platform` (`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采集文章表';

-- =============================================
-- 热点话题表
-- =============================================
DROP TABLE IF EXISTS `ac_hot_topic`;
CREATE TABLE `ac_hot_topic` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '话题ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `platform` VARCHAR(20) NOT NULL COMMENT '平台类型',
    `title` VARCHAR(500) NOT NULL COMMENT '话题标题',
    `description` TEXT COMMENT '话题描述',
    `hot_score` INT DEFAULT 0 COMMENT '热度值',
    `url` VARCHAR(500) DEFAULT '' COMMENT '链接',
    `category` VARCHAR(50) DEFAULT '' COMMENT '分类',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_platform` (`platform`),
    KEY `idx_hot_score` (`hot_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热点话题表';

-- =============================================
-- AI生成文章表
-- =============================================
DROP TABLE IF EXISTS `ac_generated_article`;
CREATE TABLE `ac_generated_article` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `hot_topic_id` BIGINT DEFAULT NULL COMMENT '热点话题ID',
    `title` VARCHAR(500) DEFAULT '' COMMENT '文章标题',
    `content` LONGTEXT COMMENT '文章内容',
    `summary` TEXT COMMENT '摘要',
    `model_used` VARCHAR(50) DEFAULT '' COMMENT '使用的模型',
    `word_count` INT DEFAULT 0 COMMENT '字数',
    `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI生成文章表';

-- =============================================
-- 平台账号表
-- =============================================
DROP TABLE IF EXISTS `pb_platform_account`;
CREATE TABLE `pb_platform_account` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '账号ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `platform` VARCHAR(20) NOT NULL COMMENT '平台类型',
    `account_name` VARCHAR(100) NOT NULL COMMENT '账号名称',
    `account_id` VARCHAR(100) DEFAULT '' COMMENT '账号ID',
    `access_token` TEXT COMMENT 'Access Token',
    `refresh_token` TEXT COMMENT 'Refresh Token',
    `token_expire_time` DATETIME DEFAULT NULL COMMENT 'Token过期时间',
    `status` CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `group_name` VARCHAR(50) DEFAULT '' COMMENT '分组名称',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_platform` (`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台账号表';

-- =============================================
-- 发布任务表
-- =============================================
DROP TABLE IF EXISTS `pb_publish_task`;
CREATE TABLE `pb_publish_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `platform_account_id` BIGINT NOT NULL COMMENT '平台账号ID',
    `article_id` BIGINT DEFAULT NULL COMMENT '文章ID',
    `platform` VARCHAR(20) NOT NULL COMMENT '平台类型',
    `title` VARCHAR(500) DEFAULT '' COMMENT '标题',
    `content` LONGTEXT COMMENT '内容',
    `summary` TEXT COMMENT '摘要',
    `cover_image` VARCHAR(500) DEFAULT '' COMMENT '封面图片',
    `status` INT DEFAULT 0 COMMENT '状态（0草稿 1发布中 2已发布 3失败）',
    `scheduled_time` DATETIME DEFAULT NULL COMMENT '定时发布时间',
    `published_time` DATETIME DEFAULT NULL COMMENT '实际发布时间',
    `publish_result` TEXT COMMENT '发布结果',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_platform` (`platform`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布任务表';

-- =============================================
-- 定时任务表
-- =============================================
DROP TABLE IF EXISTS `ts_scheduled_task`;
CREATE TABLE `ts_scheduled_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
    `task_type` VARCHAR(50) NOT NULL COMMENT '任务类型',
    `cron_expression` VARCHAR(50) DEFAULT '' COMMENT 'Cron表达式',
    `status` INT DEFAULT 0 COMMENT '状态（0停止 1运行中）',
    `last_run_time` DATETIME DEFAULT NULL COMMENT '上次执行时间',
    `next_run_time` DATETIME DEFAULT NULL COMMENT '下次执行时间',
    `run_count` INT DEFAULT 0 COMMENT '执行次数',
    `fail_count` INT DEFAULT 0 COMMENT '失败次数',
    `config` TEXT COMMENT '配置参数',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';

-- =============================================
-- 系统配置表
-- =============================================
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_name` VARCHAR(100) DEFAULT '' COMMENT '配置名称',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` CHAR(1) DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- =============================================
-- 操作日志表
-- =============================================
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `title` VARCHAR(50) DEFAULT '' COMMENT '模块标题',
    `business_type` INT DEFAULT 0 COMMENT '业务类型',
    `method` VARCHAR(200) DEFAULT '' COMMENT '方法名称',
    `request_method` VARCHAR(10) DEFAULT '' COMMENT '请求方式',
    `oper_name` VARCHAR(50) DEFAULT '' COMMENT '操作人员',
    `oper_url` VARCHAR(500) DEFAULT '' COMMENT '请求URL',
    `oper_ip` VARCHAR(128) DEFAULT '' COMMENT '主机地址',
    `oper_param` TEXT COMMENT '请求参数',
    `json_result` TEXT COMMENT '返回参数',
    `status` INT DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
    `error_msg` TEXT COMMENT '错误消息',
    `oper_time` DATETIME DEFAULT NULL COMMENT '操作时间',
    `cost_time` BIGINT DEFAULT 0 COMMENT '消耗时间',
    PRIMARY KEY (`id`),
    KEY `idx_oper_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志记录';

-- =============================================
-- 初始化数据
-- =============================================

-- 管理员用户 (密码: admin123)
INSERT INTO `sys_user` (`user_id`, `user_name`, `nick_name`, `password`, `status`, `role`, `create_by`, `create_time`)
VALUES (1, 'admin', '管理员', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', 'ADMIN', 'system', NOW());

-- 普通用户 (密码: user123)
INSERT INTO `sys_user` (`user_id`, `user_name`, `nick_name`, `password`, `status`, `role`, `create_by`, `create_time`)
VALUES (2, 'user', '普通用户', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', 'USER', 'system', NOW());
