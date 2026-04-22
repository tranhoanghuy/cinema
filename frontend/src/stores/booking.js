import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useBookingStore = defineStore('booking', () => {
  // Showtime context
  const showtime  = ref(null)
  const movie     = ref(null)

  // Selected seats: Map<seatId, SeatStatusDto>
  const selected  = ref(new Map())

  const selectedList = computed(() => [...selected.value.values()])
  const totalPrice   = computed(() =>
    selectedList.value.reduce((sum, s) => sum + Number(s.unitPrice), 0))
  const seatCount    = computed(() => selected.value.size)

  function setContext(showtimeData, movieData) {
    showtime.value = showtimeData
    movie.value    = movieData
    selected.value = new Map()
  }

  function toggleSeat(seat) {
    if (selected.value.has(seat.seatId)) {
      selected.value.delete(seat.seatId)
    } else {
      selected.value.set(seat.seatId, seat)
    }
  }

  function clearSelection() {
    selected.value = new Map()
  }

  function reset() {
    showtime.value = null
    movie.value    = null
    selected.value = new Map()
  }

  return { showtime, movie, selected, selectedList, totalPrice, seatCount,
           setContext, toggleSeat, clearSelection, reset }
})
