import axios, { AxiosError, type AxiosRequestConfig, type Canceler } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import i18n from '@/i18n'

const t = i18n.global.t

/**
 * 扩展 AxiosRequestConfig，增加 cancelRepeat 配置项
 * 用于控制 POST/PUT/DELETE 等写操作是否启用重复取消机制
 */
declare module 'axios' {
  interface AxiosRequestConfig {
    /** 是否启用重复请求取消（GET 默认启用；写操作需显式设置 true） */
    cancelRepeat?: boolean
  }
}

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

// 用于避免 401 时多次重复跳转登录页
let isRedirecting = false

/**
 * 重复请求取消机制
 *
 * <p>维护一个进行中请求的 Map（key=method+url+params），
 * 当相同 key 的新请求发起时，自动取消旧请求。
 * 避免快速切换页面或重复点击时旧请求覆盖新数据。</p>
 *
 * <p>GET 请求默认启用取消机制；POST/PUT/DELETE 等写操作默认不启用
 * （避免误取消必要的写入操作），可在请求配置中通过 cancelRepeat=true 显式启用。</p>
 */
const pendingMap = new Map<string, Canceler>()

function getRequestKey(config: AxiosRequestConfig): string {
  const { method = 'get', url = '', params = {}, data = {} } = config
  return [method.toLowerCase(), url, JSON.stringify(params), JSON.stringify(data)].join('&')
}

function addPending(config: AxiosRequestConfig): void {
  const key = getRequestKey(config)
  // 已有相同请求在进行中 → 取消旧请求
  if (pendingMap.has(key)) {
    const cancel = pendingMap.get(key)
    cancel?.('request canceled: duplicate request')
    pendingMap.delete(key)
  }
  // 注册新的 cancel token
  config.cancelToken = new axios.CancelToken(cancel => {
    if (!pendingMap.has(key)) {
      pendingMap.set(key, cancel)
    }
  })
}

function removePending(config: AxiosRequestConfig): void {
  const key = getRequestKey(config)
  if (pendingMap.has(key)) {
    pendingMap.delete(key)
  }
}

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // 写操作默认不启用重复取消（避免误取消必要写入）
    const method = (config.method || 'get').toLowerCase()
    const enableCancel = method === 'get' || config.cancelRepeat === true
    if (enableCancel) {
      addPending(config)
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    // 请求完成，从 pending 列表移除
    removePending(response.config)
    const res = response.data
    // 后端约定 code === 0 表示成功；非 0 视为业务错误
    if (res && typeof res === 'object' && 'code' in res && res.code !== 0) {
      // 兼容后端在 HTTP 200 响应中返回 code=401/403 的情况
      if (res.code === 401) {
        handleUnauthorized(res.message || t('request.unauthorized'))
      } else if (res.code === 403) {
        ElMessage.error(res.message || t('request.forbidden'))
      } else {
        ElMessage.error(res.message || t('request.requestFailed'))
      }
      return Promise.reject(new Error(res.message || t('request.requestFailed')))
    }
    return res
  },
  (error: AxiosError<{ code?: number; message?: string }>) => {
    // 被取消的请求不算错误，静默处理
    if (axios.isCancel(error)) {
      return Promise.reject(error)
    }
    // 从 pending 列表移除（请求失败也算结束）
    if (error.config) {
      removePending(error.config)
    }
    const status = error.response?.status
    const respData = error.response?.data

    // HTTP 401：后端 SecurityConfig 直接返回 401 状态码（未登录/token 过期）
    // 必须在这里处理，否则会被当作普通网络错误，用户被困在当前页
    if (status === 401) {
      const msg = (respData && typeof respData === 'object' && respData.message)
        ? respData.message
        : t('request.unauthorized')
      handleUnauthorized(msg)
      return Promise.reject(error)
    }

    // HTTP 403：权限不足（如普通用户访问管理员接口）
    if (status === 403) {
      const msg = (respData && typeof respData === 'object' && respData.message)
        ? respData.message
        : t('request.forbidden')
      ElMessage.error(msg)
      return Promise.reject(error)
    }

    // HTTP 429：限流
    if (status === 429) {
      ElMessage.error(respData?.message || t('request.tooManyRequests'))
      return Promise.reject(error)
    }

    // HTTP 5xx：服务端错误
    if (status && status >= 500) {
      ElMessage.error(t('request.serverError'))
      return Promise.reject(error)
    }

    // 网络错误 / 超时 / 其他
    if (error.code === 'ECONNABORTED') {
      ElMessage.error(t('request.timeout'))
    } else if (!error.response) {
      ElMessage.error(t('request.networkError'))
    } else {
      ElMessage.error(respData?.message || error.message || t('request.requestFailed'))
    }
    return Promise.reject(error)
  }
)

/**
 * 处理 401 未授权：清除 token，提示用户并跳转登录页
 *
 * <p>使用 isRedirecting 标志位避免多个并发请求同时触发跳转。
 * 跳转时携带 redirect 参数，登录成功后可回到原页面。</p>
 */
function handleUnauthorized(message: string) {
  localStorage.removeItem('token')
  if (isRedirecting) return
  isRedirecting = true
  ElMessage.error(message)
  const currentRoute = router.currentRoute.value
  // 避免在登录页重复跳转
  if (currentRoute.name !== 'Login') {
    router.push({
      name: 'Login',
      query: currentRoute.fullPath !== '/' ? { redirect: currentRoute.fullPath } : undefined,
    }).finally(() => {
      isRedirecting = false
    })
  } else {
    isRedirecting = false
  }
}

export default request
