<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-card card">
        <div class="register-header">
          <h1 class="register-title text-2xl font-bold">注册</h1>
          <p class="register-subtitle text-secondary text-sm mt-sm">创建您的账户，开始使用</p>
        </div>

        <form class="register-form" @submit.prevent="handleRegister">
          <div class="form-group">
            <label class="form-label" for="username">用户名</label>
            <input
              id="username"
              v-model="registerForm.username"
              type="text"
              class="form-input"
              placeholder="请输入用户名"
              required
            />
          </div>

          <div class="form-group">
            <label class="form-label" for="email">邮箱</label>
            <input
              id="email"
              v-model="registerForm.email"
              type="email"
              class="form-input"
              placeholder="请输入邮箱地址"
              required
            />
          </div>

          <div class="form-group">
            <label class="form-label" for="password">密码</label>
            <input
              id="password"
              v-model="registerForm.password"
              type="password"
              class="form-input"
              placeholder="请输入密码"
              required
            />
          </div>

          <div class="form-group">
            <label class="form-label" for="confirmPassword">确认密码</label>
            <input
              id="confirmPassword"
              v-model="registerForm.confirmPassword"
              type="password"
              class="form-input"
              placeholder="请再次输入密码"
              required
            />
          </div>

          <div v-if="errorMessage" class="error-message text-sm" style="color: #ef4444">
            {{ errorMessage }}
          </div>

          <div v-if="successMessage" class="success-message text-sm" style="color: #10b981">
            {{ successMessage }}
          </div>

          <button type="submit" class="btn btn-primary btn-full" :disabled="isLoading">
            {{ isLoading ? '注册中...' : '注册' }}
          </button>
        </form>

        <div class="register-footer text-center mt-lg">
          <p class="text-sm text-secondary">
            已有账户？
            <router-link to="/login" class="link">立即登录</router-link>
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
import type { RegisterForm } from '@/types/user'

const router = useRouter()
const userStore = useUserStore()

const registerForm = ref<RegisterForm>({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const isLoading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const handleRegister = async () => {
  // 表单验证
  if (!registerForm.value.username || !registerForm.value.email || !registerForm.value.password) {
    errorMessage.value = '请填写所有必填项'
    return
  }

  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }

  if (registerForm.value.password.length < 6) {
    errorMessage.value = '密码长度至少为6位'
    return
  }

  // 邮箱格式验证
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(registerForm.value.email)) {
    errorMessage.value = '请输入有效的邮箱地址'
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const response = await userStore.register(registerForm.value)

    if (response.code === 200) {
      successMessage.value = '注册成功！即将跳转到登录页面...'
      setTimeout(() => {
        router.push('/login')
      }, 2000)
    } else {
      errorMessage.value = response.message || '注册失败，请重试'
    }
  } catch (error: any) {
    errorMessage.value = error.response?.data?.message || '注册失败，请检查网络连接'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.register-page {
  width: 100%;
  min-height: 100vh;
  background-color: var(--color-background);
}

.register-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: var(--spacing-xl);
}

.register-card {
  width: 100%;
  max-width: 400px;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--spacing-2xl);
}

.register-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.register-title {
  color: var(--color-text);
  margin-bottom: var(--spacing-xs);
}

.register-subtitle {
  color: var(--color-text-secondary);
}

.register-form {
  width: 100%;
}

.error-message {
  margin-bottom: var(--spacing-md);
  padding: var(--spacing-sm);
  background-color: #fef2f2;
  border-radius: var(--radius-sm);
  text-align: center;
}

.success-message {
  margin-bottom: var(--spacing-md);
  padding: var(--spacing-sm);
  background-color: #f0fdf4;
  border-radius: var(--radius-sm);
  text-align: center;
}

.register-footer {
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--color-border);
}
</style>
