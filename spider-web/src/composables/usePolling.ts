import { ref, onUnmounted } from 'vue'

export interface UsePollingOptions {
  interval?: number
  immediate?: boolean
}

export function usePolling<T>(
  fetchFn: () => Promise<T>,
  options: UsePollingOptions = {}
) {
  const { interval = 3000, immediate = true } = options

  const data = ref<T | null>(null) as Ref<T | null>
  const loading = ref(false)
  const error = ref<Error | null>(null)
  let timer: ReturnType<typeof setInterval> | null = null

  const fetch = async () => {
    try {
      loading.value = true
      error.value = null
      data.value = await fetchFn()
    } catch (e) {
      error.value = e as Error
    } finally {
      loading.value = false
    }
  }

  const start = () => {
    stop()
    if (immediate) {
      fetch()
    }
    timer = setInterval(fetch, interval)
  }

  const stop = () => {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  onUnmounted(() => {
    stop()
  })

  return {
    data,
    loading,
    error,
    fetch,
    start,
    stop
  }
}
