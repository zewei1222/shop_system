<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const username = ref('')
const password = ref('')
const errorMsg = ref('')

const handleLogin = async () => {
  // 清除舊錯誤訊息
  errorMsg.value = ''

  if(!username.value || !password.value) {
    errorMsg.value = "請輸入帳號密碼"
    return
  }

  try {
    // 1. 發送登入請求 (注意路徑與後端 Controller 一致)
    const response = await axios.post('http://localhost:8080/api/auth/login', {
      username: username.value,
      password: password.value
    })

    // 2. 取得 Token
    // 後端回傳格式: { "token": "eyJhbGciOiJIUz..." }
    const token = response.data.token;

    // 3. 存入 localStorage (這是最重要的通行證！)
    localStorage.setItem('token', token);

    // 順便存一下使用者名稱，方便之後顯示 "Hello, Admin" (非必要，但體驗較好)
    localStorage.setItem('username', username.value);

    // 4. 轉跳回首頁
    alert('登入成功！')
    router.push('/')

  } catch (err) {
    console.error(err)
    // 錯誤處理
    if (err.response && (err.response.status === 403 || err.response.status === 401)) {
      errorMsg.value = '登入失敗：帳號或密碼錯誤'
    } else if (err.code === 'ERR_NETWORK') {
      errorMsg.value = '連線失敗，後端伺服器沒開？'
    } else {
      errorMsg.value = '發生未知錯誤，請稍後再試'
    }
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <h1>登入商店</h1>

      <div class="form-group">
        <label>帳號</label>
        <input type="text" v-model="username" placeholder="請輸入帳號">
      </div>

      <div class="form-group">
        <label>密碼</label>
        <input type="password" v-model="password" placeholder="請輸入密碼">
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
/* 樣式保持原本的不變，寫得很好！ */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: var(--bg-body);
}

.login-card {
  background: var(--bg-card);
  padding: 40px;
  border-radius: 12px;
  box-shadow: var(--shadow);
  width: 100%;
  max-width: 400px;
  text-align: center;
}

h1 { margin-bottom: 30px; color: var(--text-main); }

.form-group { margin-bottom: 20px; text-align: left; }
label { display: block; margin-bottom: 8px; font-weight: bold; color: var(--text-sec); }
input { width: 100%; padding: 12px; border-radius: 6px; border: 1px solid var(--border); box-sizing: border-box; }

.btn-login {
  width: 100%;
  padding: 12px;
  background-color: #3182ce;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1.1rem;
  cursor: pointer;
  margin-top: 10px;
}
.btn-login:hover { background-color: #2b6cb0; }

.error-text { color: #e53e3e; margin-bottom: 15px; font-size: 0.9rem; }

.links {
  margin-top: 20px;
  font-size: 0.9rem;
}
.links a {
  color: #3182ce;
  text-decoration: none;
}
.links a:hover {
  text-decoration: underline;
}
</style>