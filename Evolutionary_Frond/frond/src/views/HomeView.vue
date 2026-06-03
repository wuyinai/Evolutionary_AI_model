<template>
  <div class="home-page">
    <Sidebar />
    <MainContent />
  </div>
</template>

<script setup lang="ts">
import Sidebar from '@/components/layout/Sidebar.vue'
import MainContent from '@/components/layout/MainContent.vue'
</script>

<style scoped>
.home-page {
  display: flex;
  width: 100%;
  min-height: 100vh;
  background-color: var(--color-background);
}
</style>
