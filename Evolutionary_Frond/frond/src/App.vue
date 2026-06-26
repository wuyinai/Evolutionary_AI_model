<script setup lang="ts">
import { RouterView } from 'vue-router'
import { ref, onMounted } from 'vue'
import Toast from '@/components/Toast.vue'
import { setToastInstance } from '@/composables/useToast'

// Toast组件实例引用
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// 在组件挂载时设置toast实例
onMounted(() => {
  if (toastRef.value) {
    setToastInstance(toastRef.value)
  }
})
</script>

<template>
  <RouterView />
  <Toast ref="toastRef" />
</template>

<style>
/* 全局样式已在 main.css 中定义 */
/* 确保主页面背景为纯白色 */
body {
  background-color: #ffffff;
}

#app {
  background-color: #ffffff;
}
</style>
