<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue' // 記得引入 onUnmounted
import { showSuccess, showError, showConfirm } from '@/utils/swal'
import axios from 'axios'

const users = ref([])
const loading = ref(false)
const errorMsg = ref('')
const currentUser = ref(null)

// 彈窗相關
const showModal = ref(false)
const isEditMode = ref(false)
const currentUserId = ref(null)
const userForm = ref({
  username: '',
  password: '',
  role: 'ROLE_USER'
})

// ★ 新增：點擊遮罩關閉的邏輯變數
const isOverlayClick = ref(false)

const fetchUsers = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await axios.get('/user')
    users.value = res.data
  } catch (err) {
    errorMsg.value = err.response?.status === 403
        ? "權限不足"
        : "讀取失敗：" + (err.response?.data?.message || err.message)
  } finally {
    loading.value = false
  }
}

const openCreateModal = () => {
  isEditMode.value = false
  currentUserId.value = null
  userForm.value = { username: '', password: '', role: 'ROLE_USER' }
  showModal.value = true
}

const openEditModal = (u) => {
  isEditMode.value = true
  currentUserId.value = u.id
  userForm.value = { username: u.username, password: '', role: u.role }
  showModal.value = true
}

// ★ 新增：遮罩點擊判斷
const handleOverlayMousedown = (e) => {
  if (e.target === e.currentTarget) isOverlayClick.value = true
}
const handleOverlayMouseup = (e) => {
  if (isOverlayClick.value && e.target === e.currentTarget) showModal.value = false
  isOverlayClick.value = false
}

// ★ 修改後：同時處理 Esc (關閉) 和 Enter (送出)
const handleKeydown = (e) => {
  // 如果彈窗沒開，就不做事
  if (!showModal.value) return

  if (e.key === 'Escape') {
    // 按 Esc 關閉
    showModal.value = false
  } else if (e.key === 'Enter') {
    // 按 Enter 送出
    // ★ 防呆機制：如果在 "多行文字框 (textarea)" 裡按 Enter，應該是換行，而不是送出
    if (e.target.tagName === 'TEXTAREA') return;

    e.preventDefault() // 防止瀏覽器預設行為
    handleSubmit()     // 呼叫提交函式
  }
}

const handleSubmit = async () => {
  if (!userForm.value.username) {
    showError("帳號為必填")
    return
  }
  if (!isEditMode.value && !userForm.value.password) {
    showError("新增時密碼為必填")
    return
  }

  try {
    if (isEditMode.value) {
      await axios.put(`/user/${currentUserId.value}`, userForm.value)
      await showSuccess("修改成功")
    } else {
      await axios.post('/user', userForm.value)
      await showSuccess("新增成功")
    }
    showModal.value = false
    fetchUsers()
  } catch (err) {
    showError(err.response?.data?.message || err.response?.data || "操作失敗")
  }
}

const handleDelete = async (id) => {
  const isConfirmed = await showConfirm("確定刪除此使用者！")
  if(isConfirmed) {
    try {
      await axios.delete(`/user/${id}`)
      fetchUsers()
    } catch (err) {
      showError(err.response?.data || "刪除失敗")
    }
  }else{
    return
  }
}

const canOperate = (targetUser) => {
  if (!currentUser.value) return false
  if (targetUser.username === currentUser.value.sub) return true
  if (targetUser.role === 'ROLE_ADMIN') return false
  return true
}

const parseJwt = (token) => {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''))
    return JSON.parse(jsonPayload)
  } catch (e) { return null }
}

onMounted(() => {
  const token = localStorage.getItem('token')
  if (token) currentUser.value = parseJwt(token)
  fetchUsers()
  // ★ 註冊鍵盤監聽
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  // ★ 移除鍵盤監聽
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <div class="page-container">
    <div class="header-row">
      <h2>👥 使用者管理</h2>
      <button class="btn btn-primary" @click="openCreateModal">+ 新增使用者</button>
    </div>

    <div v-if="errorMsg" class="error-alert">{{ errorMsg }}</div>
    <div v-else-if="loading" class="loading">資料載入中...</div>

    <div v-else class="list-container">
      <table class="simple-table">
        <thead>
        <tr>
          <th>ID</th>
          <th>帳號</th>
          <th>角色</th>
          <th>建立者</th>
          <th>建立時間</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="u in users" :key="u.id">
          <td>{{ u.id }}</td>
          <td>{{ u.username }}</td>
          <td>
              <span :class="['role-badge', u.role === 'ROLE_ADMIN' ? 'admin' : 'user']">
                {{ u.role === 'ROLE_ADMIN' ? '管理員' : '一般用戶' }}
              </span>
          </td>
          <td class="audit-info">{{ u.createdUser || '-' }}</td>
          <td class="audit-info">{{ u.createdDate ? new Date(u.createdDate).toLocaleString() : '-' }}</td>
          <td>
            <div v-if="canOperate(u)" class="action-group">
              <button class="btn btn-edit btn-sm" @click="openEditModal(u)">編輯</button>
              <button v-if="u.username !== currentUser?.sub" class="btn btn-danger btn-sm" @click="handleDelete(u.id)">刪除</button>
            </div>
            <span v-else class="text-muted">無權限</span>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="modal-overlay"
         @mousedown="handleOverlayMousedown"
         @mouseup="handleOverlayMouseup">

      <div class="modal-content">
        <h3>{{ isEditMode ? '編輯使用者' : '新增使用者' }}</h3>

        <div class="form-group">
          <label>帳號</label>
          <input v-model="userForm.username" type="text" placeholder="輸入帳號">
        </div>

        <div class="form-group">
          <label>密碼 <small v-if="isEditMode">(若不修改請留空)</small></label>
          <input v-model="userForm.password" type="password" placeholder="輸入密碼">
        </div>

        <div class="form-group">
          <label>角色</label>
          <select v-model="userForm.role">
            <option value="ROLE_USER">一般用戶</option>
            <option value="ROLE_ADMIN">管理員</option>
          </select>
        </div>

        <div class="modal-actions">
          <button class="btn btn-cancel" @click="showModal = false">取消 (Esc)</button>
          <button class="btn btn-primary" @click="handleSubmit">確認 (Enter)</button>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* ...保留原本的 CSS... */
</style>

<style scoped>
/* 頁面基本設定 */
.page-container { max-width: 1000px; margin: 0 auto; padding: 20px; }
.header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
h2 { margin: 0; color: var(--text-main); } /* 使用變數 */

/* 錯誤與載入 */
.error-alert { background: #fed7d7; color: #c53030; padding: 15px; border-radius: 6px; text-align: center;}
.loading { text-align: center; color: var(--text-sec); padding: 20px; }

/* 表格樣式 - 深色模式相容 */
.list-container {
  background-color: var(--bg-card); /* 使用變數 */
  border-radius: 8px;
  box-shadow: var(--shadow);
  overflow: hidden;
  border: 1px solid var(--border);
}

.simple-table { width: 100%; border-collapse: collapse; }
th, td { padding: 12px; text-align: left; border-bottom: 1px solid var(--border); color: var(--text-main); }
th { background-color: var(--th-bg); font-weight: bold; color: var(--text-sec); }

/* 角色標籤 */
.role-badge { padding: 4px 8px; border-radius: 12px; font-size: 0.8rem; font-weight: bold; }
.role-badge.admin { background: #c6f6d5; color: #276749; }
.role-badge.user { background: #bee3f8; color: #2c5282; }

/* 審計資訊 */
.audit-info { font-size: 0.85rem; color: var(--text-sec); }
.text-muted { font-size: 0.85rem; color: var(--text-sec); font-style: italic; }

.action-group { display: flex; gap: 5px; }

/* 按鈕樣式 */
.btn { padding: 8px 16px; border: none; border-radius: 6px; cursor: pointer; color: white; font-weight: 500; transition: 0.2s; }
.btn:hover { opacity: 0.9; }
.btn-primary { background: #3b82f6; }
.btn-danger { background: #ef4444; }
.btn-edit { background: #f59e0b; }
.btn-sm { padding: 4px 8px; font-size: 0.8rem; }

/* 彈窗樣式 - 深色模式相容 */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.6);
  display: flex; justify-content: center; align-items: center; z-index: 999;
}

.modal-content {
  background-color: var(--bg-card); /* 變數 */
  color: var(--text-main);          /* 變數 */
  padding: 30px;
  border-radius: 12px;
  width: 400px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.5);
  border: 1px solid var(--border);
}

.modal-content h3 { margin-top: 0; margin-bottom: 20px; text-align: center; color: var(--text-main); }

.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; font-weight: bold; color: var(--text-sec); }

/* 輸入框與選單 - 深色模式相容 */
.form-group input,
.form-group select {
  width: 100%;
  padding: 8px;
  border-radius: 4px;
  border: 1px solid var(--border);
  background-color: var(--bg-body); /* 使用變數 */
  color: var(--text-main);          /* 使用變數 */
  box-sizing: border-box;
  font-size: 1rem;
}

.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
.modal-actions .btn:first-child { background-color: var(--bg-body); color: var(--text-main); border: 1px solid var(--border); } /* 取消按鈕 */
</style>