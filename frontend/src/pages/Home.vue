<template>
  <div class="max-w-7xl mx-auto px-4 py-8 space-y-12">
    <!-- Hero banner -->
    <section v-if="!searchQuery" class="relative rounded-2xl overflow-hidden h-64 md:h-80 bg-surface-card flex items-end">
      <div v-if="heroMovie" class="absolute inset-0">
        <img :src="heroMovie.backdropUrl || heroMovie.posterUrl" :alt="heroMovie.title"
             class="w-full h-full object-cover opacity-50" />
        <div class="absolute inset-0 bg-gradient-to-t from-black via-black/40 to-transparent" />
      </div>
      <div class="relative px-8 pb-8 space-y-2">
        <p class="text-xs text-brand font-semibold uppercase tracking-widest">Đang chiếu</p>
        <h1 class="text-2xl md:text-4xl font-bold leading-tight">{{ heroMovie?.title || 'CineTix' }}</h1>
        <p v-if="heroMovie" class="text-gray-300 text-sm line-clamp-2 max-w-lg">{{ heroMovie.description }}</p>
        <RouterLink v-if="heroMovie"
                    :to="{ name: 'MovieDetail', params: { id: heroMovie.id } }"
                    class="btn-primary mt-2 inline-flex">
          Đặt vé ngay
        </RouterLink>
      </div>
    </section>

    <!-- Search results -->
    <section v-if="searchQuery">
      <h2 class="text-xl font-bold mb-4">Kết quả cho "{{ searchQuery }}"</h2>
      <Spinner v-if="searching" wrap />
      <div v-else-if="searchResults.length" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        <MovieCard v-for="m in searchResults" :key="m.id" :movie="m" />
      </div>
      <p v-else class="text-gray-500 py-8 text-center">Không tìm thấy phim nào.</p>
    </section>

    <!-- Now showing -->
    <section v-if="!searchQuery">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-xl font-bold">Đang chiếu</h2>
        <span class="text-sm text-gray-500">{{ nowShowing.length }} phim</span>
      </div>
      <Spinner v-if="loadingNow" wrap />
      <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        <MovieCard v-for="m in nowShowing" :key="m.id" :movie="m" />
      </div>
    </section>

    <!-- Coming soon -->
    <section v-if="!searchQuery && comingSoon.length">
      <h2 class="text-xl font-bold mb-4">Sắp chiếu</h2>
      <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        <MovieCard v-for="m in comingSoon" :key="m.id" :movie="m" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { movieApi } from '@/api/movies'
import MovieCard from '@/components/movie/MovieCard.vue'
import Spinner   from '@/components/common/Spinner.vue'
import type { Movie } from '@/types'

const route         = useRoute()
const nowShowing    = ref<Movie[]>([])
const comingSoon    = ref<Movie[]>([])
const searchResults = ref<Movie[]>([])
const loadingNow    = ref(true)
const searching     = ref(false)
const searchQuery   = computed(() => (route.query.q as string) || '')
const heroMovie     = computed<Movie | null>(() => nowShowing.value[0] ?? null)

onMounted(async () => {
  try {
    [nowShowing.value, comingSoon.value] = await Promise.all([
      movieApi.listNowShowing(),
      movieApi.listComingSoon()
    ])
  } finally {
    loadingNow.value = false
  }
})

watch(searchQuery, async q => {
  if (!q) { searchResults.value = []; return }
  searching.value = true
  try { searchResults.value = await movieApi.search(q) }
  finally { searching.value = false }
}, { immediate: true })
</script>
