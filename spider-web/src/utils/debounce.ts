/**
 * 防抖工具函数
 *
 * <p>常用于按钮点击防抖（避免用户连续点击导致重复提交）。
 * 在等待时间内重复触发会重置计时器，只有最后一次触发后等待 delay 毫秒才会执行。</p>
 *
 * @param fn    要防抖的函数
 * @param delay 防抖延迟（毫秒），默认 300ms
 * @returns 包装后的防抖函数（带 cancel 方法用于手动取消）
 */
export function debounce<T extends (...args: any[]) => any>(fn: T, delay = 300) {
  let timer: ReturnType<typeof setTimeout> | null = null

  const debounced = (...args: Parameters<T>) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn(...args)
      timer = null
    }, delay)
  }

  /** 手动取消尚未执行的防抖调用 */
  debounced.cancel = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  return debounced
}

/**
 * 节流工具函数
 *
 * <p>常用于滚动、resize 等高频事件。在规定时间内只执行第一次触发，
 * 后续触发会被忽略，直到下一个周期。</p>
 *
 * @param fn    要节流的函数
 * @param delay 节流间隔（毫秒），默认 300ms
 */
export function throttle<T extends (...args: any[]) => any>(fn: T, delay = 300) {
  let lastTime = 0

  return (...args: Parameters<T>) => {
    const now = Date.now()
    if (now - lastTime >= delay) {
      lastTime = now
      fn(...args)
    }
  }
}
