import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { showtimeApi } from '@/api/showtimes'
import type { SeatStatus } from '@/types'

export function useSeatMap(showtimeId: string) {
  const seats   = ref<SeatStatus[]>([])
  const loading = ref(true)
  const error   = ref<string | null>(null)
  let stompClient: Client | null = null

  async function load(): Promise<void> {
    try {
      loading.value = true
      seats.value = await showtimeApi.getSeatStatus(showtimeId)
    } catch (e) {
      error.value = (e as Error).message
    } finally {
      loading.value = false
    }
  }

  function connectWs(): void {
    stompClient = new Client({
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      webSocketFactory: () => new SockJS('/ws/showtime/ws') as any,
      reconnectDelay: 5000,
      onConnect: () => {
        stompClient!.subscribe(`/topic/showtime/${showtimeId}/seats`, msg => {
          const update = JSON.parse(msg.body) as Pick<SeatStatus, 'seatId' | 'status'>
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

  function disconnect(): void {
    stompClient?.deactivate()
  }

  function seatsByRow(): [string, SeatStatus[]][] {
    const map = new Map<string, SeatStatus[]>()
    for (const s of seats.value) {
      const row = s.seatCode.replace(/\d+$/, '')
      if (!map.has(row)) map.set(row, [])
      map.get(row)!.push(s)
    }
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
