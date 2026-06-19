/**
 * 空间级别码
 */
export const SPACE_LEVEL_CODE = {
  COMMON: 0,
  PRO: 1,
  ULTRA: 2,
} as const

/**
 * 空间级别文案
 */
export const SPACE_LEVEL_DESC: Record<number, string> = {
  0: '普通版',
  1: '专业版',
  2: '旗舰版',
}

/**
 * 空间级别选项
 */
export const SPACE_LEVEL_OPTIONS = Object.keys(SPACE_LEVEL_DESC).map((key) => {
  const value = Number(key)
  return {
    label: SPACE_LEVEL_DESC[value],
    value,
  }
})
