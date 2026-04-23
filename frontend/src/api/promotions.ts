import http from './index'
import type { Promotion } from '@/types'

export const promotionApi = {
  listActive: ({ cinemaId, movieId }: { cinemaId?: string; movieId?: string } = {}): Promise<Promotion[]> =>
    http.get('/promotions', {
      params: { ...(cinemaId ? { cinemaId } : {}), ...(movieId ? { movieId } : {}) }
    }).then(r => r.data)
}
