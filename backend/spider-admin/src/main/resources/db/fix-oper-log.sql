-- =============================================
-- Fix sys_oper_log table: add missing columns
-- Run this SQL in your MySQL database
-- =============================================

-- Check current table structure first:
-- DESCRIBE sys_oper_log;

-- Drop and recreate with correct schema (WARNING: this will delete existing data)
-- If you have existing data, use ALTER TABLE statements below instead

-- Option 1: Drop and recreate (loses existing data)
DROP TABLE IF EXISTS `sys_oper_log`;

CREATE TABLE `sys_oper_log` (
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
