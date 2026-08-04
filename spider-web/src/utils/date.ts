export function formatTime(time: string | Date | null | undefined): string {
  if (!time) return ''
  const date = typeof time === 'string' ? new Date(time) : time
  if (isNaN(date.getTime())) return ''
  return date.toLocaleString()
}

export function formatDate(time: string | Date | null | undefined, format = 'YYYY-MM-DD'): string {
  if (!time) return ''
  const date = typeof time === 'string' ? new Date(time) : time
  if (isNaN(date.getTime())) return ''

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}
