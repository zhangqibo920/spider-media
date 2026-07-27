/**
 * Type definitions matching the backend entity structures.
 * Field names use camelCase (matching Java entity @Data generated getters).
 */

export interface User {
  userId: number
  userName: string
  nickName: string
  email?: string
  phonenumber?: string
  avatar?: string
  password?: string
  status: string
  role: string
  createTime?: string
}

export interface TargetAccount {
  id: number
  userId: number
  platform: string
  accountName: string
  accountId: string
  accountUrl: string
  groupName: string
  status: string
  description: string
}

export interface CollectedArticle {
  id: number
  userId: number
  targetAccountId: number
  platform: string
  title: string
  content: string
  summary: string
  url: string
  author: string
  viewCount: number
  likeCount: number
  commentCount: number
  shareCount: number
  publishTime: string
  collectedTime: string
  createTime: string
}

export interface HotTopic {
  id: number
  userId: number
  platform: string
  title: string
  description: string
  hotScore: number
  url: string
  category: string
}

export interface GeneratedArticle {
  id: number
  userId: number
  hotTopicId: number
  title: string
  content: string
  summary: string
  modelUsed: string
  wordCount: number
  status: string
  createTime: string
}

export interface PlatformAccount {
  id: number
  userId: number
  platform: string
  accountName: string
  accountId: string
  accessToken?: string
  refreshToken?: string
  tokenExpireTime?: string
  status: string
  groupName: string
}

export interface PublishTask {
  id: number
  userId: number
  platformAccountId: number
  articleId?: number
  platform: string
  title: string
  content: string
  summary?: string
  coverImage?: string
  status: number
  scheduledTime?: string
  publishedTime?: string
  publishResult?: string
  retryCount?: number
  createTime: string
}

export interface ScheduledTask {
  id: number
  userId: number
  taskName: string
  taskType: string
  cronExpression: string
  status: number
  lastRunTime?: string
  nextRunTime?: string
  runCount: number
  failCount: number
  config?: string
  createTime: string
}

export interface SysConfig {
  id: number
  configName: string
  configKey: string
  configValue: string
  configType: string
}

export interface OperLog {
  id: number
  username: string
  module: string
  action: string
  description: string
  ip: string
  method: string
  params: string
  status: number
  errorMsg: string
  createTime: string
}

/** AI 模型配置（对应后端 AiModel 实体） */
export interface AiModel {
  id: number
  modelKey: string
  modelName: string
  provider: string
  apiKey: string
  baseUrl: string
  enabled: string
  sortOrder: number
  testStatus: string
  testTime?: string
  testMessage?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

/** 字典类型（对应后端 SysDictType 实体） */
export interface SysDictType {
  id: number
  dictName: string
  dictType: string
  status: string
  remark?: string
  createTime?: string
}

/** 字典数据（对应后端 SysDictData 实体） */
export interface SysDictData {
  id: number
  dictSort: number
  dictLabel: string
  dictValue: string
  dictType: string
  cssClass?: string
  listClass?: string
  isDefault?: string
  status: string
  remark?: string
  createTime?: string
}

export interface Role {
  roleId: number
  roleName: string
  roleKey: string
  status: string
}

export interface Menu {
  menuId: number
  menuName: string
  parentId: number
  path: string
  component: string
  perms: string
  icon: string
  sortOrder: number
  menuType: string
  status: string
  visible: string
  children?: Menu[]
}

/** 分页查询参数（对应后端 PageParam） */
export interface PageParam {
  pageNo: number
  pageSize: number
}

/**
 * Backend page result format (matches Java PageResult<T>)
 */
export interface PageResult<T> {
  list: T[]
  total: number
}
