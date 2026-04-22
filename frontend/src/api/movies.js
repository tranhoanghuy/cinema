import http from './index'

export const movieApi = {
  listNowShowing: (page = 0, size = 20) =>
    http.get('/movies', { params: { page, size } }).then(r => r.data),

  listComingSoon: () =>
    http.get('/movies/coming-soon').then(r => r.data),

  getById: id =>
    http.get(`/movies/${id}`).then(r => r.data),

  search: q =>
    http.get('/movies/search', { params: { q } }).then(r => r.data),

  create: payload =>
    http.post('/movies', payload).then(r => r.data),

  update: (id, payload) =>
    http.put(`/movies/${id}`, payload).then(r => r.data)
}
