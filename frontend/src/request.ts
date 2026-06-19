import axios, { type AxiosInstance } from 'axios'
import { message } from 'ant-design-vue'

// 创建axios实例
const myAxios: AxiosInstance = axios.create({
  // baseURL: 'http://localhost:8111',
  baseURL: '',
  timeout: 10000,
  withCredentials: true,
})

// 请求拦截器
axios.interceptors.request.use(
  function (config) {
    return config
  },
  function (error) {
    return Promise.reject(error)
  },
)

// 响应拦截器
axios.interceptors.response.use(
  function (response) {
    const { data } = response
    if (data.code === 40100) {
      if (
        !response.request.responseURL.include('/user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning(data.message)
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    return Promise.reject(error)
  },
)

export default myAxios
