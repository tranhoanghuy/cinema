import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Showtime, Movie, SeatStatus } from '@/types'

export const useBookingStore = defineStore('booking', () => {
  const showtime = ref<Showtime | null>(null)
  const movie    = ref<Movie | null>(null)
  const selected = ref<Map<string, SeatStatus>>(new Map())

  const selectedList = computed(() => [...selected.value.values()])
  const totalPrice   = computed(() =>
    selectedList.value.reduce((sum, s) => sum + Number(s.unitPrice), 0))
  const seatCount    = computed(() => selected.value.size)

  function setContext(showtimeData: Showtime, movieData: Movie | null): void {
    showtime.value = showtimeData
    movie.value    = movieData
    selected.value = new Map()
  }

  function toggleSeat(seat: SeatStatus): void {
    if (selected.value.has(seat.seatId)) {
      selected.value.delete(seat.seatId)
    } else {
      selected.value.set(seat.seatId, seat)
    }
  }

  function clearSelection(): void {
    selected.value = new Map()
  }

  function reset(): void {
    showtime.value = null
    movie.value    = null
    selected.value = new Map()
  }

  return { showtime, movie, selected, selectedList, totalPrice, seatCount,
           setContext, toggleSeat, clearSelection, reset }
})
