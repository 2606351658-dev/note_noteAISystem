import axios from 'axios'
import { ElMessage } from 'element-plus'

// ============================================================
// 创建 axios 实例
// baseURL 说明：
//   - 本地开发：Vite 代理将 /api 请求转发到 http://localhost:8081
//   - Docker 部署：Nginx 将 /api 请求转发到后端容器
//   所以这里统一使用 '/api' 作为 baseURL
// ============================================================
const request = axios.create({
    baseURL: '/api',  // 统一通过代理访问后端
    timeout: 10000000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        // 从localStorage获取token
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data

        // 如果业务状态码是200，返回业务数据
        if (res.code === 200) {
            return res.data // 返回你后端的data字段
        } else {
            // 业务错误
            ElMessage.error(res.message || '请求失败')
            return Promise.reject(new Error(res.message || '请求失败'))
        }
    },
    error => {
        // HTTP错误
        if (error.response) {
            const status = error.response.status
            const message = error.response.data?.message || '请求失败'

            if (status === 401) {
                ElMessage.error('登录已过期，请重新登录')
                localStorage.removeItem('token')
                localStorage.removeItem('user')
                // 跳转到登录页
                if (window.location.pathname !== '/login') {
                    window.location.href = '/login'
                }
            } else if (status === 403) {
                ElMessage.error('权限不足')
            } else {
                ElMessage.error(`错误 ${status}: ${message}`)
            }
        } else if (error.request) {
            ElMessage.error('网络错误，请检查网络连接')
        } else {
            ElMessage.error('请求配置错误')
        }

        return Promise.reject(error)
    }
)

export default request
