<template>
  <div class="chat-view">
    <!-- 左侧对话历史侧边栏 -->
    <ChatSidebar />

    <!-- 右侧聊天主内容区域 -->
    <ChatMainContent />
  </div>
</template>

<script setup lang="ts">
import ChatSidebar from '@/components/chat/ChatSidebar.vue'
import ChatMainContent from '@/components/chat/ChatMainContent.vue'
</script>

<style scoped>
.chat-view {
  display: flex;
  width: 100%;
  height: 100vh;
  background-color: #ffffff;
}
</style>