<template>
  <span v-if="iconExists" class="feather-icon-wrap" v-html="svgContent"></span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import feather from 'feather-icons'

const props = withDefaults(defineProps<{
  type?: string
  size?: number | string
  stroke?: string
  'stroke-width'?: number | string
}>(), {
  type: 'feather',
  size: 24,
  stroke: 'currentColor',
  'stroke-width': 2,
})

const iconExists = computed(() => {
  return !!(props.type && feather.icons[props.type])
})

const svgContent = computed(() => {
  if (!iconExists.value) return ''
  const icon = feather.icons[props.type!]
  const attrs: Record<string, string> = {
    width: String(props.size),
    height: String(props.size),
    stroke: props.stroke || 'currentColor',
    'stroke-width': String(props['stroke-width']),
    fill: 'none',
    'stroke-linecap': 'round',
    'stroke-linejoin': 'round',
  }
  // Wrap the icon SVG contents with our attributes
  return icon.toSvg(attrs)
})
</script>

<style scoped>
.feather-icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
</style>
