<script setup>
import { ref, onMounted, onUnmounted } from 'vue' // 引入生命週期
import { useRouter } from 'vue-router'
import axios from 'axios'
// 引入 SweetAlert 取代原生 alert (保持風格統一)
import { showSuccess, showError } from '@/utils/swal'

const router = useRouter()
const username = ref('')
const password = ref('')
const errorMsg = ref('')

// 鍵盤監聽邏輯
const handleKeydown = (e) => {
  if (e.key === 'Enter') {
    handleLogin()
  }
}

const handleLogin = async () => {
  errorMsg.value = ''

  if(!username.value || !password.value) {
    // 這裡可以用 showError，也可以保留原本的文字提示 (視設計而定)
    // 為了不讓畫面一直跳，這裡我們用文字提示就好
    errorMsg.value = "請輸入帳號密碼"
    return
  }

  try {
    const response = await axios.post('/api/auth/login', {
      username: username.value,
      password: password.value
    })

    const token = response.data.token;
    localStorage.setItem('token', token);
    localStorage.setItem('username', username.value);

    // 改用 SweetAlert 提示成功
    await showSuccess('登入成功！')
    router.push('/')

  } catch (err) {
    console.error(err)
    if (err.response && (err.response.status === 403 || err.response.status === 401)) {
      errorMsg.value = '登入失敗：帳號或密碼錯誤'
    } else if (err.code === 'ERR_NETWORK') {
      errorMsg.value = '連線失敗，後端伺服器沒開？'
    } else {
      errorMsg.value = '發生未知錯誤，請稍後再試'
    }
  }
}

// 掛載監聽器
onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

// 移除監聽器
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <h1>登入商店</h1>

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

      <div v-if="errorMsg" class="error-text">{{ errorMsg }}</div>

      <button class="btn-login" @click="handleLogin">登入</button>

      <div class="links">
        <router-link to="/register">還沒帳號？註冊一個</router-link>
      </div>

    </div>
  </div>
</template>

<style scoped>
/* 使用全域變數，支援深色模式 */
.login-container {
  display: flex; justify-content: center; align-items: center;
  height: 100vh; background-color: var(--bg-body); transition: background-color 0.3s;
}

.login-card {
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
input:focus { outline: none; border-color: var(--primary); box-shadow: 0 0 0 3px var(--primary-light); }

.btn-login {
  width: 100%; padding: 12px; background-color: var(--primary); color: white;
  border: none; border-radius: 8px; font-size: 1.1rem; font-weight: 600;
  cursor: pointer; margin-top: 10px; transition: background-color 0.2s;
}
.btn-login:hover { background-color: var(--primary-hover); }

.error-text {
  color: var(--danger); margin-bottom: 15px; font-size: 0.9rem;
  background: rgba(239, 68, 68, 0.1); padding: 8px; border-radius: 6px;
}

.links { margin-top: 25px; font-size: 0.95rem; }
.links a { color: var(--primary); text-decoration: none; transition: color 0.2s; }
.links a:hover { color: var(--primary-hover); text-decoration: underline; }
</style>