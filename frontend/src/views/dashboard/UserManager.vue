<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const users = ref([])
const loading = ref(true)
const currentUser = ref(null)

// 1. 取得使用者列表
const getUsers = async () => {
  loading.value = true
  try {
    // 注意：請確認你的後端 UserController 是 @RequestMapping("/api/users") 還是 "/users"
    // 這裡預設使用 /api/users
    const response = await axios.get('/api/users', {
      params: { currentUserId: currentUser.value.id }
    })
    users.value = response.data
  } catch (err) {
    console.error(err)
    alert("無法取得使用者列表")
  } finally {
    loading.value = false
  }
}

// src/views/dashboard/UserManager.vue

const deleteUser = async (id) => {
  if (!confirm("確定要刪除這位使用者嗎？此動作無法復原！")) return

  try {
    await axios.delete(`/api/users/${id}`, {
      params: { userId: currentUser.value.id }
    })
    alert("刪除成功")
    getUsers()
  } catch (err) {
    console.error(err)
    // 智慧錯誤顯示邏輯
    let msg = "刪除失敗"
    if (err.response && err.response.data) {
      // 如果後端回傳的是物件 (JSON)，嘗試抓 error 或 message 欄位
      if (typeof err.response.data === 'object') {
        msg = err.response.data.error || err.response.data.message || JSON.stringify(err.response.data)
      } else {
        // 如果後端直接回傳字串 (我們剛在 Controller 改的就是這個)
        msg = err.response.data
      }
    }
    alert(msg)
  }
}

// 修改權限
const updateRole = async (userId, newRole) => {
  try {
    await axios.put(`/api/users/${userId}/role`, null, {
      params: {
        currentUserId: currentUser.value.id,
        newRole: newRole
      }
    })
    alert("權限更新成功")
    // 不需要重新整理列表，因為 v-model 已經更新了畫面
  } catch (err) {
    alert("更新失敗")
    getUsers() // 失敗的話要把畫面還原，所以重抓一次
  }
}

onMounted(() => {
  const stored = localStorage.getItem('currentUser')
  if (stored) {
    currentUser.value = JSON.parse(stored)
    getUsers()
  }
})
</script>

<template>
  <div class="container">
    <h1>👥 使用者管理</h1>

    <div v-if="loading" class="loading">讀取中...</div>

    <div v-else class="table-container">
      <table>
        <thead>
        <tr>
          <th width="80">ID</th>
          <th>使用者名稱</th>
          <th>角色權限</th>
          <th width="120">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="u in users" :key="u.id">
          <td class="id-col">#{{ u.id }}</td>
          <td class="name-col">
            {{ u.username }}
            <span v-if="u.id === currentUser.id" class="me-tag">(我)</span>
          </td>
          <td>
            <span v-if="u.id === currentUser.id" class="role-badge badge-admin">
              管理員
            </span>
            <select
                v-else
                v-model="u.role"
                @change="updateRole(u.id, $event.target.value)"
                class="role-select"
                :class="u.role === 'ROLE_ADMIN' ? 'text-admin' : 'text-user'"
            >
              <option value="ROLE_USER">一般會員</option>
              <option value="ROLE_ADMIN">管理員</option>
            </select>
          </td>
          <td>
            <button
                v-if="u.id !== currentUser.id"
                class="btn btn-danger"
                @click="deleteUser(u.id)"
            >
              刪除
            </button>
            <span v-else class="disabled-text">--</span>
          </td>
        </tr>
        </tbody>
      </table>

      <div v-if="users.length === 0" class="loading">目前沒有使用者</div>
    </div>
  </div>
</template>

<style scoped>
/* --- 容器設定 --- */
.container { max-width: 900px; margin: 0 auto; padding-bottom: 50px; }
h1 { text-align: center; margin-bottom: 30px; color: var(--text-main); font-weight: 700; letter-spacing: 1px; }

/* --- 表格容器 --- */
.table-container {
  background-color: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  overflow: hidden; /* 確保圓角不被內容切掉 */
}

table {
  width: 100%;
  border-collapse: collapse;
  /* 移除 fixed，改用自動寬度，讓瀏覽器自己算 */
}

/* 表頭與內容共用設定 */
th, td {
  padding: 16px 20px; /* 統一間距 */
  vertical-align: middle;
  border-bottom: 1px solid var(--border);
  color: var(--text-main);
}

tr:last-child td { border-bottom: none; }
tr:hover { background-color: rgba(255, 255, 255, 0.02); }

/* --- 表頭專屬設定 --- */
thead { background-color: transparent; }

th {
  font-size: 0.85rem;
  font-weight: 800;
  text-transform: uppercase;
  color: #64748b;
  border-bottom: 2px solid var(--border); /* 加粗分隔線 */
  letter-spacing: 0.05em;
}

/* ★★★ 欄位寬度與對齊修正 (這是修復跑版的關鍵) ★★★ */

/* 第 1 欄 (ID): 置中，給固定小寬度 */
th:nth-child(1), td:nth-child(1) {
  width: 60px;
  text-align: center;
  padding-left: 10px; /* 修正：減少左邊距 */
}

/* 第 2 欄 (名稱): 靠左，不設寬度讓它自動吃滿剩餘空間 */
th:nth-child(2), td:nth-child(2) {
  text-align: left;
  width: auto;
}

/* 第 3 欄 (角色): 置中，固定寬度 */
th:nth-child(3), td:nth-child(3) {
  width: 140px;
  text-align: center;
}

/* 第 4 欄 (操作): 置中，固定寬度 */
th:nth-child(4), td:nth-child(4) {
  width: 100px;
  text-align: center;
  padding-right: 20px;
}

/* --- 內容元件樣式 --- */
.id-col { color: #64748b; font-family: 'Courier New', monospace; font-weight: bold; }
.name-col { font-weight: 600; font-size: 1rem; }
.me-tag {
  font-size: 0.75rem; color: #94a3b8; border: 1px solid #475569;
  padding: 2px 6px; border-radius: 4px; margin-left: 8px;
  display: inline-block;
}

/* --- 角色標籤與下拉選單 --- */
.role-badge, .role-select {
  display: inline-block;
  width: 100px; /* 統一寬度 */
  padding: 6px 0;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  text-align: center;
  text-align-last: center; /* 強制下拉選單文字置中 */
  border: 1px solid transparent;
}

.role-select {
  appearance: none; -webkit-appearance: none;
  background-color: transparent; cursor: pointer; transition: all 0.2s;
}

/* Admin 樣式 (紅) */
.badge-admin, .text-admin {
  color: #ff7b72;
  background-color: rgba(255, 123, 114, 0.1);
  border-color: rgba(255, 123, 114, 0.2);
}

/* User 樣式 (藍) */
.badge-user, .text-user {
  color: #79c0ff;
  background-color: rgba(56, 139, 253, 0.1);
  border-color: rgba(56, 139, 253, 0.2);
}

.role-select:hover { box-shadow: 0 0 0 1px currentColor; }
.role-select:focus { outline: none; box-shadow: 0 0 0 2px currentColor; }
option { background-color: #1e293b; color: #fff; }

/* --- 操作按鈕 --- */
.btn {
  padding: 6px 14px; border-radius: 6px; cursor: pointer; font-weight: 600; font-size: 0.85rem;
  transition: all 0.2s; background: transparent;
}

.btn-danger {
  color: #ff7b72; border: 1px solid rgba(255, 123, 114, 0.4);
}

.btn-danger:hover {
  background-color: #d03838; border-color: #d03838; color: white;
  box-shadow: 0 0 10px rgba(248, 81, 73, 0.4);
}

.disabled-text { color: #475569; font-weight: bold; opacity: 0.5; font-size: 1.2rem; }
.loading { text-align: center; padding: 40px; color: #64748b; }
</style>