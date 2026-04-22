<template>
  <div v-if="auth.ready">
    <RouterView />
  </div>
  <div v-else class="min-h-screen flex items-center justify-center bg-surface">
    <div class="flex flex-col items-center gap-4">
      <div class="w-10 h-10 border-4 border-brand border-t-transparent rounded-full animate-spin" />
      <p class="text-gray-400 text-sm">Đang khởi động...</p>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
const auth = useAuthStore()
</script>
