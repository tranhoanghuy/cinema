import http from './index'

export const promotionApi = {
  listActive: ({ cinemaId, movieId } = {}) =>
    http.get('/promotions', {
      params: { ...(cinemaId ? { cinemaId } : {}), ...(movieId ? { movieId } : {}) }
    }).then(r => r.data)
}
