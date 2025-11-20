import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import axios from 'axios' // 1. 務必引入 axios

const app = createApp(App)

// --- 修改重點開始 ---

// 錯誤 1: 5173 是前端網頁介面，8080 才是後端 API
// 錯誤 2: 必須包含 http:// 協定
// 錯誤 3: 若你的 Controller 有設 @RequestMapping("/api/users")，這裡建議設為 /api
axios.defaults.baseURL = 'http://localhost:8080';

// 將 axios 掛載到全域設定 (選用，方便組件內使用)
app.config.globalProperties.$axios = axios;

// --- 修改重點結束 ---

app.use(router)
app.mount('#app')