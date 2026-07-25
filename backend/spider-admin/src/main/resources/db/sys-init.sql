-- =============================================
-- System Tables Initialization
-- =============================================

-- 1. Operation Log Table (操作日志表)
CREATE TABLE IF NOT EXISTS `sys_oper_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `username` VARCHAR(50) DEFAULT '' COMMENT '操作用户',
  `module` VARCHAR(50) DEFAULT '' COMMENT '操作模块',
  `action` VARCHAR(50) DEFAULT '' COMMENT '操作类型',
  `description` VARCHAR(500) DEFAULT '' COMMENT '操作描述',
  `ip` VARCHAR(128) DEFAULT '' COMMENT '操作IP地址',
  `method` VARCHAR(200) DEFAULT '' COMMENT '请求方法',
  `params` TEXT DEFAULT NULL COMMENT '请求参数',
  `status` INT DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` TEXT DEFAULT NULL COMMENT '错误消息',
  `create_time` DATETIME DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 2. System User Table (系统用户表)
CREATE TABLE IF NOT EXISTS `sys_user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_name` VARCHAR(50) NOT NULL COMMENT '用户账号',
  `nick_name` VARCHAR(50) DEFAULT '' COMMENT '用户昵称',
  `email` VARCHAR(100) DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` VARCHAR(20) DEFAULT '' COMMENT '手机号码',
  `avatar` VARCHAR(200) DEFAULT '' COMMENT '头像地址',
  `password` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '密码',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色（USER普通用户 ADMIN管理员）',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_name` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 3. System Config Table (系统配置表)
CREATE TABLE IF NOT EXISTS `sys_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '配置值',
  `config_type` CHAR(1) DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';
