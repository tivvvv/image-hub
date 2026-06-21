import axios, { type AxiosInstance } from 'axios'
import { message } from 'ant-design-vue'

// 创建axios实例
const myAxios: AxiosInstance = axios.create({
  baseURL: '',
  timeout: 10000,
  withCredentials: true,
})

// 请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    return config
  },
  function (error) {
    return Promise.reject(error)
  },
)

// 响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    if (data.code === 40100) {
      const responseUrl = response.request?.responseURL ?? ''
      if (
        !responseUrl.includes('/user/login') &&
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
