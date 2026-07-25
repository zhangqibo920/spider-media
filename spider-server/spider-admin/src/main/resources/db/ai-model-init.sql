-- =============================================
-- AI Model Management Table
-- =============================================

CREATE TABLE IF NOT EXISTS `ai_model` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模型主键',
  `model_key` VARCHAR(100) NOT NULL COMMENT '模型唯一标识（如 deepseek-chat、glm-4）',
  `model_name` VARCHAR(100) NOT NULL COMMENT '模型显示名称',
  `provider` VARCHAR(50) NOT NULL COMMENT '模型提供方（deepseek、zhipu等）',
  `api_key` VARCHAR(500) NOT NULL DEFAULT '' COMMENT 'API密钥',
  `base_url` VARCHAR(500) NOT NULL COMMENT 'API基础地址',
  `enabled` CHAR(1) NOT NULL DEFAULT 'N' COMMENT '是否启用（Y启用 N禁用）',
  `sort_order` INT DEFAULT 0 COMMENT '排序序号',
  `test_status` VARCHAR(20) DEFAULT 'UNTESTED' COMMENT '测试状态（UNTESTED/TESTING/SUCCESS/FAILED）',
  `test_time` DATETIME DEFAULT NULL COMMENT '最近测试时间',
  `test_message` VARCHAR(500) DEFAULT NULL COMMENT '测试结果详情',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_model_key` (`model_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型管理表';

-- 初始化默认模型配置
INSERT INTO `ai_model` (`model_key`, `model_name`, `provider`, `api_key`, `base_url`, `enabled`, `sort_order`, `test_status`, `create_by`, `create_time`, `remark`) VALUES
('deepseek-chat', 'DeepSeek Chat', 'deepseek', '', 'https://api.deepseek.com', 'N', 1, 'UNTESTED', 'admin', NOW(), 'DeepSeek 通用对话模型，性价比高'),
('glm-4', '智谱 GLM-4', 'zhipu', '', 'https://open.bigmodel.cn/api/paas/v4', 'N', 2, 'UNTESTED', 'admin', NOW(), '智谱 AI 通用对话模型');
