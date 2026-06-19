// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** heartBeat GET /api/heartBeat */
export async function heartBeatUsingGet(options?: { [key: string]: any }) {
  return request<API.BusinessResponseString_>('/api/heartBeat', {
    method: 'GET',
    ...(options || {}),
  })
}
