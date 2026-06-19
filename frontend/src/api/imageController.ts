// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** deleteImage DELETE /api/image */
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

/** getImageById GET /api/image/${param0} */
export async function getImageByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getImageByIdUsingGETParams,
  options?: { [key: string]: any },
) {
  const { id: param0, ...queryParams } = params
  return request<API.BusinessResponseImage_>(`/api/image/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** expandImage POST /api/image/expand */
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

/** queryImageExpandTaskStatus GET /api/image/expand/task/status/${param0} */
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

/** fetchImage POST /api/image/fetch */
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

/** listImageByPage POST /api/image/page */
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

/** listImageVOByPage POST /api/image/page/vo */
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

/** listImageVOByPageWithCache POST /api/image/page/vo/cache */
export async function listImageVoByPageWithCacheUsingPost(
  body: API.ImageQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BusinessResponsePageImageVO_>('/api/image/page/vo/cache', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** reviewImage POST /api/image/review */
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

/** listImageTagCategory GET /api/image/tagList */
export async function listImageTagCategoryUsingGet(options?: { [key: string]: any }) {
  return request<API.BusinessResponseImageTagCategory_>('/api/image/tagList', {
    method: 'GET',
    ...(options || {}),
  })
}

/** updateImage POST /api/image/update */
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

/** batchUpdateImage POST /api/image/update/batch */
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

/** uploadImage POST /api/image/upload */
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

/** uploadImageByUrl POST /api/image/upload/url */
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

/** getImageVOById GET /api/image/vo/${param0} */
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
