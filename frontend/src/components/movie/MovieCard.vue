<template>
  <RouterLink :to="{ name: 'MovieDetail', params: { id: movie.id } }"
              class="group card overflow-hidden hover:border-brand/50 transition-all duration-200 hover:-translate-y-1 block">
    <!-- Poster -->
    <div class="relative aspect-[2/3] overflow-hidden bg-surface-hover">
      <img
        v-if="movie.posterUrl"
        :src="movie.posterUrl"
        :alt="movie.title"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
        loading="lazy"
      />
      <div v-else class="w-full h-full flex items-center justify-center text-gray-600">
        <svg class="w-12 h-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1"
                d="M7 4v16M17 4v16M3 8h4m10 0h4M3 12h18M3 16h4m10 0h4M4 20h16a1 1 0 001-1V5a1 1 0 00-1-1H4a1 1 0 00-1 1v14a1 1 0 001 1z"/>
        </svg>
      </div>

      <!-- Age rating badge -->
      <span v-if="movie.ageRating"
            :class="['absolute top-2 left-2 badge font-bold', ageBadgeColor]">
        {{ movie.ageRating }}
      </span>

      <!-- IMDB score -->
      <div v-if="movie.imdbScore"
           class="absolute bottom-2 right-2 flex items-center gap-1 bg-black/70 rounded px-1.5 py-0.5">
        <svg class="w-3 h-3 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
          <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
        </svg>
        <span class="text-yellow-400 text-xs font-bold">{{ movie.imdbScore }}</span>
      </div>
    </div>

    <!-- Info -->
    <div class="p-3">
      <h3 class="font-semibold text-sm leading-snug line-clamp-2 group-hover:text-brand transition-colors">
        {{ movie.title }}
      </h3>
      <div class="mt-1.5 flex items-center gap-2 text-xs text-gray-500">
        <span v-if="movie.durationMinutes">{{ movie.durationMinutes }} phút</span>
        <span v-if="movie.durationMinutes && movie.language">·</span>
        <span v-if="movie.language">{{ movie.language }}</span>
      </div>
      <div v-if="movie.genres?.length" class="mt-2 flex flex-wrap gap-1">
        <span v-for="g in movie.genres.slice(0,2)" :key="g"
              class="badge bg-surface-hover text-gray-400 text-[10px]">{{ g }}</span>
      </div>
    </div>
  </RouterLink>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ movie: { type: Object, required: true } })
const ageBadgeColor = computed(() => {
  const r = props.movie.ageRating
  if (r === 'C18') return 'bg-red-600 text-white'
  if (r === 'C16') return 'bg-orange-500 text-white'
  if (r === 'C13') return 'bg-yellow-500 text-black'
  return 'bg-green-600 text-white'
})
</script>
