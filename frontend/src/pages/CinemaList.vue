<template>
  <div class="max-w-5xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold mb-2">Hệ thống rạp chiếu</h1>

    <!-- City filter -->
    <div class="flex flex-wrap gap-2 mb-6">
      <button v-for="city in cities" :key="city"
              @click="selectedCity = city === selectedCity ? '' : city"
              :class="['px-4 py-1.5 rounded-full text-sm border transition-all',
                       selectedCity === city
                         ? 'bg-brand border-brand text-white'
                         : 'border-surface-border text-gray-400 hover:border-gray-500']">
        {{ city || 'Tất cả' }}
      </button>
    </div>

    <Spinner v-if="loading" wrap />

    <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <div v-for="c in filteredCinemas" :key="c.id"
           class="card p-4 hover:border-surface-hover transition-colors space-y-2">
        <h3 class="font-semibold">{{ c.name }}</h3>
        <p class="text-sm text-gray-400 flex items-start gap-1.5">
          <svg class="w-4 h-4 mt-0.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
          </svg>
          {{ c.address }}, {{ c.city }}
        </p>
        <div v-if="c.phone" class="text-xs text-gray-500">📞 {{ c.phone }}</div>
        <RouterLink :to="{ name: 'Home', query: { cinema: c.id } }"
                    class="btn-ghost text-xs py-1.5 mt-1 w-full">
          Xem lịch chiếu
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { cinemaApi } from '@/api/cinemas'
import Spinner from '@/components/common/Spinner.vue'
import type { Cinema } from '@/types'

const cinemas      = ref<Cinema[]>([])
const loading      = ref(true)
const selectedCity = ref('')

const cities = computed(() => ['', ...new Set(cinemas.value.map(c => c.city).filter(Boolean))])

const filteredCinemas = computed(() =>
  selectedCity.value ? cinemas.value.filter(c => c.city === selectedCity.value) : cinemas.value)

onMounted(async () => {
  try { cinemas.value = await cinemaApi.list() }
  finally { loading.value = false }
})
</script>
