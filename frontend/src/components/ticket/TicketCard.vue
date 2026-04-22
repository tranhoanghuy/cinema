<template>
  <div class="card p-0 overflow-hidden select-none">
    <!-- Header bar -->
    <div class="bg-gradient-to-r from-brand to-brand-light px-4 py-3 flex items-center justify-between">
      <span class="font-bold tracking-wide text-white">CineTix</span>
      <span :class="['badge', statusBadge]">{{ statusLabel }}</span>
    </div>

    <div class="p-4 flex gap-4">
      <!-- QR Code -->
      <div class="shrink-0 w-20 h-20 bg-white rounded-lg p-1 flex items-center justify-center">
        <img
          v-if="ticket.qrCodeBase64"
          :src="`data:image/png;base64,${ticket.qrCodeBase64}`"
          alt="QR Code"
          class="w-full h-full object-contain"
        />
        <div v-else class="text-gray-400 text-xs text-center">QR</div>
      </div>

      <!-- Details -->
      <div class="flex-1 min-w-0 space-y-1.5">
        <p class="font-semibold text-sm leading-tight line-clamp-1">{{ ticket.movieTitle }}</p>
        <div class="text-xs text-gray-400 space-y-0.5">
          <p>{{ ticket.cinemaName }}</p>
          <p>{{ ticket.screenName }} · {{ ticket.seatCode }}</p>
          <p>{{ formatDate(ticket.showtimeStart) }}</p>
        </div>
        <p class="text-xs font-mono text-gray-600">{{ ticket.serialNumber }}</p>
      </div>
    </div>

    <!-- Dashed divider -->
    <div class="mx-4 border-t border-dashed border-surface-border" />

    <div class="px-4 py-2.5 flex items-center justify-between text-xs text-gray-500">
      <span>Booking #{{ ticket.bookingId?.slice(-8) }}</span>
      <span class="font-medium text-white">{{ formatPrice(ticket.unitPrice) }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import dayjs from 'dayjs'

const props = defineProps({ ticket: { type: Object, required: true } })

const statusLabel = computed(() => ({
  ISSUED:  'Hợp lệ',
  USED:    'Đã dùng',
  EXPIRED: 'Hết hạn',
  VOIDED:  'Hủy'
}[props.ticket.status] || props.ticket.status))

const statusBadge = computed(() => ({
  ISSUED:  'bg-green-900 text-green-300',
  USED:    'bg-gray-700 text-gray-400',
  EXPIRED: 'bg-yellow-900 text-yellow-400',
  VOIDED:  'bg-red-900 text-red-400'
}[props.ticket.status] || 'bg-gray-700 text-gray-400'))

function formatDate(ts) {
  return dayjs(ts).format('HH:mm — DD/MM/YYYY')
}
function formatPrice(p) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p)
}
</script>
