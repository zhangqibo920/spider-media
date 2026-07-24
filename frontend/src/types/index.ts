export interface User {
  id: number
  username: string
  nickname?: string
  email?: string
  phone?: string
  avatar?: string
  role: string
  status: number
}

export interface TargetAccount {
  id: number
  platform: string
  accountName: string
  accountId: string
  accountUrl: string
  groupName: string
  status: number
  description: string
}

export interface CollectedArticle {
  id: number
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
}

export interface HotTopic {
  id: number
  platform: string
  title: string
  description: string
  hotScore: number
  url: string
  category: string
}

export interface GeneratedArticle {
  id: number
  title: string
  content: string
  summary: string
  modelUsed: string
  wordCount: number
  status: string
}

export interface PlatformAccount {
  id: number
  platform: string
  accountName: string
  accountId: string
  status: number
  groupName: string
}

export interface PublishTask {
  id: number
  platform: string
  title: string
  content: string
  status: number
  scheduledTime: string
  publishedTime: string
}

export interface ScheduledTask {
  id: number
  taskName: string
  taskType: string
  cronExpression: string
  status: number
  lastRunTime: string
  nextRunTime: string
  runCount: number
  failCount: number
}

export interface PageResult<T> {
  total: number
  page: number
  size: number
  records: T[]
}
