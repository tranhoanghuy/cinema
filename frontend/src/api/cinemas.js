import http from './index'

export const cinemaApi = {
  list: (city) =>
    http.get('/cinemas', { params: city ? { city } : {} }).then(r => r.data),

  getById: id =>
    http.get(`/cinemas/${id}`).then(r => r.data),

  listScreens: cinemaId =>
    http.get(`/cinemas/${cinemaId}/screens`).then(r => r.data)
}
