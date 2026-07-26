import request from '@/utils/request'

/** 验证码响应数据 */
export interface CaptchaResult {
  captchaId: string
  img: string
}

/**
 * 获取图形验证码
 * @returns 包含 captchaId 和 Base64 图片的响应
 */
export function getCaptcha() {
  return request.get<unknown, { data: CaptchaResult }>('/auth/captcha')
}

/**
 * 用户登录（需携带验证码）
 * @param username   用户名
 * @param password   密码
 * @param captchaId  验证码唯一标识
 * @param captchaCode 用户输入的验证码文本
 */
export function login(
  username: string,
  password: string,
  captchaId: string,
  captchaCode: string,
) {
  return request.post('/auth/login', { username, password, captchaId, captchaCode })
}

export function register(username: string, password: string) {
  return request.post('/auth/register', { username, password })
}

export function getCurrentUser() {
  return request.get('/auth/getInfo')
}
