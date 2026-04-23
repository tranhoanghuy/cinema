<template>
  <div class="max-w-xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold mb-6">Hồ sơ cá nhân</h1>

    <Spinner v-if="loading" wrap />

    <form v-else @submit.prevent="save" class="card p-6 space-y-4">
      <!-- Avatar -->
      <div class="flex items-center gap-4 mb-2">
        <div class="w-16 h-16 rounded-full bg-brand flex items-center justify-center text-2xl font-bold">
          {{ auth.fullName.charAt(0).toUpperCase() }}
        </div>
        <div>
          <p class="font-semibold text-lg">{{ auth.fullName }}</p>
          <p class="text-sm text-gray-400">{{ auth.username }}</p>
        </div>
      </div>

      <div class="grid gap-4 sm:grid-cols-2">
        <div>
          <label class="block text-xs text-gray-500 mb-1">Họ và tên</label>
          <input v-model="form.displayName" type="text" class="input" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-1">Số điện thoại</label>
          <input v-model="form.phoneNumber" type="tel" class="input" />
        </div>
        <div class="sm:col-span-2">
          <label class="block text-xs text-gray-500 mb-1">Địa chỉ</label>
          <input v-model="form.address" type="text" class="input" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-1">Ngày sinh</label>
          <input v-model="form.dateOfBirth" type="date" class="input" />
        </div>
        <div>
          <label class="block text-xs text-gray-500 mb-1">Giới tính</label>
          <select v-model="form.gender" class="input">
            <option value="">Không xác định</option>
            <option value="MALE">Nam</option>
            <option value="FEMALE">Nữ</option>
            <option value="OTHER">Khác</option>
          </select>
        </div>
      </div>

      <div class="flex items-center justify-between pt-2">
        <p v-if="saved" class="text-green-400 text-sm">✓ Đã lưu thành công</p>
        <p v-if="error" class="text-red-400 text-sm">{{ error }}</p>
        <button type="submit" :disabled="saving" class="btn-primary ml-auto">
          {{ saving ? 'Đang lưu...' : 'Lưu thay đổi' }}
        </button>
      </div>
    </form>

    <!-- Stats -->
    <div class="grid grid-cols-3 gap-3 mt-4">
      <RouterLink to="/my/bookings"
                  class="card p-3 text-center hover:border-brand/40 transition-colors">
        <p class="text-2xl font-bold text-brand">{{ stats.bookings }}</p>
        <p class="text-xs text-gray-500 mt-0.5">Đặt vé</p>
      </RouterLink>
      <RouterLink to="/my/tickets"
                  class="card p-3 text-center hover:border-brand/40 transition-colors">
        <p class="text-2xl font-bold text-brand">{{ stats.tickets }}</p>
        <p class="text-xs text-gray-500 mt-0.5">Vé đã dùng</p>
      </RouterLink>
      <div class="card p-3 text-center">
        <p class="text-2xl font-bold text-brand">{{ stats.savedAmount }}</p>
        <p class="text-xs text-gray-500 mt-0.5">Đã tiết kiệm</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { userApi }    from '@/api/users'
import { bookingApi } from '@/api/bookings'
import { ticketApi }  from '@/api/tickets'
import { useAuthStore } from '@/stores/auth'
import Spinner from '@/components/common/Spinner.vue'
import type { UserProfile, PagedResponse, Booking, Ticket } from '@/types'

const auth    = useAuthStore()
const loading = ref(true)
const saving  = ref(false)
const saved   = ref(false)
const error   = ref('')

const form  = ref<UserProfile>({ displayName: '', phoneNumber: '', address: '', dateOfBirth: '', gender: '' })
const stats = ref({ bookings: 0, tickets: 0, savedAmount: '0₫' })

onMounted(async () => {
  try {
    const [profile, myBookings, myTickets] = await Promise.allSettled([
      userApi.getMe(),
      bookingApi.listMine(0, 1),
      ticketApi.listMine()
    ])
    if (profile.status === 'fulfilled' && profile.value) {
      const p = profile.value
      form.value = {
        displayName: p.displayName || auth.fullName,
        phoneNumber: p.phoneNumber || '',
        address:     p.address     || '',
        dateOfBirth: p.dateOfBirth || '',
        gender:      p.gender      || ''
      }
    }
    if (myBookings.status === 'fulfilled') {
      const r = myBookings.value
      stats.value.bookings = Array.isArray(r)
        ? r.length
        : (r as PagedResponse<Booking>).totalElements
    }
    if (myTickets.status === 'fulfilled') {
      stats.value.tickets = (myTickets.value as Ticket[]).filter(t => t.status === 'USED').length
    }
  } finally {
    loading.value = false
  }
})

async function save(): Promise<void> {
  saving.value = true
  error.value  = ''
  saved.value  = false
  try {
    await userApi.updateMe(form.value)
    saved.value = true
    setTimeout(() => { saved.value = false }, 3000)
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    saving.value = false
  }
}
</script>
