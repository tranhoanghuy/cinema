<template>
  <div class="max-w-5xl mx-auto px-4 py-8">
    <Spinner v-if="loading" wrap />

    <div v-else-if="showtime">
      <!-- Show info header -->
      <div class="flex flex-col sm:flex-row gap-4 mb-8 card p-4">
        <img v-if="posterUrl" :src="posterUrl" alt="" class="w-16 rounded-lg object-cover self-start" />
        <div class="flex-1 space-y-1">
          <h1 class="text-lg font-bold">{{ showtime.movieTitle }}</h1>
          <p class="text-sm text-gray-400">
            {{ showtime.cinemaName }} · {{ showtime.screenName }}
          </p>
          <p class="text-sm text-brand font-medium">
            {{ dayjs(showtime.startTime).format('HH:mm — dddd, DD/MM/YYYY') }}
          </p>
          <div class="flex gap-2 text-xs">
            <span class="badge bg-surface-hover text-gray-400">{{ showtime.format }}</span>
            <span class="text-gray-500">{{ showtime.availableSeats }} ghế trống</span>
          </div>
        </div>
      </div>

      <!-- Seat map -->
      <div class="card p-6 mb-6">
        <SeatMap
          :seats-by-row="seatMap.seatsByRow()"
          :selected-ids="bookingStore.selected"
          @toggle="bookingStore.toggleSeat"
        />
      </div>

      <!-- Selection summary + CTA -->
      <Transition name="slide-up">
        <div v-if="bookingStore.seatCount > 0"
             class="sticky bottom-4 card p-4 flex flex-col sm:flex-row items-center justify-between gap-3 shadow-2xl border-brand/40">
          <div>
            <p class="text-sm text-gray-400">Đã chọn {{ bookingStore.seatCount }} ghế</p>
            <p class="font-bold text-lg">{{ formatPrice(bookingStore.totalPrice) }}</p>
            <p class="text-xs text-gray-500">
              {{ bookingStore.selectedList.map(s => s.seatCode).join(', ') }}
            </p>
          </div>
          <RouterLink :to="{ name: 'Checkout' }" class="btn-primary w-full sm:w-auto">
            Tiếp tục đặt vé →
          </RouterLink>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import 'dayjs/locale/vi'
dayjs.locale('vi')

import { showtimeApi } from '@/api/showtimes'
import { movieApi }    from '@/api/movies'
import { useSeatMap }  from '@/composables/useSeatMap'
import { useBookingStore } from '@/stores/booking'
import SeatMap from '@/components/seat/SeatMap.vue'
import Spinner from '@/components/common/Spinner.vue'

const route  = useRoute()
const showtime  = ref(null)
const posterUrl = ref(null)
const loading   = ref(true)

const seatMap      = useSeatMap(route.params.id)
const bookingStore = useBookingStore()

onMounted(async () => {
  try {
    showtime.value = await showtimeApi.getById(route.params.id)
    bookingStore.setContext(showtime.value, null)

    // Load seats and movie poster in parallel
    const [, movieData] = await Promise.all([
      seatMap.load(),
      movieApi.getById(showtime.value.movieId).catch(() => null)
    ])
    if (movieData) posterUrl.value = movieData.posterUrl

    seatMap.connectWs()
  } finally {
    loading.value = false
  }
})

function formatPrice(p) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p)
}
</script>
