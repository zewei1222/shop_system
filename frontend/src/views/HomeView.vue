<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
// 在 script setup 最上方引入
import { showConfirm } from '@/utils/swal'

const router = useRouter()
const route = useRoute()
const currentUser = ref(null)
const showUserMenu = ref(false)
const menuRef = ref(null)
const isSidebarOpen = ref(true)

// ★ 工具函式：解析 JWT Token (不依賴後端，直接看 Token 內容)
const parseJwt = (token) => {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    }).join(''))
    return JSON.parse(jsonPayload)
  } catch (e) {
    return null
  }
}

const toggleSidebar = () => {
  isSidebarOpen.value = !isSidebarOpen.value
}

// 判斷是否為管理員 (根據 Token 解析出的內容，或是簡單判斷 username)
const isAdmin = computed(() => {
  // 假設 Token 裡沒有 role 欄位，我們先暫時用 username 判斷，之後 Phase 4 再教你標準做法
  // 這裡先假設 admin 帳號就是管理員
  return currentUser.value && currentUser.value.sub === 'admin'
})

const userInitial = computed(() => {
  // JWT 的標準欄位是 'sub' (Subject) 代表帳號
  return currentUser.value?.sub?.charAt(0).toUpperCase() || 'U'
})

const handleLogout = async() => {
  const isConfirmed = await showConfirm("確定要登出嗎?")
  if(isConfirmed){
    // ★ 關鍵修正：清除 token
    localStorage.removeItem("token")
    localStorage.removeItem("username")
    router.push('/login')
  }
}

const toggleMenu = () => { showUserMenu.value = !showUserMenu.value }

const handleClickOutside = (event) => {
  if (showUserMenu.value && menuRef.value && !menuRef.value.contains(event.target)) {
    showUserMenu.value = false
  }
}

onMounted(() => {
  // 1. 檢查是否有 Token
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }

  // 2. 解析 Token 取得使用者資訊
  const payload = parseJwt(token)
  if (payload) {
    currentUser.value = payload
    // payload 裡通常包含: { sub: "username", exp: 123456... }
  } else {
    // Token 格式錯誤，清除並登出
    handleLogout()
  }

  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="layout">
    <aside class="sidebar" :class="{ 'closed': !isSidebarOpen }">

      <div class="sidebar-header">
        <button class="toggle-btn" @click="toggleSidebar" title="切換選單">
          <svg viewBox="0 0 24 24" width="24" height="24" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="12" x2="21" y2="12"></line><line x1="3" y1="6" x2="21" y2="6"></line><line x1="3" y1="18" x2="21" y2="18"></line></svg>
        </button>

        <span class="logo-text" v-show="isSidebarOpen">
          商店後台
        </span>
      </div>

      <nav class="menu">
        <router-link to="/" class="menu-item" :class="{ active: route.path === '/' }">
          <span class="icon">📦</span>
          <span class="text" v-show="isSidebarOpen">商品管理</span>
        </router-link>

        <template v-if="isAdmin">
          <router-link to="/users" class="menu-item" :class="{ active: route.path === '/users' }">
            <span class="icon">👥</span>
            <span class="text" v-show="isSidebarOpen">使用者管理</span>
          </router-link>

          <router-link to="/categories" class="menu-item" :class="{ active: route.path === '/categories' }">
            <span class="icon">🏷️</span>
            <span class="text" v-show="isSidebarOpen">分類管理</span>
          </router-link>
        </template>
      </nav>
    </aside>

    <div class="main-wrapper">
      <header class="top-header">
        <div class="breadcrumbs">
          現在位置：{{ route.path === '/' ? '商品管理' : route.path.replace('/', '') }}
        </div>
        <div class="user-profile-container" ref="menuRef">
          <button class="avatar-btn" @click="toggleMenu">{{userInitial}}</button>
          <transition name="fade">
            <div v-if="showUserMenu" class="dropdown-menu">
              <div class="menu-header">
                <div class="large-avatar">{{ userInitial }}</div>
                <div class="user-details">
                  <div class="name">{{ currentUser?.username }}</div>
                  <div class="role">{{ currentUser?.role }}</div>
                </div>
              </div>
              <hr class="divider">
              <ul class="menu-list">
                <li @click="alert('dev')">帳號設定</li>
                <li @click="handleLogout" class="text-danger">登出帳號</li>
              </ul>
            </div>
          </transition>
        </div>
      </header>

      <main class="main-content">
        <router-view></router-view>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* --- Layout 佈局 --- */
.layout {
  display: flex;
  min-height: 100vh;
  overflow-x: hidden; /* 防止動畫過程出現橫向卷軸 */
  /* 背景色由全域 style.css body 決定 */
}

/* --- Sidebar 側邊欄 (關鍵動畫區) --- */
.sidebar {
  width: 260px; /* 展開時的預設寬度 */
  background-color: #2d3748;
  color: white;
  display: flex;
  flex-direction: column;
  padding: 20px 12px; /* 調整內距 */
  box-shadow: 4px 0 10px rgba(0,0,0,0.1);
  z-index: 10;
  flex-shrink: 0; /* 禁止被壓縮 */

  /* ★ 寬度變化的過渡動畫：使用貝茲曲線讓體驗更滑順 */
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  overflow: hidden;    /* 隱藏縮小後超出的內容 */
  white-space: nowrap; /* 防止文字換行 */
}

/* 當側邊欄關閉時的狀態 */
.sidebar.closed {
  width: 72px; /* 縮小後的寬度 (剛好容納按鈕) */
}

/* --- Sidebar Header (漢堡按鈕 + Logo) --- */
.sidebar-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  height: 40px; /* 固定高度 */
  padding-left: 4px; /* 微調讓按鈕跟下方的 Icon 對齊 */
}

/* 漢堡選單按鈕 */
.toggle-btn {
  background: transparent;
  border: none;
  color: #cbd5e0;
  cursor: pointer;
  padding: 0;
  border-radius: 50%; /* 圓形 */
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  /* 固定大小，確保縮放時不變形 */
  min-width: 40px;
  height: 40px;
}

.toggle-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
}

/* Logo 文字 */
.logo-text {
  font-size: 1.2rem;
  font-weight: bold;
  color: #fff;
  margin-left: 12px;
  opacity: 1;
  transition: opacity 0.2s;
}

/* 當側邊欄關閉時，隱藏 Logo 文字 (避免破版) */
.sidebar.closed .logo-text {
  opacity: 0;
  pointer-events: none;
}

/* --- Menu Items (選單項目) --- */
.menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 0 4px; /* 上下 padding 由 height 撐開 */
  height: 48px;   /* 增加高度更好點擊 */
  color: #cbd5e0;
  text-decoration: none;
  border-radius: 12px; /* 圓角 */
  transition: all 0.2s;
  overflow: hidden;
}

.menu-item:hover {
  background-color: #4a5568;
  color: white;
}

.menu-item.active {
  background-color: #3182ce; /* 啟用狀態藍色 */
  color: white;
  font-weight: bold;
}

/* Icon 設定 (關鍵：固定寬度以在此區塊置中) */
.menu-item .icon {
  min-width: 40px; /* 跟上方的漢堡按鈕同寬，確保視覺對齊 */
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.2rem;
}

/* 文字設定 */
.menu-item .text {
  margin-left: 12px;
  font-size: 0.95rem;
  opacity: 1;
  transition: opacity 0.2s;
}

/* --- Main Wrapper (右側區塊) --- */
.main-wrapper {
  flex: 1; /* 自動填滿剩餘空間 */
  display: flex;
  flex-direction: column;
  position: relative;
  min-width: 0; /* 修正 Flexbox 在某些瀏覽器的 bug */
}

/* --- Top Header (頂部導覽列) --- */
.top-header {
  height: 64px;
  background-color: var(--bg-card);
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between; /* 麵包屑靠左，頭像靠右 */
  align-items: center;
  padding: 0 30px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.breadcrumbs {
  font-size: 0.95rem;
  color: var(--text-sec);
  font-weight: 500;
}

/* --- 右上角使用者頭像區塊 --- */
.user-profile-container {
  position: relative;
}

.avatar-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #3182ce;
  color: white;
  font-weight: bold;
  font-size: 1.1rem;
  border: 2px solid transparent;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.2s;
}

.avatar-btn:hover {
  box-shadow: 0 0 0 4px rgba(49, 130, 206, 0.2);
}

/* --- 下拉選單 (Dropdown) --- */
.dropdown-menu {
  position: absolute;
  top: 55px;
  right: 0;
  width: 280px;
  background-color: var(--bg-card);
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.2);
  border: 1px solid var(--border);
  z-index: 100;
  padding: 8px;
  overflow: hidden;
}

.menu-header {
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.large-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #3182ce;
  color: white;
  font-size: 1.8rem;
  display: flex;
  justify-content: center;
  align-items: center;
}

.user-details { text-align: center; }
.name { font-weight: bold; font-size: 1.1rem; color: var(--text-main); }
.role { font-size: 0.8rem; color: var(--text-sec); background: #edf2f7; padding: 2px 8px; border-radius: 10px; margin-top: 4px; display: inline-block; }

.divider {
  border: 0;
  border-top: 1px solid var(--border);
  margin: 4px 10px;
}

.menu-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.menu-list li {
  padding: 12px 16px;
  cursor: pointer;
  font-size: 0.95rem;
  border-radius: 8px;
  color: var(--text-main);
  transition: background 0.2s;
  margin-bottom: 2px;
}

.menu-list li:hover {
  background-color: var(--hover-bg);
}

.text-danger { color: #e53e3e !important; }

/* --- 主要內容區容器 --- */
.main-content {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

/* Vue Transition 動畫 (Fade) */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>