import http from './index'
import type { Movie } from '@/types'

export const movieApi = {
  listNowShowing: (page = 0, size = 20): Promise<Movie[]> =>
    http.get('/movies', { params: { page, size } }).then(r => r.data),

  listComingSoon: (): Promise<Movie[]> =>
    http.get('/movies/coming-soon').then(r => r.data),

  getById: (id: string): Promise<Movie> =>
    http.get(`/movies/${id}`).then(r => r.data),

  search: (q: string): Promise<Movie[]> =>
    http.get('/movies/search', { params: { q } }).then(r => r.data),

  create: (payload: Partial<Movie>): Promise<Movie> =>
    http.post('/movies', payload).then(r => r.data),

  update: (id: string, payload: Partial<Movie>): Promise<Movie> =>
    http.put(`/movies/${id}`, payload).then(r => r.data)
}
