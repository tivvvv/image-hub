// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 删除图片 DELETE /api/image */
export async function deleteImageUsingDelete(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BusinessResponseBoolean_>('/api/image', {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 扩展图片 POST /api/image/expand */
export async function expandImageUsingPost(
  body: API.ImageExpandRequest,
  options?: { [key: string]: any },
) {
  return request<API.BusinessResponseImageExpandTaskCreateResult_>('/api/image/expand', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 查询扩图任务状态 GET /api/image/expand/task/status/${param0} */
export async function queryImageExpandTaskStatusUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.queryImageExpandTaskStatusUsingGETParams,
  options?: { [key: string]: any },
) {
  const { taskId: param0, ...queryParams } = params
  return request<API.BusinessResponseImageExpandTaskStatusQueryResult_>(
    `/api/image/expand/task/status/${param0}`,
    {
      method: 'GET',
      params: { ...queryParams },
      ...(options || {}),
    },
  )
}

/** 抓取图片 POST /api/image/fetch */
export async function fetchImageUsingPost(
  body: API.ImageFetchRequest,
  options?: { [key: string]: any },
) {
  return request<API.BusinessResponseInt_>('/api/image/fetch', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 分页获取图片 POST /api/image/page */
export async function listImageByPageUsingPost(
  body: API.ImageQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BusinessResponsePageImage_>('/api/image/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 分页获取图片视图 POST /api/image/page/vo */
export async function listImageVoByPageUsingPost(
  body: API.ImageQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BusinessResponsePageImageVO_>('/api/image/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 审核图片 POST /api/image/review */
export async function reviewImageUsingPost(
  body: API.ImageReviewRequest,
  options?: { [key: string]: any },
) {
  return request<API.BusinessResponseBoolean_>('/api/image/review', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取图片标签和分类 GET /api/image/tagList */
export async function listImageTagCategoryUsingGet(options?: { [key: string]: any }) {
  return request<API.BusinessResponseImageTagCategory_>('/api/image/tagList', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 更新图片 POST /api/image/update */
export async function updateImageUsingPost(
  body: API.ImageUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BusinessResponseBoolean_>('/api/image/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 批量更新图片 POST /api/image/update/batch */
export async function batchUpdateImageUsingPost(
  body: API.ImageBatchUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BusinessResponseBoolean_>('/api/image/update/batch', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 上传图片 POST /api/image/upload */
export async function uploadImageUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadImageUsingPOSTParams,
  body: {},
  file?: File,
  options?: { [key: string]: any },
) {
  const formData = new FormData()

  if (file) {
    formData.append('file', file)
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele]

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''))
        } else {
          formData.append(ele, JSON.stringify(item))
        }
      } else {
        formData.append(ele, item)
      }
    }
  })

  return request<API.BusinessResponseImageVO_>('/api/image/upload', {
    method: 'POST',
    params: {
      ...params,
    },
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    ...(options || {}),
  })
}

/** 通过链接上传图片 POST /api/image/upload/url */
export async function uploadImageByUrlUsingPost(
  body: API.ImageUploadRequest,
  options?: { [key: string]: any },
) {
  return request<API.BusinessResponseImageVO_>('/api/image/upload/url', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 根据编号获取图片视图 GET /api/image/vo/${param0} */
export async function getImageVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getImageVOByIdUsingGETParams,
  options?: { [key: string]: any },
) {
  const { id: param0, ...queryParams } = params
  return request<API.BusinessResponseImageVO_>(`/api/image/vo/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}
