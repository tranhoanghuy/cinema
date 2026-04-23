<template>
  <div class="max-w-2xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold mb-6">Xác nhận đặt vé</h1>

    <div v-if="!bookingStore.showtime" class="text-center py-16 text-gray-500">
      <p>Không có thông tin ghế. <RouterLink to="/" class="text-brand">Quay lại</RouterLink></p>
    </div>

    <div v-else class="space-y-4">
      <!-- Order summary -->
      <div class="card p-4 space-y-3">
        <h2 class="font-semibold">Thông tin đặt vé</h2>
        <div class="text-sm space-y-1.5 text-gray-300">
          <p><span class="text-gray-500 w-28 inline-block">Phim:</span> {{ bookingStore.showtime.movieTitle }}</p>
          <p><span class="text-gray-500 w-28 inline-block">Rạp:</span> {{ bookingStore.showtime.cinemaName }}</p>
          <p><span class="text-gray-500 w-28 inline-block">Phòng:</span> {{ bookingStore.showtime.screenName }}</p>
          <p><span class="text-gray-500 w-28 inline-block">Suất chiếu:</span> {{ dayjs(bookingStore.showtime.startTime).format('HH:mm DD/MM/YYYY') }}</p>
          <p><span class="text-gray-500 w-28 inline-block">Ghế:</span>
            <span class="font-medium">{{ bookingStore.selectedList.map(s => s.seatCode).join(', ') }}</span>
          </p>
        </div>

        <!-- Seat breakdown -->
        <div class="border-t border-surface-border pt-3 space-y-1">
          <div v-for="s in bookingStore.selectedList" :key="s.seatId"
               class="flex justify-between text-sm">
            <span class="text-gray-400">{{ s.seatCode }} ({{ formatCategory(s.category) }})</span>
            <span>{{ formatPrice(s.unitPrice) }}</span>
          </div>
        </div>
      </div>

      <!-- Voucher -->
      <div class="card p-4 space-y-3">
        <h2 class="font-semibold">Mã giảm giá</h2>
        <div class="flex gap-2">
          <input v-model="voucherCode" type="text" placeholder="Nhập mã voucher"
                 class="input flex-1" :disabled="voucherApplied" />
          <button @click="applyVoucher" :disabled="applyingVoucher || voucherApplied"
                  class="btn-ghost shrink-0">
            {{ voucherApplied ? '✓' : 'Áp dụng' }}
          </button>
        </div>
        <p v-if="voucherError" class="text-red-400 text-xs">{{ voucherError }}</p>
        <p v-if="discount > 0" class="text-green-400 text-sm">
          Giảm: {{ formatPrice(discount) }}
        </p>
      </div>

      <!-- Payment method -->
      <div class="card p-4 space-y-3">
        <h2 class="font-semibold">Phương thức thanh toán</h2>
        <div class="grid grid-cols-2 gap-2">
          <label v-for="m in paymentMethods" :key="m.value"
                 :class="['flex items-center gap-3 p-3 rounded-lg border cursor-pointer transition-all',
                          paymentMethod === m.value
                            ? 'border-brand bg-brand/10'
                            : 'border-surface-border hover:border-surface-hover']">
            <input type="radio" v-model="paymentMethod" :value="m.value" class="sr-only" />
            <span class="text-xl">{{ m.icon }}</span>
            <span class="text-sm font-medium">{{ m.label }}</span>
          </label>
        </div>
      </div>

      <!-- Total + submit -->
      <div class="card p-4">
        <div class="flex justify-between mb-1 text-sm text-gray-400">
          <span>Tạm tính</span>
          <span>{{ formatPrice(bookingStore.totalPrice) }}</span>
        </div>
        <div v-if="discount > 0" class="flex justify-between mb-1 text-sm text-green-400">
          <span>Giảm giá</span>
          <span>-{{ formatPrice(discount) }}</span>
        </div>
        <div class="flex justify-between font-bold text-lg border-t border-surface-border pt-2 mt-2">
          <span>Tổng cộng</span>
          <span class="text-brand">{{ formatPrice(bookingStore.totalPrice - discount) }}</span>
        </div>

        <button @click="submitBooking" :disabled="submitting"
                class="btn-primary w-full mt-4 py-3 text-base">
          <span v-if="submitting" class="flex items-center gap-2">
            <div class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            Đang xử lý...
          </span>
          <span v-else>Đặt vé — {{ formatPrice(bookingStore.totalPrice - discount) }}</span>
        </button>
        <p v-if="submitError" class="text-red-400 text-xs mt-2 text-center">{{ submitError }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { bookingApi }      from '@/api/bookings'
import { useBookingStore } from '@/stores/booking'

const router       = useRouter()
const bookingStore = useBookingStore()

const voucherCode     = ref('')
const voucherApplied  = ref(false)
const applyingVoucher = ref(false)
const voucherError    = ref('')
const discount        = ref(0)
const paymentMethod   = ref('VNPAY')
const submitting      = ref(false)
const submitError     = ref('')

const paymentMethods = [
  { value: 'VNPAY',         label: 'VNPay',     icon: '💳' },
  { value: 'MOMO',          label: 'MoMo',      icon: '🟣' },
  { value: 'ZALOPAY',       label: 'ZaloPay',   icon: '🔵' },
  { value: 'BANK_TRANSFER', label: 'Ngân hàng', icon: '🏦' }
]

async function applyVoucher(): Promise<void> {
  if (!voucherCode.value.trim()) return
  applyingVoucher.value = true
  voucherError.value = ''
  try {
    // Validation happens server-side during booking creation; preview discount locally
    // The real validation calls PromotionService.validateVoucher via gRPC
    voucherApplied.value = true
  } catch (e) {
    voucherError.value = (e as Error).message
  } finally {
    applyingVoucher.value = false
  }
}

async function submitBooking(): Promise<void> {
  if (!bookingStore.showtime) return
  submitting.value = true
  submitError.value = ''
  try {
    const payload = {
      showtimeId: bookingStore.showtime.id,
      seatIds:    bookingStore.selectedList.map(s => s.seatId),
      paymentMethod: paymentMethod.value,
      ...(voucherApplied.value && voucherCode.value ? { voucherCode: voucherCode.value } : {})
    }
    const booking = await bookingApi.create(payload)
    bookingStore.reset()
    router.push({ name: 'BookingConfirmation', params: { id: booking.bookingId } })
  } catch (e) {
    submitError.value = (e as Error).message
  } finally {
    submitting.value = false
  }
}

function formatPrice(p: number): string {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p)
}
function formatCategory(cat: string): string {
  return ({ STANDARD: 'Thường', VIP: 'VIP', COUPLE: 'Couple' } as Record<string, string>)[cat] ?? cat
}
</script>
