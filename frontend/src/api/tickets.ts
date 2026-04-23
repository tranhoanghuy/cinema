import http from './index'
import type { Ticket } from '@/types'

export const ticketApi = {
  listMine: (): Promise<Ticket[]> =>
    http.get('/tickets/my').then(r => r.data),

  getByBooking: (bookingId: string): Promise<Ticket[]> =>
    http.get(`/tickets/booking/${bookingId}`).then(r => r.data),

  validate: (id: string): Promise<Ticket> =>
    http.post(`/tickets/${id}/validate`).then(r => r.data)
}
