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

/**
 * Backend page result format (matches Java PageResult<T>)
 */
export interface PageResult<T> {
  list: T[]
  total: number
}
