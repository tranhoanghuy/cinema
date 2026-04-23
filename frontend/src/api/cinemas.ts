import http from './index'
import type { Cinema, Screen } from '@/types'

export const cinemaApi = {
  list: (city?: string): Promise<Cinema[]> =>
    http.get('/cinemas', { params: city ? { city } : {} }).then(r => r.data),

  getById: (id: string): Promise<Cinema> =>
    http.get(`/cinemas/${id}`).then(r => r.data),

  listScreens: (cinemaId: string): Promise<Screen[]> =>
    http.get(`/cinemas/${cinemaId}/screens`).then(r => r.data)
}
