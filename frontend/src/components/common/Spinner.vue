<template>
  <div :class="['flex justify-center items-center', wrapClass]">
    <div :class="['border-4 border-brand border-t-transparent rounded-full animate-spin', sizeClass]" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({
  size: { type: String, default: 'md' },   // sm | md | lg
  wrap: { type: Boolean, default: false }
})
const sizeClass = computed(() => ({ sm: 'w-5 h-5', md: 'w-8 h-8', lg: 'w-12 h-12' }[props.size]))
const wrapClass = computed(() => props.wrap ? 'py-16' : '')
</script>
