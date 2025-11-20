<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios' // 1. 引入 axios

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
    // 2. 改用 axios.post
    // 注意：這裡不需要寫 http://IP:8080，因為 main.js 已經設定了 baseURL
    // 路徑只需寫 /users/login (對應後端 Controller 路徑)
    const response = await axios.post('api/users/login', {
      username: username.value,
      password: password.value
    })

    // axios 成功會直接回傳結果，不需要像 fetch 那樣檢查 response.ok
    // 3. 儲存使用者資訊 (假設後端回傳的物件直接包含 user info)
    localStorage.setItem('currentUser', JSON.stringify(response.data))

    alert('登入成功！')
    router.push('/')

  } catch (err) {
    console.error(err)
    // 4. 處理錯誤回傳
    if (err.response && err.response.status === 400) {
      // 這是我們剛才在後端 GlobalExceptionHandler 設定的錯誤格式
      // 如果是 Map 格式，可能需要轉字串顯示，這裡先簡單顯示
      errorMsg.value = '登入失敗：帳號或密碼錯誤'
    } else {
      errorMsg.value = '連線失敗，請檢查網路或後端伺服器'
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
      <input type="text" v-model="username" placeholder="請輸入帳號"></input>
    </div>

    <div class="form-group">
      <label>密碼</label>
      <input type="password" v-model="password" placeholder="請輸入帳號"></input>
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
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh; /* 佔滿全螢幕高度 */
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