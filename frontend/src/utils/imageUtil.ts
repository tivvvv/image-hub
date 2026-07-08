import { saveAs } from 'file-saver'

/**
 * 格式化文件大小
 * @param size
 */
export const formatSize = (size?: number | string) => {
  if (size === undefined || size === null || size === '') {
    return '未知'
  }
  const numericSize = Number(size)
  if (Number.isNaN(numericSize)) {
    return '未知'
  }
  if (numericSize < 1024) return numericSize + ' B'
  if (numericSize < 1024 * 1024) return (numericSize / 1024).toFixed(2) + ' KB'
  return (numericSize / (1024 * 1024)).toFixed(2) + ' MB'
}

/**
 * 下载图片
 * @param url
 * @param fileName
 */
export function downloadImage(url?: string, fileName?: string) {
  if (!url) {
    return
  }
  saveAs(url, fileName)
}
