/**
 * 图片审核状态码
 */
export const PIC_REVIEW_STATUS_CODE = {
  REVIEWING: 0,
  PASS: 1,
  REJECT: 2,
}

/**
 * 图片审核状态文案
 */
export const PIC_REVIEW_STATUS_DESC = {
  0: '审核中',
  1: '通过',
  2: '拒绝',
}

/**
 * 图片审核下拉列表选项
 */
export const PIC_REVIEW_STATUS_OPTIONS = Object.keys(PIC_REVIEW_STATUS_DESC).map((key) => {
  return {
    label: PIC_REVIEW_STATUS_DESC[Number(key) as keyof typeof PIC_REVIEW_STATUS_DESC],
    value: key,
  }
})
