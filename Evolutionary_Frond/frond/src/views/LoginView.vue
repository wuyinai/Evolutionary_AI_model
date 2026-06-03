<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card card">
        <div class="login-header">
          <h1 class="login-title text-2xl font-bold">登录</h1>
          <p class="login-subtitle text-secondary text-sm mt-sm">欢迎回来，请登录您的账户</p>
        </div>

        <form class="login-form" @submit.prevent="handleLogin">
          <div class="form-group">
            <label class="form-label" for="username">用户名</label>
            <input
              id="username"
              v-model="loginForm.username"
              type="text"
              class="form-input"
              placeholder="请输入用户名"
              required
            />
          </div>

          <div class="form-group">
            <label class="form-label" for="password">密码</label>
            <input
              id="password"
              v-model="loginForm.password"
              type="password"
              class="form-input"
              placeholder="请输入密码"
              required
            />
          </div>

          <div class="form-options flex justify-between items-center">
            <label class="checkbox-label">
              <input v-model="loginForm.rememberMe" type="checkbox" />
              <span class="text-sm">记住我</span>
            </label>
            <a href="#" class="link text-sm">忘记密码？</a>
          </div>

          <div v-if="errorMessage" class="error-message text-sm" style="color: #ef4444">
            {{ errorMessage }}
          </div>

          <button type="submit" class="btn btn-primary btn-full" :disabled="isLoading">
            {{ isLoading ? '登录中...' : '登录' }}
          </button>
        </form>

        <div class="login-footer text-center mt-lg">
          <p class="text-sm text-secondary">
            还没有账户？
            <router-link to="/register" class="link">立即注册</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { LoginForm } from '@/types/user'

const router = useRouter()
const userStore = useUserStore()

const loginForm = ref<LoginForm>({
  username: '',
  password: '',
  rememberMe: false,
})

const isLoading = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    errorMessage.value = '请输入用户名和密码'
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const response = await userStore.login(loginForm.value)

    if (response.code === 200) {
      // 登录成功，跳转到主页
      router.push('/')
    } else {
      errorMessage.value = response.message || '登录失败，请重试'
    }
  } catch (error: any) {
    errorMessage.value = error.response?.data?.message || '登录失败，请检查网络连接'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-page {
  width: 100%;
  min-height: 100vh;
  background-color: var(--color-background);
}

.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: var(--spacing-xl);
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--spacing-2xl);
}

.login-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.login-title {
  color: var(--color-text);
  margin-bottom: var(--spacing-xs);
}

.login-subtitle {
  color: var(--color-text-secondary);
}

.login-form {
  width: 100%;
}

.form-options {
  margin-bottom: var(--spacing-lg);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  cursor: pointer;
}

.checkbox-label input[type='checkbox'] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.error-message {
  margin-bottom: var(--spacing-md);
  padding: var(--spacing-sm);
  background-color: #fef2f2;
  border-radius: var(--radius-sm);
  text-align: center;
}

.login-footer {
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--color-border);
}
</style>
