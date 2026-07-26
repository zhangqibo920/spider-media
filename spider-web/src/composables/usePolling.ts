import { ref, onUnmounted, type Ref } from 'vue'

/**
 * 轮询请求 Composable
 *
 * <p>封装定时轮询逻辑，确保组件卸载时自动清理定时器，
 * 避免内存泄漏和无效请求。</p>
 *
 * <p>使用示例：
 * <pre>
 * const { start, stop } = usePolling(async () => {
 *   await fetchTaskList()
 * }, 5000)
 *
 * onMounted(() => start())
 * </pre></p>
 *
 * @param fn        要轮询执行的异步函数
 * @param interval  轮询间隔（毫秒）
 * @param immediate 是否立即执行一次（默认 false）
 */
export function usePolling(
  fn: () => Promise<void> | void,
  interval: number,
  immediate = false,
) {
  const timer: Ref<ReturnType<typeof setInterval> | null> = ref(null)
  const isPolling = ref(false)

  /** 启动轮询 */
  function start() {
    if (isPolling.value) return
    isPolling.value = true
    if (immediate) fn()
    timer.value = setInterval(() => {
      fn()
    }, interval)
  }

  /** 停止轮询并清理定时器 */
  function stop() {
    if (timer.value) {
      clearInterval(timer.value)
      timer.value = null
    }
    isPolling.value = false
  }

  // 组件卸载时自动清理，防止内存泄漏和无效请求
  onUnmounted(() => {
    stop()
  })

  return {
    isPolling,
    start,
    stop,
  }
}
