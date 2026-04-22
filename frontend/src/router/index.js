import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      { path: '',           name: 'Home',        component: () => import('@/pages/Home.vue') },
      { path: 'movies/:id', name: 'MovieDetail', component: () => import('@/pages/MovieDetail.vue') },
      { path: 'cinemas',    name: 'Cinemas',     component: () => import('@/pages/CinemaList.vue') },
      {
        path: 'showtime/:id/seats',
        name: 'SeatSelection',
        component: () => import('@/pages/SeatSelection.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'booking/checkout',
        name: 'Checkout',
        component: () => import('@/pages/BookingCheckout.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'booking/:id/confirmation',
        name: 'BookingConfirmation',
        component: () => import('@/pages/BookingConfirmation.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'my/bookings',
        name: 'MyBookings',
        component: () => import('@/pages/MyBookings.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'my/tickets',
        name: 'MyTickets',
        component: () => import('@/pages/MyTickets.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'my/profile',
        name: 'Profile',
        component: () => import('@/pages/Profile.vue'),
        meta: { requiresAuth: true }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, saved) {
    return saved || { top: 0, behavior: 'smooth' }
  }
})

router.beforeEach(async (to) => {
  if (!to.meta.requiresAuth) return true
  const auth = useAuthStore()
  if (!auth.isAuthenticated) {
    auth.login(window.location.origin + to.fullPath)
    return false
  }
  return true
})

export default router
