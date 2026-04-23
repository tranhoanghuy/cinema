<template>
  <div :class="['flex justify-center items-center', wrapClass]">
    <div :class="['border-4 border-brand border-t-transparent rounded-full animate-spin', sizeClass]" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  size?: 'sm' | 'md' | 'lg'
  wrap?: boolean
}>(), {
  size: 'md',
  wrap: false
})

const sizeClass = computed(() => ({ sm: 'w-5 h-5', md: 'w-8 h-8', lg: 'w-12 h-12' }[props.size!]))
const wrapClass = computed(() => props.wrap ? 'py-16' : '')
</script>
