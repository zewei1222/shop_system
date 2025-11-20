<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'

const categories = ref([])
const loading = ref(true)
const currentUser = ref(null)
const isOverlayClick = ref(false)
// 彈窗控制
const showModal = ref(false)
const isEditMode = ref(false)
const currentCategoryId = ref(null)
const categoryName = ref('') // 表單綁定變數

// 1. 取得分類
const getCategories = async () => {
  loading.value = true
  try {
    const response = await axios.get('/category')
    categories.value = response.data
  } catch (err) {
    console.error(err)
    alert("無法取得分類列表")
  } finally {
    loading.value = false
  }
}

// 開啟新增彈窗
const openCreateModal = () => {
  isEditMode.value = false
  currentCategoryId.value = null
  categoryName.value = ''
  showModal.value = true
}

// 開啟編輯彈窗
const openEditModal = (category) => {
  isEditMode.value = true
  currentCategoryId.value = category.id
  categoryName.value = category.name
  showModal.value = true
}

// 2. [新增] 鍵盤事件監聽 (處理 Esc)
const handleKeydown = (e) => {
  // 如果彈窗是開啟的，且按下了 Escape 鍵
  if (showModal.value && e.key === 'Escape') {
    showModal.value = false
  }
}
// 送出表單 (判斷是新增還是修改)
const handleSubmit = async () => {
  if (!categoryName.value.trim()) {
    alert("名稱不能為空")
    return
  }

  try {
    if (isEditMode.value) {
      // 修改
      await axios.put(`/category/${currentCategoryId.value}`,
          { name: categoryName.value },
          { params: { userId: currentUser.value.id } }
      )
      alert("修改成功")
    } else {
      // 新增
      await axios.post('/category',
          { name: categoryName.value },
          { params: { userId: currentUser.value.id } }
      )
      alert("新增成功")
    }
    showModal.value = false
    getCategories()
  }catch (err) {
    console.error(err)
    // ★ 優化錯誤訊息讀取邏輯
    // 1. 嘗試讀取 err.response.data (可能是字串，也可能是 JSON)
    // 2. 如果是 JSON 物件，嘗試讀取 .error 欄位 (配合 GlobalExceptionHandler)
    let msg = "操作失敗"
    if (err.response && err.response.data) {
      if (typeof err.response.data === 'object') {
        msg = err.response.data.error || err.response.data.message || JSON.stringify(err.response.data)
      } else {
        msg = err.response.data
      }
    }
    alert(msg)
  }
}

// 刪除分類
const deleteCategory = async (id) => {
  if (!confirm("確定要刪除此分類嗎？(若分類下有商品將無法刪除)")) return

  try {
    await axios.delete(`/category/${id}`, {
      params: { userId: currentUser.value.id }
    })
    alert("刪除成功")
    getCategories()
  } catch (err) {
    // 顯示後端回傳的錯誤訊息
    alert(err.response?.data || "刪除失敗")
  }
}

onMounted(() => {
  const stored = localStorage.getItem('currentUser')
  if (stored) {
    currentUser.value = JSON.parse(stored)
    getCategories()
  }
  // 3. [新增] 綁定全域鍵盤事件
  document.addEventListener('keydown', handleKeydown)
})

// 4. [新增] 組件銷毀時移除監聽 (避免記憶體洩漏)
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

const handleOverlayMousedown = (e) => {
  // 如果點擊的目標是遮罩本人 (e.currentTarget)，標記為 true
  if (e.target === e.currentTarget) {
    isOverlayClick.value = true
  }
}

const handleOverlayMouseup = (e) => {
  // 只有當：
  // 1. 滑鼠是在遮罩上按下的 (isOverlayClick 為 true)
  // 2. 滑鼠也是在遮罩上放開的 (e.target === e.currentTarget)
  // 才會執行關閉
  if (isOverlayClick.value && e.target === e.currentTarget) {
    showModal.value = false
  }
  // 重置狀態
  isOverlayClick.value = false
}
</script>

<template>
  <div class="container">
    <h1>🏷️ 分類管理</h1>

    <div class="toolbar">
      <button class="btn btn-primary" @click="openCreateModal">
        + 新增分類
      </button>
    </div>

    <div v-if="loading" class="loading">讀取中...</div>

    <div v-else class="table-container">
      <table>
        <thead>
        <tr>
          <th width="80">ID</th>
          <th>分類名稱</th>
          <th width="180">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="c in categories" :key="c.id">
          <td class="id-col">#{{ c.id }}</td>
          <td class="name-col">{{ c.name }}</td>
          <td>
            <button class="btn btn-edit" @click="openEditModal(c)">編輯</button>
            <button class="btn btn-danger" @click="deleteCategory(c.id)">刪除</button>
          </td>
        </tr>
        </tbody>
      </table>
      <div v-if="categories.length === 0" class="loading">目前沒有分類</div>
    </div>

    <div v-if="showModal" class="modal-overlay"
         @mousedown="handleOverlayMousedown"
         @mouseup="handleOverlayMouseup">
      <div class="modal-content">
        <h2>{{ isEditMode ? '編輯分類' : '新增分類' }}</h2>

        <div class="form-group">
          <label>分類名稱</label>
          <input
              type="text"
              v-model="categoryName"
              placeholder="例如：3C周邊、食品..."
              @keyup.enter="handleSubmit"
          >
        </div>

        <div class="modal-actions">
          <button class="btn" @click="showModal = false">取消 (Esc)</button>

          <button class="btn btn-primary" @click="handleSubmit">
            {{ isEditMode ? '儲存變更' : '確認新增' }} (Enter)
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* 沿用之前的設計風格 */
.container { max-width: 800px; margin: 0 auto; padding-bottom: 50px; }
h1 { text-align: center; margin-bottom: 30px; color: var(--text-main); }

.toolbar { margin-bottom: 20px; display: flex; justify-content: flex-end; }

.table-container {
  background-color: var(--bg-card);
  border-radius: 8px;
  box-shadow: var(--shadow);
  overflow: hidden;
  border: 1px solid var(--border);
}
table { width: 100%; border-collapse: collapse; }
th, td { padding: 16px; text-align: left; border-bottom: 1px solid var(--border); color: var(--text-main); }
th { background-color: var(--th-bg); color: var(--text-sec); font-weight: 600; }

.id-col { color: var(--text-sec); font-size: 0.9rem; }
.name-col { font-weight: bold; font-size: 1rem; }

/* 按鈕 */
.btn { padding: 8px 16px; border-radius: 6px; cursor: pointer; border: none; font-weight: 600; margin-left: 8px; }
.btn-primary { background-color: #3182ce; color: white; }
.btn-primary:hover { background-color: #2b6cb0; }

.btn-edit { background-color: #ecc94b; color: #744210; }
.btn-edit:hover { background-color: #d69e2e; }

.btn-danger { background-color: #e53e3e; color: white; }
.btn-danger:hover { background-color: #c53030; }

/* 彈窗 */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background-color: rgba(0,0,0,0.75);
  display: flex; justify-content: center; align-items: center;
  z-index: 9999;
}
.modal-content {
  background-color: var(--bg-card); color: var(--text-main);
  padding: 30px; border-radius: 12px; width: 400px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.5);
  border: 1px solid var(--border);
}
.modal-content h2 { margin-top: 0; margin-bottom: 20px; color: var(--text-main); }

.form-group { margin-bottom: 20px; }
label { display: block; margin-bottom: 8px; color: var(--text-sec); font-weight: bold; }
input {
  width: 100%; padding: 10px; border-radius: 6px; border: 1px solid var(--border);
  background-color: var(--bg-body); color: var(--text-main); box-sizing: border-box;
}

.modal-actions { display: flex; justify-content: flex-end; gap: 10px; }
.loading { text-align: center; padding: 40px; color: var(--text-sec); }
</style>