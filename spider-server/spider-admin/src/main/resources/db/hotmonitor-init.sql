-- =============================================
-- 热点监控模块增量 SQL（针对已有数据库执行）
-- 执行前请先确认 spider_media 数据库已存在
-- =============================================

-- 0. 修复 AI创作 菜单类型（原为 C，需改为 M 以支持子菜单）
UPDATE `sys_menu` SET `menu_type` = 'M', `component` = '' WHERE `menu_id` = 3;

-- 0a. 重建 AI创作 下的子菜单（先删后插，保证幂等）
DELETE FROM `sys_role_menu` WHERE `menu_id` IN (15, 16, 17);
DELETE FROM `sys_menu` WHERE `menu_id` IN (15, 16, 17);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `visible`, `create_by`, `create_time`) VALUES
(15, '热点抓取',   3, 'fetch',    'aicreation/AiCreationView',    'TrendCharts', 0, 'C', '0', 'system', NOW()),
(16, '关键词管理', 3, 'keywords', 'hotmonitor/KeywordManageView', 'Search',       1, 'C', '0', 'system', NOW()),
(17, '热点信息流', 3, 'hot-feed', 'hotmonitor/HotFeedView',       'TrendCharts', 2, 'C', '0', 'system', NOW());

-- 1. 监控关键词表
DROP TABLE IF EXISTS `hm_keyword`;
CREATE TABLE `hm_keyword` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '关键词ID',
    `user_id`         BIGINT       NOT NULL COMMENT '用户ID',
    `keyword`         VARCHAR(100) NOT NULL COMMENT '监控关键词',
    `status`          CHAR(1)      DEFAULT '0' COMMENT '0=激活 1=暂停',
    `interval_min`    INT          DEFAULT 30 COMMENT '抓取间隔(分钟)',
    `notify_email`    CHAR(1)      DEFAULT '0' COMMENT '是否邮件通知 0=否 1=是',
    `notify_email_addr` VARCHAR(100) DEFAULT '' COMMENT '通知邮箱地址',
    `notify_site`     CHAR(1)      DEFAULT '1' COMMENT '是否站内通知 0=否 1=是',
    `sources`         VARCHAR(200) DEFAULT '' COMMENT '抓取源列表(逗号分隔,空=全部)',
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

-- 2. 站内通知表
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

-- 3. 热点话题表扩展字段（用 information_schema 检测列是否存在，避免重复执行报错）
--   执行前请确认 ac_hot_topic 表已存在
SET @col_exists = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
                   WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'ac_hot_topic' AND `COLUMN_NAME` = 'keyword_id');
SET @s = IF(@col_exists = 0,
    'ALTER TABLE `ac_hot_topic` ADD COLUMN `keyword_id` BIGINT DEFAULT NULL COMMENT ''关联关键词ID'' AFTER `category`',
    'SELECT 1');
PREPARE `stmt` FROM @s; EXECUTE `stmt`; DEALLOCATE PREPARE `stmt`;

SET @col_exists = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
                   WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'ac_hot_topic' AND `COLUMN_NAME` = 'source');
SET @s = IF(@col_exists = 0,
    'ALTER TABLE `ac_hot_topic` ADD COLUMN `source` VARCHAR(50) DEFAULT '''' COMMENT ''来源标识'' AFTER `keyword_id`',
    'SELECT 1');
PREPARE `stmt` FROM @s; EXECUTE `stmt`; DEALLOCATE PREPARE `stmt`;

SET @col_exists = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
                   WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'ac_hot_topic' AND `COLUMN_NAME` = 'ai_score');
SET @s = IF(@col_exists = 0,
    'ALTER TABLE `ac_hot_topic` ADD COLUMN `ai_score` TINYINT DEFAULT NULL COMMENT ''AI重要性评分1-5'' AFTER `source`',
    'SELECT 1');
PREPARE `stmt` FROM @s; EXECUTE `stmt`; DEALLOCATE PREPARE `stmt`;

SET @col_exists = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
                   WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'ac_hot_topic' AND `COLUMN_NAME` = 'ai_summary');
SET @s = IF(@col_exists = 0,
    'ALTER TABLE `ac_hot_topic` ADD COLUMN `ai_summary` VARCHAR(500) DEFAULT '''' COMMENT ''AI智能摘要'' AFTER `ai_score`',
    'SELECT 1');
PREPARE `stmt` FROM @s; EXECUTE `stmt`; DEALLOCATE PREPARE `stmt`;

SET @col_exists = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
                   WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'ac_hot_topic' AND `COLUMN_NAME` = 'ai_verified');
SET @s = IF(@col_exists = 0,
    'ALTER TABLE `ac_hot_topic` ADD COLUMN `ai_verified` CHAR(1) DEFAULT ''0'' COMMENT ''AI真假判定 0=未验证 1=真实 2=可疑 3=虚假'' AFTER `ai_summary`',
    'SELECT 1');
PREPARE `stmt` FROM @s; EXECUTE `stmt`; DEALLOCATE PREPARE `stmt`;

SET @col_exists = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
                   WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'ac_hot_topic' AND `COLUMN_NAME` = 'relevance');
SET @s = IF(@col_exists = 0,
    'ALTER TABLE `ac_hot_topic` ADD COLUMN `relevance` TINYINT DEFAULT NULL COMMENT ''与关键词相关性0-100'' AFTER `ai_verified`',
    'SELECT 1');
PREPARE `stmt` FROM @s; EXECUTE `stmt`; DEALLOCATE PREPARE `stmt`;

-- hm_keyword 列安全新增
SET @col_exists = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
                   WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'hm_keyword' AND `COLUMN_NAME` = 'notify_email_addr');
SET @s = IF(@col_exists = 0,
    'ALTER TABLE `hm_keyword` ADD COLUMN `notify_email_addr` VARCHAR(100) DEFAULT '''' COMMENT ''通知邮箱地址'' AFTER `notify_email`',
    'SELECT 1');
PREPARE `stmt` FROM @s; EXECUTE `stmt`; DEALLOCATE PREPARE `stmt`;

SET @col_exists = (SELECT COUNT(*) FROM `information_schema`.`COLUMNS`
                   WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'hm_keyword' AND `COLUMN_NAME` = 'sources');
SET @s = IF(@col_exists = 0,
    'ALTER TABLE `hm_keyword` ADD COLUMN `sources` VARCHAR(200) DEFAULT '''' COMMENT ''抓取源列表(逗号分隔,空=全部)'' AFTER `notify_site`',
    'SELECT 1');
PREPARE `stmt` FROM @s; EXECUTE `stmt`; DEALLOCATE PREPARE `stmt`;

-- 索引安全检测
SET @idx_exists = (SELECT COUNT(*) FROM `information_schema`.`STATISTICS`
                   WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = 'ac_hot_topic' AND `INDEX_NAME` = 'idx_keyword_id');
SET @s = IF(@idx_exists = 0,
    'ALTER TABLE `ac_hot_topic` ADD INDEX `idx_keyword_id` (`keyword_id`)',
    'SELECT 1');
PREPARE `stmt` FROM @s; EXECUTE `stmt`; DEALLOCATE PREPARE `stmt`;

-- 4. 角色-菜单关联（先清理旧关联，再添加新关联）
DELETE FROM `sys_role_menu` WHERE `menu_id` IN (15, 16, 17);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 15), (1, 16), (1, 17),
(2, 15), (2, 16), (2, 17);

-- 4a. 热点平台字典新增4源
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'hot_topic_platform' AND `dict_value` IN ('baidu','bilibili','hackernews','github');
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `remark`, `del_flag`, `create_time`) VALUES
(5, '百度',       'baidu',      'hot_topic_platform', '', '0', NOW()),
(6, 'B站',        'bilibili',   'hot_topic_platform', '', '0', NOW()),
(7, 'HackerNews', 'hackernews', 'hot_topic_platform', '', '0', NOW()),
(8, 'GitHub',     'github',     'hot_topic_platform', '', '0', NOW());

-- 5. SMTP 邮件配置（先删后插，保证幂等）
DELETE FROM `sys_config` WHERE `config_key` LIKE 'smtp.%';
INSERT INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`) VALUES
('SMTP 服务器',   'smtp.host',     '', 'Y', 'system', NOW()),
('SMTP 端口',     'smtp.port',     '587', 'Y', 'system', NOW()),
('SMTP 用户名',   'smtp.username', '', 'Y', 'system', NOW()),
('SMTP 密码',     'smtp.password', '', 'Y', 'system', NOW()),
('发件人地址',    'smtp.from',     '', 'Y', 'system', NOW());
