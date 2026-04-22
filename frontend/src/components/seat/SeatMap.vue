<template>
  <div class="w-full overflow-x-auto">
    <!-- Screen indicator -->
    <div class="flex flex-col items-center mb-6 select-none">
      <div class="w-3/4 max-w-xs h-2 bg-gradient-to-b from-white/20 to-transparent rounded-t-full" />
      <p class="text-xs text-gray-500 mt-1 tracking-widest uppercase">Màn hình</p>
    </div>

    <!-- Seat grid -->
    <div class="flex flex-col items-center gap-1.5">
      <div v-for="[row, rowSeats] in seatsByRow" :key="row" class="flex items-center gap-1.5">
        <!-- Row label -->
        <span class="w-5 text-center text-xs text-gray-600 shrink-0 select-none">{{ row }}</span>

        <!-- Seats -->
        <div class="flex gap-1">
          <button
            v-for="seat in rowSeats"
            :key="seat.seatId"
            :disabled="!isSelectable(seat)"
            :title="`${seat.seatCode} — ${formatCategory(seat.category)} — ${formatPrice(seat.unitPrice)}`"
            :class="['seat-btn', seatClass(seat)]"
            @click="$emit('toggle', seat)"
          >
            <span class="text-[9px] leading-none select-none">{{ colNum(seat.seatCode) }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Legend -->
    <div class="flex flex-wrap justify-center gap-4 mt-8 text-xs text-gray-400">
      <div v-for="item in legend" :key="item.label" class="flex items-center gap-1.5">
        <div :class="['w-4 h-4 rounded-sm', item.color]" />
        <span>{{ item.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  seatsByRow:  { type: Array,  required: true },   // [[row, SeatStatusDto[]]]
  selectedIds: { type: Set,    default: () => new Set() }
})
const emit = defineEmits(['toggle'])

const legend = [
  { label: 'Trống',       color: 'bg-seat-available' },
  { label: 'Đang chọn',   color: 'bg-seat-selected' },
  { label: 'Đang giữ',    color: 'bg-seat-held' },
  { label: 'Đã bán',      color: 'bg-seat-booked' },
  { label: 'VIP',         color: 'bg-seat-vip' },
  { label: 'Couple',      color: 'bg-seat-couple' }
]

function isSelectable(seat) {
  return seat.status === 'AVAILABLE'
}

function seatClass(seat) {
  if (props.selectedIds.has(seat.seatId)) return 'bg-seat-selected text-white'
  if (seat.status === 'HELD')      return 'bg-seat-held text-black cursor-not-allowed opacity-60'
  if (seat.status === 'CONFIRMED') return 'bg-seat-booked text-gray-500 cursor-not-allowed'
  if (seat.category === 'VIP')     return 'bg-seat-vip/20 border border-seat-vip text-seat-vip hover:bg-seat-vip hover:text-white'
  if (seat.category === 'COUPLE')  return 'bg-seat-couple/20 border border-seat-couple text-seat-couple hover:bg-seat-couple hover:text-white'
  return 'bg-seat-available/20 border border-seat-available text-seat-available hover:bg-seat-available hover:text-black'
}

function colNum(code) {
  return code.replace(/\D/g, '')
}

function formatCategory(cat) {
  return { STANDARD: 'Thường', VIP: 'VIP', COUPLE: 'Couple' }[cat] || cat
}

function formatPrice(p) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p)
}
</script>

<style scoped>
.seat-btn {
  @apply w-7 h-7 rounded-sm flex items-center justify-center
         transition-all duration-100 cursor-pointer
         disabled:cursor-not-allowed;
}
</style>
