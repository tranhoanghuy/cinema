<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black/70 backdrop-blur-sm" @click="$emit('update:modelValue', false)" />
        <!-- Panel -->
        <div :class="['relative w-full card p-6 shadow-2xl animate-slide-up', maxWidthClass]">
          <button
            v-if="closable"
            @click="$emit('update:modelValue', false)"
            class="absolute top-4 right-4 text-gray-500 hover:text-white transition-colors"
          >
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
          <h3 v-if="title" class="text-lg font-semibold mb-4 pr-6">{{ title }}</h3>
          <slot />
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({
  modelValue: Boolean,
  title:      String,
  closable:   { type: Boolean, default: true },
  size:       { type: String, default: 'md' }   // sm | md | lg | xl
})
defineEmits(['update:modelValue'])
const maxWidthClass = computed(() => ({
  sm: 'max-w-sm', md: 'max-w-md', lg: 'max-w-lg', xl: 'max-w-xl'
}[props.size]))
</script>

<style scoped>
.modal-enter-active, .modal-leave-active { transition: opacity 0.2s; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>
