<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios' // 1. 引入 axios

const router = useRouter()
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const errorMsg = ref('')

const handleRegister = async () => {
  // 重置錯誤訊息
  errorMsg.value = ''

  // 前端基本防呆 (可保留，減少無效請求)
  if(!username.value || !password.value) {
    errorMsg.value = "請填寫完整欄位"
    return
  }

  if(password.value !== confirmPassword.value){
    errorMsg.value = "兩次密碼輸入不一致"
    return
  }

  try {
    // 2. 改用 axios.post，並使用相對路徑
    // 這樣會自動指向 main.js 設定的 http://172.20.10.3:8080/api
    await axios.post('/api/users/register', {
      username: username.value,
      password: password.value
    })

    // 3. 成功處理 (Axios 沒有 response.ok，不報錯就是成功)
    alert('註冊成功！請重新登入')
    router.push('/login')

  } catch (err) {
    console.error(err)

    // 4. 處理後端回傳的驗證錯誤 (配合 GlobalExceptionHandler)
    if (err.response && err.response.status === 400) {
      const data = err.response.data;

      // 如果後端回傳的是 Map (欄位錯誤)，例如 { password: "長度不足", username: "不能為空" }
      if (typeof data === 'object') {
        // 將所有錯誤訊息串接起來顯示，或只顯示第一筆
        errorMsg.value = Object.values(data).join(', ');
      } else {
        // 一般錯誤訊息
        errorMsg.value = data || '註冊失敗：資料格式錯誤';
      }
    } else {
      errorMsg.value = '連線錯誤，請檢查網路或伺服器狀態';
    }
  }
}
</script>

<template>
  <div class="register-container">
    <div class="register-card">
      <h1>📝 註冊新帳號</h1>

      <div class="form-group">
        <label>帳號</label>
        <input type="text" v-model="username" placeholder="設定您的帳號">
      </div>

      <div class="form-group">
        <label>密碼</label>
        <input type="password" v-model="password" placeholder="設定您的密碼">
      </div>

      <div class="form-group">
        <label>確認密碼</label>
        <input type="password" v-model="confirmPassword" placeholder="請再次輸入密碼">
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
/* 樣式可以直接複製 LoginView 的，改一下名字就好，這裡微調了背景色 */
.register-container {
  display: flex; justify-content: center; align-items: center;
  height: 100vh; background-color: var(--bg-body);
}
.register-card {
  background: var(--bg-card); padding: 40px; border-radius: 12px;
  box-shadow: var(--shadow); width: 100%; max-width: 400px; text-align: center;
}
h1 { margin-bottom: 30px; color: var(--text-main); }
.form-group { margin-bottom: 20px; text-align: left; }
label { display: block; margin-bottom: 8px; font-weight: bold; color: var(--text-sec); }
input { width: 100%; padding: 12px; border-radius: 6px; border: 1px solid var(--border); box-sizing: border-box; }

.btn-register {
  width: 100%; padding: 12px; background-color: #28a745; /* 綠色 */
  color: white; border: none; border-radius: 6px; font-size: 1.1rem; cursor: pointer; margin-top: 10px;
}
.btn-register:hover { background-color: #218838; }

.error-text { color: #e53e3e; margin-bottom: 15px; font-size: 0.9rem; }
.links { margin-top: 20px; font-size: 0.9rem; }
</style>