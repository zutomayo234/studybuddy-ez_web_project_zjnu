import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { clearLogin } from '../utils/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('sb_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.message
    if (status === 401) {
      ElMessage.error(msg || '登录已过期，请重新登录')

      clearLogin()
      router.push('/login')
    } else if (status === 403) {
      ElMessage.error(msg || '权限不足')
    } else if (status === 404) {
      ElMessage.error(msg || '请求的资源不存在')
    } else if (status === 422) {

      ElMessage.warning(msg || '内容未通过安全校验')
    } else {
      ElMessage.error(msg || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request
