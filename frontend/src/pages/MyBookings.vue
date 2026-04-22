<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold mb-6">Lịch sử đặt vé</h1>

    <!-- Filter tabs -->
    <div class="flex gap-2 mb-6 border-b border-surface-border">
      <button v-for="tab in tabs" :key="tab.value"
              @click="activeTab = tab.value"
              :class="['px-4 py-2.5 text-sm font-medium -mb-px border-b-2 transition-colors',
                       activeTab === tab.value
                         ? 'border-brand text-white'
                         : 'border-transparent text-gray-500 hover:text-gray-300']">
        {{ tab.label }}
      </button>
    </div>

    <Spinner v-if="loading" wrap />

    <div v-else-if="bookings.length" class="space-y-3">
      <div v-for="b in bookings" :key="b.bookingId"
           class="card p-4 hover:border-surface-hover transition-colors">
        <div class="flex items-start justify-between gap-3">
          <div class="space-y-1 flex-1 min-w-0">
            <p class="font-semibold line-clamp-1">{{ b.movieTitle || 'Phim' }}</p>
            <p class="text-sm text-gray-400">
              {{ b.cinemaName }} · {{ dayjs(b.showtimeStart).format('HH:mm DD/MM/YYYY') }}
            </p>
            <p class="text-xs text-gray-500">
              {{ b.seats?.join(', ') }}  ·  {{ b.itemCount }} ghế
            </p>
          </div>
          <div class="text-right shrink-0">
            <span :class="['badge', statusBadge(b.status)]">{{ statusLabel(b.status) }}</span>
            <p class="text-brand font-bold mt-1">{{ formatPrice(b.totalAmount) }}</p>
          </div>
        </div>

        <div class="flex gap-2 mt-3 pt-3 border-t border-surface-border">
          <RouterLink :to="{ name: 'BookingConfirmation', params: { id: b.bookingId } }"
                      class="btn-ghost text-xs py-1.5 px-3">
            Chi tiết
          </RouterLink>
          <RouterLink v-if="b.status === 'CONFIRMED'"
                      :to="{ name: 'MyTickets' }"
                      class="btn-ghost text-xs py-1.5 px-3">
            E-Ticket
          </RouterLink>
          <button v-if="canCancel(b.status)"
                  @click="confirmCancel(b)"
                  class="ml-auto text-xs text-red-400 hover:text-red-300 transition-colors">
            Hủy đặt vé
          </button>
        </div>
      </div>

      <!-- Pagination -->
      <div class="flex justify-center gap-2 mt-6">
        <button @click="loadPage(page - 1)" :disabled="page === 0" class="btn-ghost px-3 py-1.5 text-sm">
          ← Trước
        </button>
        <span class="px-3 py-1.5 text-sm text-gray-400">Trang {{ page + 1 }}</span>
        <button @click="loadPage(page + 1)" :disabled="!hasMore" class="btn-ghost px-3 py-1.5 text-sm">
          Sau →
        </button>
      </div>
    </div>

    <div v-else class="text-center py-16 text-gray-500">
      <p class="text-4xl mb-3">🎬</p>
      <p>Bạn chưa có đặt vé nào.</p>
      <RouterLink to="/" class="btn-primary mt-4 inline-flex">Đặt vé ngay</RouterLink>
    </div>

    <!-- Cancel confirm modal -->
    <Modal v-model="showCancelModal" title="Hủy đặt vé" size="sm">
      <p class="text-gray-300 text-sm mb-4">
        Bạn có chắc chắn muốn hủy đặt vé này? Hành động này không thể hoàn tác.
      </p>
      <div class="flex gap-2">
        <button @click="showCancelModal = false" class="btn-ghost flex-1">Không</button>
        <button @click="doCancel" :disabled="cancelling" class="btn-primary flex-1 bg-red-600 hover:bg-red-500">
          {{ cancelling ? 'Đang hủy...' : 'Xác nhận hủy' }}
        </button>
      </div>
    </Modal>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import dayjs from 'dayjs'
import { bookingApi } from '@/api/bookings'
import Modal   from '@/components/common/Modal.vue'
import Spinner from '@/components/common/Spinner.vue'

const bookings = ref([])
const loading  = ref(true)
const page     = ref(0)
const hasMore  = ref(false)
const activeTab = ref('')

const showCancelModal = ref(false)
const cancelling      = ref(false)
const cancelTarget    = ref(null)

const tabs = [
  { label: 'Tất cả',    value: '' },
  { label: 'Đã xác nhận', value: 'CONFIRMED' },
  { label: 'Chờ thanh toán', value: 'PENDING_PAYMENT' },
  { label: 'Đã hủy',    value: 'CANCELLED' }
]

async function loadPage(p) {
  loading.value = true
  try {
    const result = await bookingApi.listMine(p, 10, activeTab.value || undefined)
    // Handle both paged and plain array responses
    if (Array.isArray(result)) {
      bookings.value = result
      hasMore.value  = false
    } else {
      bookings.value = result.content || result
      hasMore.value  = result.content?.length === 10
      page.value     = p
    }
  } finally {
    loading.value = false
  }
}

watch(activeTab, () => loadPage(0))
onMounted(() => loadPage(0))

function confirmCancel(booking) {
  cancelTarget.value = booking
  showCancelModal.value = true
}

async function doCancel() {
  if (!cancelTarget.value) return
  cancelling.value = true
  try {
    await bookingApi.cancel(cancelTarget.value.bookingId)
    showCancelModal.value = false
    loadPage(page.value)
  } finally {
    cancelling.value = false
  }
}

function canCancel(status) {
  return ['PENDING_PAYMENT', 'PROCESSING'].includes(status)
}

function statusLabel(s) {
  return {
    CONFIRMED: 'Đã xác nhận', PENDING_PAYMENT: 'Chờ thanh toán',
    PROCESSING: 'Đang xử lý', CANCELLED: 'Đã hủy', FAILED: 'Thất bại'
  }[s] || s
}

function statusBadge(s) {
  return {
    CONFIRMED:       'bg-green-900 text-green-300',
    PENDING_PAYMENT: 'bg-yellow-900 text-yellow-400',
    PROCESSING:      'bg-blue-900 text-blue-300',
    CANCELLED:       'bg-gray-700 text-gray-400',
    FAILED:          'bg-red-900 text-red-400'
  }[s] || 'bg-gray-700 text-gray-400'
}

function formatPrice(p) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p)
}
</script>
