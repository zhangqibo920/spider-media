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
-- 监控关键词表
-- =============================================
DROP TABLE IF EXISTS `hm_keyword`;
CREATE TABLE `hm_keyword` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '关键词ID',
    `user_id`         BIGINT       NOT NULL COMMENT '用户ID',
    `keyword`         VARCHAR(100) NOT NULL COMMENT '监控关键词',
    `status`          CHAR(1)      DEFAULT '0' COMMENT '0=激活 1=暂停',
    `interval_min`    INT          DEFAULT 30 COMMENT '抓取间隔(分钟)',
    `notify_email`    CHAR(1)      DEFAULT '0' COMMENT '是否邮件通知 0=否 1=是',
    `notify_site`     CHAR(1)      DEFAULT '1' COMMENT '是否站内通知 0=否 1=是',
    `last_fetch_time` DATETIME     DEFAULT NULL COMMENT '上次抓取时间',
    `del_flag`        CHAR(1)      DEFAULT '0' COMMENT '删除标志',
    `create_by`       VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time`     DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`       VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time`     DATETIME     DEFAULT NULL COMMENT '更新时间',
    `remark`          VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控关键词表';

-- =============================================
-- 站内通知表
-- =============================================
DROP TABLE IF EXISTS `hm_notification`;
CREATE TABLE `hm_notification` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id`      BIGINT       NOT NULL COMMENT '接收用户ID',
    `title`        VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content`      TEXT         COMMENT '通知内容',
    `type`         VARCHAR(20)  DEFAULT 'HOT' COMMENT '类型 HOT=热点通知',
    `is_read`      CHAR(1)      DEFAULT '0' COMMENT '0=未读 1=已读',
    `hot_topic_id` BIGINT       DEFAULT NULL COMMENT '关联热点话题ID',
    `del_flag`     CHAR(1)      DEFAULT '0' COMMENT '删除标志',
    `create_by`    VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time`  DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_by`    VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time`  DATETIME     DEFAULT NULL COMMENT '更新时间',
    `remark`       VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`id`),
    KEY `idx_user_unread` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知表';

-- =============================================
-- 热点话题表扩展字段
-- =============================================
ALTER TABLE `ac_hot_topic`
    ADD COLUMN `keyword_id`    BIGINT       DEFAULT NULL COMMENT '关联关键词ID' AFTER `category`,
    ADD COLUMN `source`        VARCHAR(50)  DEFAULT '' COMMENT '来源标识' AFTER `keyword_id`,
    ADD COLUMN `ai_score`      TINYINT      DEFAULT NULL COMMENT 'AI重要性评分1-5' AFTER `source`,
    ADD COLUMN `ai_summary`    VARCHAR(500) DEFAULT '' COMMENT 'AI智能摘要' AFTER `ai_score`,
    ADD COLUMN `ai_verified`   CHAR(1)      DEFAULT '0' COMMENT 'AI真假判定 0=未验证 1=真实 2=可疑 3=虚假' AFTER `ai_summary`,
    ADD COLUMN `relevance`     TINYINT      DEFAULT NULL COMMENT '与关键词相关性0-100' AFTER `ai_verified`,
    ADD INDEX `idx_keyword_id` (`keyword_id`);

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
-- 菜单表（树形结构）
-- =============================================
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `menu_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID（0表示顶级菜单）',
    `path` VARCHAR(200) DEFAULT '' COMMENT '路由地址',
    `component` VARCHAR(200) DEFAULT '' COMMENT '组件路径（前端视图路径）',
    `perms` VARCHAR(100) DEFAULT '' COMMENT '权限标识',
    `icon` VARCHAR(50) DEFAULT '' COMMENT '菜单图标（Element Plus图标名）',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `menu_type` CHAR(1) DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
    `status` CHAR(1) DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
    `visible` CHAR(1) DEFAULT '0' COMMENT '显示状态（0显示 1隐藏）',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`menu_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- =============================================
-- 角色表
-- =============================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `role_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name` VARCHAR(30) NOT NULL COMMENT '角色名称',
    `role_key` VARCHAR(20) NOT NULL COMMENT '角色权限字符串（如 ADMIN, USER, EDITOR）',
    `status` CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `remark` VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (`role_id`),
    UNIQUE KEY `idx_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- =============================================
-- 角色-菜单关联表
-- =============================================
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单关联表';

-- =============================================
-- 用户-角色关联表
-- =============================================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

-- =============================================
-- 初始化数据
-- =============================================

-- 管理员用户 (密码: admin123)
INSERT INTO `sys_user` (`user_id`, `user_name`, `nick_name`, `password`, `status`, `role`, `create_by`, `create_time`)
VALUES (1, 'admin', '管理员', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', 'ADMIN', 'system', NOW());

-- 普通用户 (密码: user123)
INSERT INTO `sys_user` (`user_id`, `user_name`, `nick_name`, `password`, `status`, `role`, `create_by`, `create_time`)
VALUES (2, 'user', '普通用户', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', 'USER', 'system', NOW());

-- 初始化角色
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `status`, `create_by`, `create_time`) VALUES
(1, '管理员', 'ADMIN', '0', 'system', NOW()),
(2, '普通用户', 'USER', '0', 'system', NOW());

-- 关联用户-角色（向后兼容现有 sys_user.role 字段）
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1), (2, 2);

-- 初始化菜单（ADMIN 可见所有菜单，USER 可见非管理菜单）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `path`, `component`, `perms`, `icon`, `sort_order`, `menu_type`, `visible`, `create_by`, `create_time`) VALUES
(1, '工作台',     0, '/dashboard',   'dashboard/DashboardView',   '',  'Odometer',  1, 'C', '0', 'system', NOW()),
(2, '数据采集',   0, '/collection',  'datacollection/CollectionView', '', 'Download',  2, 'C', '0', 'system', NOW()),
(3, 'AI创作',    0, '/ai-creation', 'aicreation/AiCreationView', '', 'MagicStick', 3, 'C', '0', 'system', NOW()),
(4, '内容发布',   0, '/publish',     'contentpublish/PublishView', '', 'Promotion',  4, 'C', '0', 'system', NOW()),
(5, '任务调度',   0, '/scheduler',   'taskscheduler/SchedulerView', '', 'Timer',      5, 'C', '0', 'system', NOW()),
(6, '系统管理',   0, '/admin',       '',                      '', 'Setting',    6, 'M', '0', 'system', NOW()),
(7, '个人中心',   0, '/profile',     'userauth/ProfileView',  '', 'User',       7, 'C', '1', 'system', NOW()),
(8,  '系统配置', 6, 'config',  'systemadmin/SysConfigView',   '', 'Setting',        1, 'C', '0', 'system', NOW()),
(9,  '用户管理', 6, 'users',   'systemadmin/AdminUsersView',  '', 'User',           2, 'C', '0', 'system', NOW()),
(10, '操作日志', 6, 'logs',    'systemadmin/AdminLogsView',   '', 'Document',       3, 'C', '0', 'system', NOW()),
(11, '模型管理', 6, 'models',  'systemadmin/ModelManageView', '', 'Cpu',            4, 'C', '0', 'system', NOW()),
(12, '字典管理', 6, 'dict',    'systemadmin/DictManageView',  '', 'List',           5, 'C', '0', 'system', NOW()),
(13, '菜单管理', 6, 'menus',   'systemadmin/MenuManageView',  '', 'Menu',           6, 'C', '0', 'system', NOW()),
(14, '角色管理', 6, 'roles',   'systemadmin/RoleManageView',  '', 'UserFilled',     7, 'C', '0', 'system', NOW()),
-- 热点监控（属于 AI创作 的子菜单，menu_id=3）
(15, '关键词管理', 3, 'keywords', 'hotmonitor/KeywordManageView', '', 'Search', 1, 'C', '0', 'system', NOW()),
(16, '热点信息流', 3, 'hot-feed', 'hotmonitor/HotFeedView',       '', 'TrendCharts', 2, 'C', '0', 'system', NOW());

-- 分配角色-菜单（ADMIN 拥有所有菜单，USER 排除系统管理）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7),
(1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14),
(1, 15), (1, 16),
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 7), (2, 15), (2, 16);
