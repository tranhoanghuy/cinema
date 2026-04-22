import http from './index'

export const ticketApi = {
  listMine: () =>
    http.get('/tickets/my').then(r => r.data),

  getByBooking: bookingId =>
    http.get(`/tickets/booking/${bookingId}`).then(r => r.data),

  validate: id =>
    http.post(`/tickets/${id}/validate`).then(r => r.data)
}
