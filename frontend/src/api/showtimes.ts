import http from './index'
import type { Showtime, SeatStatus } from '@/types'

export const showtimeApi = {
  list: (): Promise<Showtime[]> =>
    http.get('/showtimes').then(r => r.data),

  getById: (id: string): Promise<Showtime> =>
    http.get(`/showtimes/${id}`).then(r => r.data),

  getSeatStatus: (id: string): Promise<SeatStatus[]> =>
    http.get(`/showtimes/${id}/seats`).then(r => r.data),

  create: (payload: Partial<Showtime>): Promise<Showtime> =>
    http.post('/showtimes', payload).then(r => r.data),

  openForSale: (id: string): Promise<Showtime> =>
    http.patch(`/showtimes/${id}/open-for-sale`).then(r => r.data)
}
