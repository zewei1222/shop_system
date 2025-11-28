import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './assets/main.css'
import App from './App.vue'
import router from './router'
import axios from 'axios' // 1. 引入 axios

// --- [架構師設定] 全域 Axios 設定 ---
// 設定後端基礎路徑，以後呼叫 API 只要寫 '/product' 即可
axios.defaults.baseURL = 'http://localhost:8080'

// 設定請求攔截器 (Request Interceptor)
// 就像是過海關前，自動幫你把護照 (Token) 夾在機票 (Request) 裡
axios.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        // 這裡就是你一直想做的：自動加入 Authorization Header
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

// 設定回應攔截器 (Response Interceptor)
// 如果後端回傳 403 (Token 過期或無效)，強制登出
axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response && (error.response.status === 403 || error.response.status === 401)) {
            alert('登入已過期，請重新登入')
            localStorage.removeItem('token')
            window.location.href = '/login' // 強制跳轉
        }
        return Promise.reject(error)
    }
)
// -----------------------------------

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')