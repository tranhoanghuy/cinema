import http from './index'

export const bookingApi = {
  create: payload =>
    http.post('/bookings', payload).then(r => r.data),

  getById: id =>
    http.get(`/bookings/${id}`).then(r => r.data),

  listMine: (page = 0, size = 20, status) =>
    http.get('/bookings/my', { params: { page, size, ...(status ? { status } : {}) } })
        .then(r => r.data),

  cancel: id =>
    http.delete(`/bookings/${id}`).then(r => r.data)
}
