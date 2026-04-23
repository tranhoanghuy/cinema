<template>
  <div>
    <!-- Backdrop -->
    <div v-if="movie" class="relative h-64 md:h-96 overflow-hidden">
      <img v-if="movie.backdropUrl || movie.posterUrl"
           :src="movie.backdropUrl || movie.posterUrl"
           :alt="movie.title"
           class="w-full h-full object-cover opacity-40" />
      <div class="absolute inset-0 bg-gradient-to-t from-surface via-surface/60 to-transparent" />
    </div>

    <div class="max-w-7xl mx-auto px-4 -mt-24 relative pb-12">
      <Spinner v-if="loading" wrap />
      <div v-else-if="movie" class="flex flex-col md:flex-row gap-8">
        <!-- Poster -->
        <div class="shrink-0 w-40 md:w-56">
          <img v-if="movie.posterUrl" :src="movie.posterUrl" :alt="movie.title"
               class="w-full rounded-xl shadow-2xl" />
        </div>

        <!-- Info -->
        <div class="flex-1 space-y-4">
          <h1 class="text-3xl md:text-4xl font-bold">{{ movie.title }}</h1>
          <p v-if="movie.originalTitle !== movie.title" class="text-gray-400 italic">{{ movie.originalTitle }}</p>

          <div class="flex flex-wrap gap-3 text-sm">
            <span v-if="movie.ageRating" class="badge bg-red-900 text-red-300">{{ movie.ageRating }}</span>
            <span v-if="movie.durationMinutes" class="text-gray-400">{{ movie.durationMinutes }} phút</span>
            <span v-if="movie.language" class="text-gray-400">{{ movie.language }}</span>
            <span v-if="movie.releaseDate" class="text-gray-400">
              {{ dayjs(movie.releaseDate).format('DD/MM/YYYY') }}
            </span>
          </div>

          <!-- Genres -->
          <div v-if="movie.genres?.length" class="flex flex-wrap gap-2">
            <span v-for="g in movie.genres" :key="g"
                  class="badge bg-surface-hover text-gray-300 px-3 py-1">{{ g }}</span>
          </div>

          <!-- IMDB -->
          <div v-if="movie.imdbScore" class="flex items-center gap-2">
            <svg class="w-5 h-5 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
              <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
            </svg>
            <span class="text-yellow-400 font-bold">{{ movie.imdbScore }}</span>
            <span class="text-gray-500 text-sm">/ 10 (IMDb)</span>
          </div>

          <!-- Description -->
          <p v-if="movie.description" class="text-gray-300 leading-relaxed">{{ movie.description }}</p>

          <div class="grid grid-cols-2 gap-3 text-sm">
            <div v-if="movie.director">
              <p class="text-gray-500 text-xs uppercase tracking-wide mb-0.5">Đạo diễn</p>
              <p>{{ movie.director }}</p>
            </div>
            <div v-if="movie.castList">
              <p class="text-gray-500 text-xs uppercase tracking-wide mb-0.5">Diễn viên</p>
              <p class="line-clamp-2">{{ movie.castList }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Showtimes section -->
      <div v-if="movie" class="mt-10">
        <h2 class="text-xl font-bold mb-6">Lịch chiếu</h2>

        <Spinner v-if="loadingShowtimes" />
        <div v-else-if="groupedShowtimes.size" class="space-y-6">
          <div v-for="[cinema, times] in groupedShowtimes" :key="cinema" class="card p-4">
            <h3 class="font-semibold mb-3 text-brand">{{ cinema }}</h3>
            <div class="flex flex-wrap gap-2">
              <RouterLink
                v-for="st in times"
                :key="st.id"
                :to="{ name: 'SeatSelection', params: { id: st.id } }"
                class="px-4 py-2 rounded-lg border border-surface-border hover:border-brand hover:bg-brand/10 transition-all text-center"
              >
                <p class="font-semibold text-sm">{{ dayjs(st.startTime).format('HH:mm') }}</p>
                <p class="text-xs text-gray-500 mt-0.5">{{ st.format }} · {{ st.availableSeats }} ghế</p>
              </RouterLink>
            </div>
          </div>
        </div>
        <p v-else class="text-gray-500 py-4">Không có lịch chiếu nào.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import { movieApi }    from '@/api/movies'
import { showtimeApi } from '@/api/showtimes'
import Spinner from '@/components/common/Spinner.vue'
import type { Movie, Showtime } from '@/types'

const route            = useRoute()
const movie            = ref<Movie | null>(null)
const showtimes        = ref<Showtime[]>([])
const loading          = ref(true)
const loadingShowtimes = ref(true)

const groupedShowtimes = computed(() => {
  const map = new Map<string, Showtime[]>()
  for (const st of showtimes.value) {
    if (st.movieId !== movie.value?.id) continue
    if (!map.has(st.cinemaName)) map.set(st.cinemaName, [])
    map.get(st.cinemaName)!.push(st)
  }
  return map
})

onMounted(async () => {
  try {
    [movie.value, showtimes.value] = await Promise.all([
      movieApi.getById(route.params.id as string),
      showtimeApi.list()
    ])
  } finally {
    loading.value = false
    loadingShowtimes.value = false
  }
})
</script>
