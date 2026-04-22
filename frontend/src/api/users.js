import http from './index'

export const userApi = {
  getMe: () =>
    http.get('/users/me').then(r => r.data),

  updateMe: payload =>
    http.put('/users/me', payload).then(r => r.data)
}
