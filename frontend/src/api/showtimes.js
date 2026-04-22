import http from './index'

export const showtimeApi = {
  list: () =>
    http.get('/showtimes').then(r => r.data),

  getById: id =>
    http.get(`/showtimes/${id}`).then(r => r.data),

  getSeatStatus: id =>
    http.get(`/showtimes/${id}/seats`).then(r => r.data),

  // admin
  create: payload =>
    http.post('/showtimes', payload).then(r => r.data),

  openForSale: id =>
    http.patch(`/showtimes/${id}/open-for-sale`).then(r => r.data)
}
