import http from './index'
import type { Booking, BookingPayload, PagedResponse } from '@/types'

export const bookingApi = {
  create: (payload: BookingPayload): Promise<Booking> =>
    http.post('/bookings', payload).then(r => r.data),

  getById: (id: string): Promise<Booking> =>
    http.get(`/bookings/${id}`).then(r => r.data),

  listMine: (page = 0, size = 20, status?: string): Promise<Booking[] | PagedResponse<Booking>> =>
    http.get('/bookings/my', { params: { page, size, ...(status ? { status } : {}) } })
        .then(r => r.data),

  cancel: (id: string): Promise<void> =>
    http.delete(`/bookings/${id}`).then(r => r.data)
}
