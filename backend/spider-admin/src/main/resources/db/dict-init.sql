-- =============================================
-- RuoYi-style Dictionary System Initialization
-- =============================================

-- 1. Dictionary Type Table (字典类型表)
CREATE TABLE IF NOT EXISTS `sys_dict_type` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典类型主键',
  `dict_name` VARCHAR(100) NOT NULL COMMENT '字典类型名称',
  `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型唯一标识',
  `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- 2. Dictionary Data Table (字典数据表)
CREATE TABLE IF NOT EXISTS `sys_dict_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典数据主键',
  `dict_sort` INT NOT NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签',
  `dict_value` VARCHAR(100) NOT NULL COMMENT '字典键值',
  `dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型',
  `css_class` VARCHAR(100) DEFAULT NULL COMMENT '样式属性（Element Plus Tag type）',
  `list_class` VARCHAR(100) DEFAULT NULL COMMENT '表格回显样式',
  `is_default` CHAR(1) DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- =============================================
-- 3. Initialize Dictionary Types
-- =============================================

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES
('用户状态', 'sys_user_status', '0', 'admin', NOW(), '用户账号状态'),
('用户角色', 'sys_user_role', '0', 'admin', NOW(), '用户角色类型'),
('系统配置类型', 'sys_config_type', '0', 'admin', NOW(), '系统配置类型'),
('发布任务状态', 'pb_publish_status', '0', 'admin', NOW(), '内容发布任务状态'),
('发布账号状态', 'pb_account_status', '0', 'admin', NOW(), '发布平台账号状态'),
('对标账号状态', 'dc_account_status', '0', 'admin', NOW(), '数据采集对标账号状态'),
('定时任务状态', 'ts_task_status', '0', 'admin', NOW(), '定时任务运行状态'),
('AI文章状态', 'ac_article_status', '0', 'admin', NOW(), 'AI生成文章状态'),
('热点平台', 'hot_topic_platform', '0', 'admin', NOW(), '热点话题来源平台'),
('发布平台', 'publish_platform', '0', 'admin', NOW(), '内容发布目标平台'),
('采集平台', 'collection_platform', '0', 'admin', NOW(), '数据采集来源平台');

-- =============================================
-- 4. Initialize Dictionary Data
-- =============================================

-- 用户状态
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `status`, `create_time`) VALUES
(1, '正常', '0', 'sys_user_status', 'success', '0', NOW()),
(2, '停用', '1', 'sys_user_status', 'danger', '0', NOW());

-- 用户角色
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `status`, `create_time`) VALUES
(1, '普通用户', 'USER', 'sys_user_role', 'info', '0', NOW()),
(2, '管理员', 'ADMIN', 'sys_user_role', 'danger', '0', NOW());

-- 系统配置类型
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `status`, `create_time`) VALUES
(1, '内置', 'Y', 'sys_config_type', 'danger', '0', NOW()),
(2, '自定义', 'N', 'sys_config_type', 'info', '0', NOW());

-- 发布任务状态
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `status`, `create_time`) VALUES
(1, '草稿', '0', 'pb_publish_status', 'info', '0', NOW()),
(2, '发布中', '1', 'pb_publish_status', 'warning', '0', NOW()),
(3, '已发布', '2', 'pb_publish_status', 'success', '0', NOW()),
(4, '失败', '3', 'pb_publish_status', 'danger', '0', NOW());

-- 发布账号状态
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `status`, `create_time`) VALUES
(1, '在线', '0', 'pb_account_status', 'success', '0', NOW()),
(2, '离线', '1', 'pb_account_status', 'info', '0', NOW());

-- 对标账号状态
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `status`, `create_time`) VALUES
(1, '监控中', '0', 'dc_account_status', 'success', '0', NOW()),
(2, '已暂停', '1', 'dc_account_status', 'info', '0', NOW());

-- 定时任务状态
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `status`, `create_time`) VALUES
(1, '已停止', '0', 'ts_task_status', 'info', '0', NOW()),
(2, '运行中', '1', 'ts_task_status', 'success', '0', NOW());

-- AI文章状态
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `status`, `create_time`) VALUES
(1, '生成中', 'GENERATING', 'ac_article_status', 'warning', '0', NOW()),
(2, '已完成', 'COMPLETED', 'ac_article_status', 'success', '0', NOW()),
(3, '失败', 'FAILED', 'ac_article_status', 'danger', '0', NOW());

-- 热点平台
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `status`, `create_time`) VALUES
(1, '微博', 'weibo', 'hot_topic_platform', '', '0', NOW()),
(2, '抖音', 'douyin', 'hot_topic_platform', '', '0', NOW()),
(3, '知乎', 'zhihu', 'hot_topic_platform', '', '0', NOW()),
(4, '头条', 'toutiao', 'hot_topic_platform', '', '0', NOW());

-- 发布平台
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `status`, `create_time`) VALUES
(1, '微信公众号', 'wechat', 'publish_platform', '', '0', NOW()),
(2, '今日头条', 'toutiao', 'publish_platform', '', '0', NOW()),
(3, '百家号', 'baijia', 'publish_platform', '', '0', NOW()),
(4, '小红书', 'xiaohongshu', 'publish_platform', '', '0', NOW()),
(5, '抖音', 'douyin', 'publish_platform', '', '0', NOW());

-- 采集平台
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `status`, `create_time`) VALUES
(1, '微信公众号', 'wechat', 'collection_platform', '', '0', NOW()),
(2, '百家号', 'baijia', 'collection_platform', '', '0', NOW()),
(3, '头条号', 'toutiao', 'collection_platform', '', '0', NOW()),
(4, '小红书', 'xiaohongshu', 'collection_platform', '', '0', NOW()),
(5, '抖音', 'douyin', 'collection_platform', '', '0', NOW()),
(6, '知乎', 'zhihu', 'collection_platform', '', '0', NOW());
