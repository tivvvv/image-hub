// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 删除空间 DELETE /api/space */
export async function deleteSpaceUsingDelete(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.BusinessResponseBoolean_>('/api/space', {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 新增空间 POST /api/space/add */
export async function addSpaceUsingPost(
  body: API.SpaceAddRequest,
  options?: { [key: string]: any },
) {
  return request<API.BusinessResponseSpaceVO_>('/api/space/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取空间等级 GET /api/space/level */
export async function getSpaceLevelUsingGet(options?: { [key: string]: any }) {
  return request<API.BusinessResponseListSpaceLevelVO_>('/api/space/level', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 分页获取空间 POST /api/space/page */
export async function listSpaceByPageUsingPost(
  body: API.SpaceQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BusinessResponsePageSpace_>('/api/space/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 分页获取空间视图 POST /api/space/page/vo */
export async function listSpaceVoByPageUsingPost(
  body: API.SpaceQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BusinessResponsePageSpaceVO_>('/api/space/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 更新空间 POST /api/space/update */
export async function updateSpaceUsingPost(
  body: API.SpaceUpdateRequest,
  options?: { [key: string]: any },
) {
  return request<API.BusinessResponseSpaceVO_>('/api/space/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** 根据编号获取空间视图 GET /api/space/vo/${param0} */
export async function getSpaceVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceVOByIdUsingGETParams,
  options?: { [key: string]: any },
) {
  const { id: param0, ...queryParams } = params
  return request<API.BusinessResponseSpaceVO_>(`/api/space/vo/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}
