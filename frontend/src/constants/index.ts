/**
 * Status dictionary - centralized status definitions for the entire application
 *
 * Each status group provides:
 * - A map from status code to display label
 * - A map from status code to Element Plus tag type
 * - A helper function to get label or tag type by code
 */

// ========================
// Target Account Status (dc_target_account.status: String '0'/'1')
// ========================
export const TARGET_ACCOUNT_STATUS = {
  labels: { '0': '监控中', '1': '已暂停' } as Record<string, string>,
  types: { '0': 'success', '1': 'info' } as Record<string, string>,
  NORMAL: '0',
  DISABLED: '1',
} as const

export function getTargetAccountStatusLabel(status: string): string {
  return TARGET_ACCOUNT_STATUS.labels[status] ?? '未知'
}
export function getTargetAccountStatusType(status: string): string {
  return TARGET_ACCOUNT_STATUS.types[status] ?? 'info'
}

// ========================
// Publish Task Status (pb_publish_task.status: Integer 0-3)
// ========================
export const PUBLISH_TASK_STATUS = {
  labels: { 0: '草稿', 1: '发布中', 2: '已发布', 3: '失败' } as Record<number, string>,
  types: { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' } as Record<number, string>,
  DRAFT: 0,
  PUBLISHING: 1,
  PUBLISHED: 2,
  FAILED: 3,
} as const

export function getPublishTaskStatusLabel(status: number): string {
  return PUBLISH_TASK_STATUS.labels[status] ?? '未知'
}
export function getPublishTaskStatusType(status: number): string {
  return PUBLISH_TASK_STATUS.types[status] ?? 'info'
}

// ========================
// Platform Account Status (pb_platform_account.status: String '0'/'1')
// ========================
export const PLATFORM_ACCOUNT_STATUS = {
  labels: { '0': '在线', '1': '离线' } as Record<string, string>,
  types: { '0': 'success', '1': 'info' } as Record<string, string>,
  ONLINE: '0',
  OFFLINE: '1',
} as const

export function getPlatformAccountStatusLabel(status: string): string {
  return PLATFORM_ACCOUNT_STATUS.labels[status] ?? '未知'
}
export function getPlatformAccountStatusType(status: string): string {
  return PLATFORM_ACCOUNT_STATUS.types[status] ?? 'info'
}

// ========================
// Scheduled Task Status (ts_scheduled_task.status: Integer 0/1)
// ========================
export const SCHEDULED_TASK_STATUS = {
  labels: { 0: '已停止', 1: '运行中' } as Record<number, string>,
  types: { 0: 'info', 1: 'success' } as Record<number, string>,
  STOPPED: 0,
  RUNNING: 1,
} as const

export function getScheduledTaskStatusLabel(status: number): string {
  return SCHEDULED_TASK_STATUS.labels[status] ?? '未知'
}
export function getScheduledTaskStatusType(status: number): string {
  return SCHEDULED_TASK_STATUS.types[status] ?? 'info'
}

// ========================
// User Account Status (sys_user.status: String '0'/'1')
// ========================
export const USER_STATUS = {
  labels: { '0': '正常', '1': '停用' } as Record<string, string>,
  types: { '0': 'success', '1': 'danger' } as Record<string, string>,
  NORMAL: '0',
  DISABLED: '1',
} as const

export function getUserStatusLabel(status: string): string {
  return USER_STATUS.labels[status] ?? '未知'
}
export function getUserStatusType(status: string): string {
  return USER_STATUS.types[status] ?? 'info'
}

// ========================
// User Role (sys_user.role: String)
// ========================
export const USER_ROLE = {
  labels: { USER: '普通用户', ADMIN: '管理员' } as Record<string, string>,
  types: { USER: 'info', ADMIN: 'danger' } as Record<string, string>,
  USER: 'USER',
  ADMIN: 'ADMIN',
} as const

export function getUserRoleLabel(role: string): string {
  return USER_ROLE.labels[role] ?? '未知'
}
export function getUserRoleType(role: string): string {
  return USER_ROLE.types[role] ?? 'info'
}

// ========================
// AI Generation Status (ac_generated_article.status: String)
// ========================
export const AI_ARTICLE_STATUS = {
  labels: { GENERATING: '生成中', COMPLETED: '已完成', FAILED: '失败' } as Record<string, string>,
  types: { GENERATING: 'warning', COMPLETED: 'success', FAILED: 'danger' } as Record<string, string>,
  GENERATING: 'GENERATING',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED',
} as const

export function getAiArticleStatusLabel(status: string): string {
  return AI_ARTICLE_STATUS.labels[status] ?? '未知'
}
export function getAiArticleStatusType(status: string): string {
  return AI_ARTICLE_STATUS.types[status] ?? 'info'
}

// ========================
// Hot Topic Platforms
// ========================
export const HOT_TOPIC_PLATFORMS = [
  { label: '微博', value: 'weibo' },
  { label: '抖音', value: 'douyin' },
  { label: '知乎', value: 'zhihu' },
  { label: '头条', value: 'toutiao' },
] as const

// ========================
// Publish Target Platforms
// ========================
export const PUBLISH_PLATFORMS = [
  { label: '微信公众号', value: 'wechat' },
  { label: '今日头条', value: 'toutiao' },
  { label: '百家号', value: 'baijia' },
  { label: '小红书', value: 'xiaohongshu' },
  { label: '抖音', value: 'douyin' },
] as const

// ========================
// Collection Target Platforms
// ========================
export const COLLECTION_PLATFORMS = [
  { label: '微信公众号', value: 'wechat' },
  { label: '百家号', value: 'baijia' },
  { label: '头条号', value: 'toutiao' },
  { label: '小红书', value: 'xiaohongshu' },
  { label: '抖音', value: 'douyin' },
  { label: '知乎', value: 'zhihu' },
] as const

// ========================
// Config Type (sys_config.configType: String)
// ========================
export const CONFIG_TYPE = {
  labels: { Y: '内置', N: '自定义' } as Record<string, string>,
  types: { Y: 'danger', N: 'info' } as Record<string, string>,
  BUILTIN: 'Y',
  CUSTOM: 'N',
} as const

export function getConfigTypeLabel(type: string): string {
  return CONFIG_TYPE.labels[type] ?? '未知'
}
export function getConfigTypeTagType(type: string): string {
  return CONFIG_TYPE.types[type] ?? 'info'
}
