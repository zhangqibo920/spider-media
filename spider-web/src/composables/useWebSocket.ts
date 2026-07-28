import { ref } from 'vue'

export function useWebSocket(token: string) {
  let ws: WebSocket | null = null
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let heartbeatTimer: ReturnType<typeof setInterval> | null = null
  const connected = ref(false)

  type MessageHandler = (data: any) => void
  const handlers = new Set<MessageHandler>()

  function connect() {
    if (!token || ws?.readyState === WebSocket.OPEN) return

    const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = location.hostname + ':8080'
    const url = `${protocol}//${host}/ws/notification?token=${token}`

    ws = new WebSocket(url)

    ws.onopen = () => {
      connected.value = true
      startHeartbeat()
    }

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        handlers.forEach(fn => fn(data))
      } catch { /* ignore parse errors */ }
    }

    ws.onclose = () => {
      connected.value = false
      stopHeartbeat()
      scheduleReconnect()
    }

    ws.onerror = () => {
      ws?.close()
    }
  }

  function disconnect() {
    if (reconnectTimer) clearTimeout(reconnectTimer)
    stopHeartbeat()
    if (ws) {
      ws.onclose = null
      ws.close()
      ws = null
    }
    connected.value = false
  }

  function onMessage(handler: MessageHandler) {
    handlers.add(handler)
    return () => handlers.delete(handler)
  }

  function startHeartbeat() {
    heartbeatTimer = setInterval(() => {
      if (ws?.readyState === WebSocket.OPEN) {
        ws.send('ping')
      }
    }, 30000)
  }

  function stopHeartbeat() {
    if (heartbeatTimer) clearInterval(heartbeatTimer)
  }

  function scheduleReconnect() {
    if (reconnectTimer) clearTimeout(reconnectTimer)
    reconnectTimer = setTimeout(() => connect(), 5000)
  }

  return { connected, connect, disconnect, onMessage }
}
