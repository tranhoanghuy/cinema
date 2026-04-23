<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold mb-6">E-Ticket của tôi</h1>

    <Spinner v-if="loading" wrap />

    <div v-else-if="tickets.length" class="grid gap-4 sm:grid-cols-2">
      <TicketCard v-for="t in tickets" :key="t.id" :ticket="t" />
    </div>

    <div v-else class="text-center py-16 text-gray-500">
      <p class="text-4xl mb-3">🎟️</p>
      <p>Bạn chưa có vé nào.</p>
      <RouterLink to="/" class="btn-primary mt-4 inline-flex">Đặt vé ngay</RouterLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ticketApi } from '@/api/tickets'
import TicketCard from '@/components/ticket/TicketCard.vue'
import Spinner    from '@/components/common/Spinner.vue'
import type { Ticket } from '@/types'

const tickets = ref<Ticket[]>([])
const loading = ref(true)

onMounted(async () => {
  try { tickets.value = await ticketApi.listMine() }
  finally { loading.value = false }
})
</script>
