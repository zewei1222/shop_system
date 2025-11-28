<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { showSuccess, showError } from '@/utils/swal'

const router = useRouter()
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const errorMsg = ref('')

const handleKeydown = (e) => {
  if (e.key === 'Enter') {
    handleRegister()
  }
}

const handleRegister = async () => {
  errorMsg.value = ''

  if(!username.value || !password.value || !confirmPassword.value) {
    errorMsg.value = "所有欄位皆為必填"
    return
  }

  if (password.value !== confirmPassword.value) {
    errorMsg.value = "兩次密碼輸入不一致"
    return
  }

  try {
    // 呼叫後端註冊 API
    await axios.post('/api/auth/register', {
      username: username.value,
      password: password.value
    })

    await showSuccess('註冊成功！請登入')
    router.push('/login')

  } catch (err) {
    console.error(err)
    if (err.response && err.response.data) {
      // 嘗試顯示後端的錯誤訊息 (例如：帳號已存在)
      const data = err.response.data
      errorMsg.value = typeof data === 'object'
          ? (data.message || JSON.stringify(data))
          : data
    } else {
      errorMsg.value = '註冊失敗，請稍後再試'
    }
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <div class="register-container">
    <div class="register-card">
      <h1>註冊新帳號</h1>

      <div class="form-group">
        <label>帳號</label>
        <input
            type="text"
            v-model="username"
            placeholder="請輸入帳號"
            autofocus
        >
      </div>

      <div class="form-group">
        <label>密碼</label>
        <input
            type="password"
            v-model="password"
            placeholder="請輸入密碼"
        >
      </div>

      <div class="form-group">
        <label>確認密碼</label>
        <input
            type="password"
            v-model="confirmPassword"
            placeholder="再次輸入密碼"
        >
      </div>

      <div v-if="errorMsg" class="error-text">{{ errorMsg }}</div>

      <button class="btn-register" @click="handleRegister">確認註冊</button>

      <div class="links">
        <router-link to="/login">已有帳號？點此登入</router-link>
      </div>

    </div>
  </div>
</template>

<style scoped>
/* 與 LoginView 共用相同的現代化 CSS */
.register-container {
  display: flex; justify-content: center; align-items: center;
  height: 100vh; background-color: var(--bg-body); transition: background-color 0.3s;
}

.register-card {
  background-color: var(--bg-card); padding: 40px; border-radius: 16px;
  box-shadow: var(--shadow-lg); width: 100%; max-width: 400px; text-align: center;
  border: 1px solid var(--border);
}

h1 { margin-bottom: 30px; color: var(--text-main); font-weight: 700; }

.form-group { margin-bottom: 20px; text-align: left; }
label { display: block; margin-bottom: 8px; font-weight: 600; color: var(--text-sec); font-size: 0.9rem; }

input {
  width: 100%; padding: 12px; border-radius: 8px; border: 1px solid var(--border);
  background-color: var(--bg-body); color: var(--text-main); font-size: 1rem;
  box-sizing: border-box; transition: all 0.2s;
}
input:focus { outline: none; border-color: var(--success); box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2); } /* 註冊頁面用綠色系強調 */

.btn-register {
  width: 100%; padding: 12px; background-color: var(--success); color: white; /* 綠色按鈕 */
  border: none; border-radius: 8px; font-size: 1.1rem; font-weight: 600;
  cursor: pointer; margin-top: 10px; transition: background-color 0.2s;
}
.btn-register:hover { opacity: 0.9; }

.error-text {
  color: var(--danger); margin-bottom: 15px; font-size: 0.9rem;
  background: rgba(239, 68, 68, 0.1); padding: 8px; border-radius: 6px;
}

.links { margin-top: 25px; font-size: 0.95rem; }
.links a { color: var(--primary); text-decoration: none; transition: color 0.2s; }
.links a:hover { color: var(--primary-hover); text-decoration: underline; }
</style>