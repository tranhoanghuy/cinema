import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api/v1',
  timeout: 15_000,
  headers: { 'Content-Type': 'application/json' }
})

http.interceptors.request.use(async config => {
  const { useAuthStore } = await import('@/stores/auth')
  const auth = useAuthStore()
  if (auth.isAuthenticated) {
    const t = await auth.getToken()
    if (t) config.headers.Authorization = `Bearer ${t}`
  }
  return config
})

http.interceptors.response.use(
  res => res.data?.data !== undefined ? { ...res, data: res.data.data } : res,
  err => {
    const msg = err.response?.data?.message || err.message || 'Lỗi không xác định'
    return Promise.reject(new Error(msg))
  }
)

export default http
