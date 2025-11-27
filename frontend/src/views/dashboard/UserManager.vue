<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const users = ref([])
const loading = ref(false)
const errorMsg = ref('')

const fetchUsers = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await axios.get('/user')
    users.value = res.data
  } catch (err) {
    // 如果後端回傳 403，顯示權限不足
    if (err.response && err.response.status === 403) {
      errorMsg.value = "您沒有權限瀏覽此頁面 (需要 ADMIN 權限)"
    } else {
      errorMsg.value = "讀取失敗：" + err.message
    }
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id) => {
  if(!confirm("確定刪除此使用者？")) return
  try {
    await axios.delete(`/user/${id}`)
    fetchUsers()
  } catch (err) {
    alert("刪除失敗")
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <div class="page-container">
    <h2>👥 使用者管理</h2>

    <div v-if="errorMsg" class="error-alert">{{ errorMsg }}</div>
    <div v-else-if="loading">載入中...</div>

    <div v-else class="list-container">
      <table class="simple-table">
        <thead>
        <tr>
          <th>ID</th>
          <th>帳號</th>
          <th>角色</th>
          <th>建立時間</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="u in users" :key="u.id">
          <td>{{ u.id }}</td>
          <td>{{ u.username }}</td>
          <td>
              <span :class="['role-badge', u.role === 'ADMIN' ? 'admin' : 'user']">
                {{ u.role }}
              </span>
          </td>
          <td>{{ u.createdDate ? new Date(u.createdDate).toLocaleString() : '-' }}</td>
          <td>
            <button class="btn btn-danger btn-sm" @click="handleDelete(u.id)">刪除</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page-container { max-width: 900px; margin: 0 auto; padding: 20px; }
h2 { margin-bottom: 20px; color: var(--text-main); }

.error-alert { background: #fed7d7; color: #c53030; padding: 15px; border-radius: 6px; }

.simple-table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: var(--shadow); }
th, td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
th { background: #f7fafc; font-weight: bold; }

.role-badge { padding: 4px 8px; border-radius: 12px; font-size: 0.8rem; font-weight: bold; }
.role-badge.admin { background: #c6f6d5; color: #276749; }
.role-badge.user { background: #bee3f8; color: #2c5282; }

.btn { padding: 6px 12px; border: none; border-radius: 4px; cursor: pointer; color: white; }
.btn-danger { background: #e53e3e; }
</style>