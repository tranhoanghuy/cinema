import http from './index'
import type { UserProfile } from '@/types'

export const userApi = {
  getMe: (): Promise<UserProfile> =>
    http.get('/users/me').then(r => r.data),

  updateMe: (payload: UserProfile): Promise<UserProfile> =>
    http.put('/users/me', payload).then(r => r.data)
}
