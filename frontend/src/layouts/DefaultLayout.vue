<template>
  <div class="min-h-screen flex flex-col bg-surface">
    <!-- Navbar -->
    <header class="sticky top-0 z-50 bg-surface/90 backdrop-blur-sm border-b border-surface-border">
      <div class="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between gap-4">
        <!-- Logo -->
        <RouterLink to="/" class="flex items-center gap-2 shrink-0">
          <span class="text-2xl font-bold tracking-tight">
            <span class="text-brand">Cine</span><span class="text-white">Tix</span>
          </span>
        </RouterLink>

        <!-- Search -->
        <div class="hidden md:flex flex-1 max-w-md relative">
          <input
            v-model="searchQ"
            @keyup.enter="doSearch"
            type="text"
            placeholder="Tìm phim..."
            class="input pl-10"
          />
          <svg class="absolute left-3 top-2.5 w-4 h-4 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
          </svg>
        </div>

        <!-- Nav links -->
        <nav class="hidden md:flex items-center gap-1">
          <RouterLink to="/" class="nav-link" active-class="text-white">Phim</RouterLink>
          <RouterLink to="/cinemas" class="nav-link" active-class="text-white">Rạp</RouterLink>
        </nav>

        <!-- Auth -->
        <div class="flex items-center gap-2">
          <template v-if="auth.isAuthenticated">
            <RouterLink to="/my/bookings" class="nav-link hidden sm:block">Vé của tôi</RouterLink>
            <div class="relative" ref="dropdownRef">
              <button @click="dropdownOpen = !dropdownOpen"
                      class="flex items-center gap-2 px-3 py-1.5 rounded-lg hover:bg-surface-hover transition-colors">
                <div class="w-7 h-7 rounded-full bg-brand flex items-center justify-center text-xs font-bold">
                  {{ auth.fullName.charAt(0).toUpperCase() }}
                </div>
                <span class="hidden sm:block text-sm text-gray-300 max-w-[100px] truncate">{{ auth.fullName }}</span>
              </button>
              <div v-if="dropdownOpen"
                   class="absolute right-0 top-full mt-1 w-48 card shadow-xl py-1 animate-slide-up">
                <RouterLink to="/my/profile" @click="dropdownOpen=false"
                            class="dropdown-item">Hồ sơ</RouterLink>
                <RouterLink to="/my/tickets" @click="dropdownOpen=false"
                            class="dropdown-item">E-Ticket</RouterLink>
                <div class="border-t border-surface-border my-1" />
                <button @click="auth.logout()" class="dropdown-item w-full text-left text-red-400">
                  Đăng xuất
                </button>
              </div>
            </div>
          </template>
          <button v-else @click="auth.login()" class="btn-primary text-sm">
            Đăng nhập
          </button>
        </div>
      </div>
    </header>

    <!-- Main content -->
    <main class="flex-1">
      <RouterView v-slot="{ Component }">
        <Transition name="fade" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </main>

    <!-- Footer -->
    <footer class="border-t border-surface-border py-8 mt-12">
      <div class="max-w-7xl mx-auto px-4 text-center text-gray-500 text-sm">
        © 2025 CineTix. Nền tảng đặt vé xem phim trực tuyến.
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { onClickOutside } from '@vueuse/core'
import { useAuthStore } from '@/stores/auth'

const auth         = useAuthStore()
const router       = useRouter()
const searchQ      = ref('')
const dropdownOpen = ref(false)
const dropdownRef  = ref<HTMLElement | null>(null)

onClickOutside(dropdownRef, () => { dropdownOpen.value = false })

function doSearch(): void {
  if (!searchQ.value.trim()) return
  router.push({ name: 'Home', query: { q: searchQ.value.trim() } })
}
</script>

<style scoped>
.nav-link {
  @apply px-3 py-1.5 rounded-lg text-sm text-gray-400 hover:text-white hover:bg-surface-hover transition-colors;
}
.dropdown-item {
  @apply block px-4 py-2 text-sm text-gray-300 hover:bg-surface-hover hover:text-white transition-colors;
}
.fade-enter-active,
.fade-leave-active { transition: opacity 0.15s ease; }
.fade-enter-from,
.fade-leave-to   { opacity: 0; }
</style>
