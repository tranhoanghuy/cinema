<template>
  <div class="max-w-lg mx-auto px-4 py-12 text-center">
    <Spinner v-if="loading" wrap />

    <div v-else-if="booking" class="space-y-6 animate-slide-up">
      <!-- Status icon -->
      <div :class="['mx-auto w-20 h-20 rounded-full flex items-center justify-center text-4xl',
                    statusColor]">
        {{ statusIcon }}
      </div>

      <div>
        <h1 class="text-2xl font-bold">{{ statusTitle }}</h1>
        <p class="text-gray-400 mt-1">{{ statusDesc }}</p>
      </div>

      <!-- Booking details card -->
      <div class="card p-5 text-left space-y-2 text-sm">
        <div class="flex justify-between">
          <span class="text-gray-500">Mã đặt vé</span>
          <span class="font-mono font-medium">{{ booking.bookingId?.slice(-12).toUpperCase() }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-500">Trạng thái</span>
          <span :class="statusTextColor">{{ booking.status }}</span>
        </div>
        <div class="flex justify-between">
          <span class="text-gray-500">Tổng tiền</span>
          <span class="font-bold text-brand">{{ formatPrice(booking.totalAmount) }}</span>
        </div>
      </div>

      <!-- Payment redirect info -->
      <div v-if="booking.paymentUrl" class="card p-4 space-y-3">
        <p class="text-sm text-gray-400">Nhấn nút bên dưới để thanh toán</p>
        <a :href="booking.paymentUrl" target="_blank" rel="noopener"
           class="btn-primary w-full">
          Thanh toán ngay →
        </a>
      </div>

      <!-- Waiting message for pending -->
      <div v-else-if="booking.status === 'PENDING_PAYMENT'" class="text-sm text-yellow-400">
        ⏳ Đang chờ xác nhận thanh toán...
      </div>

      <!-- Actions -->
      <div class="flex flex-col sm:flex-row gap-3 justify-center">
        <RouterLink :to="{ name: 'MyBookings' }" class="btn-ghost">
          Xem lịch sử đặt vé
        </RouterLink>
        <RouterLink :to="{ name: 'MyTickets' }" class="btn-primary">
          Xem E-Ticket
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { bookingApi } from '@/api/bookings'
import Spinner from '@/components/common/Spinner.vue'
import type { Booking, BookingStatus } from '@/types'

const route   = useRoute()
const booking = ref<Booking | null>(null)
const loading = ref(true)
let pollTimer: ReturnType<typeof setInterval> | null = null

onMounted(async () => {
  await loadBooking()
  pollTimer = setInterval(async () => {
    if (booking.value?.status === 'PENDING_PAYMENT' || booking.value?.status === 'PROCESSING') {
      await loadBooking()
    } else {
      if (pollTimer) clearInterval(pollTimer)
    }
  }, 3000)
})

onUnmounted(() => { if (pollTimer) clearInterval(pollTimer) })

async function loadBooking(): Promise<void> {
  try {
    booking.value = await bookingApi.getById(route.params.id as string)
  } finally {
    loading.value = false
  }
}

type StatusInfo = { icon: string; color: string; title: string; desc: string; textColor: string }

const statusMap: Record<BookingStatus, StatusInfo> = {
  CONFIRMED:       { icon: '✅', color: 'bg-green-900/40',  title: 'Đặt vé thành công!',        desc: 'Vé của bạn đã được xác nhận. Kiểm tra E-Ticket bên dưới.', textColor: 'text-green-400' },
  PENDING_PAYMENT: { icon: '⏳', color: 'bg-yellow-900/40', title: 'Đang chờ thanh toán',        desc: 'Vui lòng hoàn tất thanh toán trong vòng 15 phút.',          textColor: 'text-yellow-400' },
  PROCESSING:      { icon: '⏳', color: 'bg-yellow-900/40', title: 'Đang xử lý...',              desc: 'Hệ thống đang xử lý đặt vé của bạn...',                     textColor: 'text-yellow-400' },
  CANCELLED:       { icon: '❌', color: 'bg-red-900/40',    title: 'Đặt vé đã hủy',             desc: 'Đặt vé đã bị hủy. Ghế đã được giải phóng.',                 textColor: 'text-red-400' },
  FAILED:          { icon: '❌', color: 'bg-red-900/40',    title: 'Đặt vé thất bại',           desc: 'Đã có lỗi xảy ra. Vui lòng thử lại.',                       textColor: 'text-red-400' }
}

const currentStatus = computed<StatusInfo>(() =>
  booking.value?.status ? statusMap[booking.value.status] : statusMap.CONFIRMED)

const statusIcon      = computed(() => currentStatus.value.icon)
const statusColor     = computed(() => currentStatus.value.color)
const statusTitle     = computed(() => currentStatus.value.title)
const statusDesc      = computed(() => currentStatus.value.desc)
const statusTextColor = computed(() => currentStatus.value.textColor)

function formatPrice(p: number): string {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p)
}
</script>
