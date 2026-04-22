import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { showtimeApi } from '@/api/showtimes'

export function useSeatMap(showtimeId) {
  const seats    = ref([])   // SeatStatusDto[]
  const loading  = ref(true)
  const error    = ref(null)
  let   stompClient = null

  async function load() {
    try {
      loading.value = true
      seats.value = await showtimeApi.getSeatStatus(showtimeId)
    } catch (e) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  function connectWs() {
    stompClient = new Client({
      webSocketFactory: () => new SockJS('/ws/showtime/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        stompClient.subscribe(`/topic/showtime/${showtimeId}/seats`, msg => {
          const update = JSON.parse(msg.body)   // { seatId, status, bookingId }
          const idx = seats.value.findIndex(s => s.seatId === update.seatId)
          if (idx !== -1) {
            seats.value[idx] = { ...seats.value[idx], status: update.status }
          }
        })
      },
      onStompError: () => { /* silently degrade */ }
    })
    stompClient.activate()
  }

  function disconnect() {
    stompClient?.deactivate()
  }

  function seatsByRow() {
    const map = new Map()
    for (const s of seats.value) {
      const row = s.seatCode.replace(/\d+$/, '')
      if (!map.has(row)) map.set(row, [])
      map.get(row).push(s)
    }
    // Sort seats within each row by column number
    map.forEach(row => row.sort((a, b) => {
      const na = parseInt(a.seatCode.replace(/\D/g, ''))
      const nb = parseInt(b.seatCode.replace(/\D/g, ''))
      return na - nb
    }))
    return [...map.entries()].sort(([a], [b]) => a.localeCompare(b))
  }

  onUnmounted(disconnect)

  return { seats, loading, error, load, connectWs, disconnect, seatsByRow }
}
