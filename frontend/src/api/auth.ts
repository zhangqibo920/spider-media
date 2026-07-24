import request from '@/utils/request'

export function login(username: string, password: string) {
  return request.post('/auth/login', { username, password })
}

export function register(username: string, password: string, email: string) {
  return request.post('/auth/register', { username, password, email })
}

export function getCurrentUser() {
  return request.get('/auth/getInfo')
}
