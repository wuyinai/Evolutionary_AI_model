<template>
  <div class="main-layout">
    <!-- 左侧导航栏 -->
    <Navigation />

    <!-- 主内容区域 -->
    <main class="main-content-area">
      <div class="content-wrapper">
        <!-- 页面切换过渡动画 -->
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import Navigation from './Navigation.vue'
</script>

<style scoped>
.main-layout {
  display: flex;
  width: 100%;
  min-height: 100vh;
  background-color: #ffffff;
}

/* 主内容区域 */
.main-content-area {
  flex: 1;
  margin-left: 240px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  overflow-x: hidden;
}

.content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  width: 100%;
  max-width: 100%;
}

/* 页面切换过渡动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease-out;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content-area {
    margin-left: 0;
  }

  .content-wrapper {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .content-wrapper {
    padding: 12px;
  }
}
</style>