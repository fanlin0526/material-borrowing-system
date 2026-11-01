/**
 * 将日期对象转换为后端可直接解析的本地时间字符串。
 *
 * 不能使用 toISOString()：它会先转换成 UTC，在东八区会造成 8 小时偏移，
 * 极端情况下把“明天”变成“今天”，触发预计归还时间校验失败。
 */
export function toLocalDateTimeString(value) {
  if (!value) return null
  const d = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(d.getTime())) return null
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
    + `T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}
