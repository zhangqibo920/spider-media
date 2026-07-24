import request from '@/utils/request'

export function fetchHotTopics(platform: string) {
  return request.post('/ai/hotTopic/fetch', null, { params: { platform } })
}

export function getHotTopics() {
  return request.get('/ai/hotTopic/list')
}

export function generateArticle(hotTopicId: number, model = 'deepseek') {
  return request.post('/ai/article/generate', { hotTopicId, model })
}

export function getGeneratedArticles(page = 1, size = 20) {
  return request.get('/ai/article/page', { params: { page, size } })
}
