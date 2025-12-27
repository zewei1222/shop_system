<script setup>
import { ref, onMounted, onUnmounted } from 'vue' // 記得引入 onUnmounted
import { showSuccess, showError, showConfirm } from '@/utils/swal'
import axios from 'axios'

const categories = ref([])
const loading = ref(false)

// 彈窗相關狀態
const showModal = ref(false)
const isEditMode = ref(false)
const currentCategoryId = ref(null)
const categoryForm = ref({ name: '' })

const isSubmitting = ref(false)

// ★ 新增：點擊遮罩關閉的邏輯變數
const isOverlayClick = ref(false)

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await axios.get('/category')
    categories.value = res.data
  } catch (err) {
    console.error(err)
    showError("無法讀取分類列表")
  } finally {
    loading.value = false
  }
}

const openCreateModal = () => {
  isEditMode.value = false
  currentCategoryId.value = null
  categoryForm.value = { name: '' }
  showModal.value = true
}

const openEditModal = (c) => {
  isEditMode.value = true
  currentCategoryId.value = c.id
  categoryForm.value = { name: c.name }
  showModal.value = true
}

// ★ 新增：遮罩點擊判斷 (防止誤觸)
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

  // 如果使用者正在用輸入法選字 (例如打中文)，不要觸發送出
  if (e.isComposing) return

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
  if (!categoryForm.value.name.trim()) {
    showError("分類名稱不能為空")
    return
  }

  // ★ 關鍵防護：如果正在送出中，直接擋掉，防止連點或重複觸發
  if (isSubmitting.value) return
  isSubmitting.value = true // 上鎖

  try {
    if (isEditMode.value) {
      const res = await axios.put(`/category/${currentCategoryId.value}`, {
        name: categoryForm.value.name
      })
      await showSuccess(res.data.message || "修改成功")
    } else {
      const res = await axios.post('/category', {
        name: categoryForm.value.name
      })
      // 確保讀取 res.data 中的 message
      await showSuccess(res.data.message || "新增成功")
    }
    showModal.value = false
    fetchCategories()
  } catch (err) {
    // 優先讀取後端拋出的 RuntimeException 訊息
    const errorMsg = err.response?.data?.error || "操作失敗";
    showError(errorMsg);
    console.error("錯誤詳情:", err.response?.data);
  } finally{
      isSubmitting.value = false
  }
}

const handleDelete = async (id) => {
  const isConfirmed = await showConfirm("確定刪除此分類？無法刪除還有商品的分類！")
  if(isConfirmed) {
    try {
      await axios.delete(`/category/${id}`)
      fetchCategories()
      await showSuccess("刪除成功")
    } catch (err) {
      showError(err.response?.data?.message || "刪除失敗，還有商品使用此分類")
    }
  }else{
    return;
  }

}

onMounted(() => {
  fetchCategories()
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
      <h2>🏷️ 分類管理</h2>
      <button class="btn btn-primary" @click="openCreateModal">+ 新增分類</button>
    </div>

    <div v-if="loading">載入中...</div>
    <div v-else class="list-container">
      <table class="simple-table">
        <thead>
        <tr>
          <th width="80">ID</th>
          <th>分類名稱</th>
          <th width="180">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="c in categories" :key="c.id">
          <td>{{ c.id }}</td>
          <td>{{ c.name }}</td>
          <td>
            <button class="btn btn-edit btn-sm" @click="openEditModal(c)">編輯</button>
            <button class="btn btn-danger btn-sm" @click="handleDelete(c.id)">刪除</button>
          </td>
        </tr>
        </tbody>
      </table>
      <div v-if="categories.length === 0" class="empty">暫無分類</div>
    </div>

    <div v-if="showModal" class="modal-overlay"
         @mousedown="handleOverlayMousedown"
         @mouseup="handleOverlayMouseup">

      <div class="modal-content">
        <h3>{{ isEditMode ? '編輯分類' : '新增分類' }}</h3>

        <div class="form-group">
          <label>分類名稱</label>
          <input
              v-model="categoryForm.name"
              type="text"
              placeholder="輸入分類名稱..."
              @keyup.enter="handleSubmit"
          >
        </div>

        <div class="modal-actions">
          <button class="btn btn-cancel" @click="showModal = false">取消(Esc)</button>
          <button class="btn btn-primary" @click="handleSubmit">確認(Enter)</button>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.page-container { max-width: 800px; margin: 0 auto; padding: 40px 20px; }

/* 標題區塊 */
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}
h2 {
  margin: 0;
  color: var(--text-main);
  font-size: 1.8rem;
  font-weight: 700;
  display: flex; align-items: center; gap: 10px;
}

/* 表格容器 (卡片化) */
.list-container {
  background-color: var(--bg-card);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  overflow: hidden;
  border: 1px solid var(--border);
}

.simple-table { width: 100%; border-collapse: collapse; }
th, td { padding: 16px; text-align: left; border-bottom: 1px solid var(--border); color: var(--text-main); }
th {
  background-color: var(--th-bg);
  font-weight: 600;
  color: var(--text-sec);
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.empty { text-align: center; padding: 40px; color: var(--text-muted); }

/* 按鈕系統 */
.btn {
  padding: 9px 18px; border: none; border-radius: 8px; font-weight: 500; cursor: pointer;
  transition: all 0.2s; font-size: 0.9rem;
}
.btn:active { transform: scale(0.98); }

.btn-primary {
  background-color: var(--primary); color: white;
  box-shadow: 0 2px 4px rgba(79, 70, 229, 0.2);
}
.btn-primary:hover { background-color: var(--primary-hover); }

.btn-danger { background-color: var(--bg-body); color: var(--danger); border: 1px solid var(--border); }
.btn-danger:hover { background-color: #fee2e2; border-color: var(--danger); }

.btn-edit { background-color: var(--bg-body); color: var(--text-sec); border: 1px solid var(--border); }
.btn-edit:hover { background-color: var(--bg-hover); color: var(--primary); border-color: var(--primary); }

.btn-cancel { background: transparent; border: 1px solid var(--border); color: var(--text-main); }
.btn-cancel:hover { background: var(--bg-hover); }

.btn-sm { padding: 6px 12px; font-size: 0.85rem; margin-right: 6px; }

/* 彈窗系統 (Modern Modal) */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.6);
  backdrop-filter: blur(4px); /* 毛玻璃特效 */
  display: flex; justify-content: center; align-items: center; z-index: 9999;
  animation: fadeIn 0.2s ease-out;
}

.modal-content {
  background-color: var(--bg-card);
  color: var(--text-main);
  padding: 32px;
  border-radius: 16px;
  width: 420px;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border);
  animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.modal-content h3 { margin-top: 0; margin-bottom: 24px; text-align: center; font-size: 1.5rem; }

.form-group { margin-bottom: 24px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 600; color: var(--text-sec); font-size: 0.9rem; }

.form-group input {
  width: 100%; padding: 12px; border-radius: 8px;
  border: 1px solid var(--border);
  background-color: var(--bg-body);
  color: var(--text-main);
  font-size: 1rem;
  box-sizing: border-box;
  transition: border-color 0.2s;
}
.form-group input:focus { outline: none; border-color: var(--primary); box-shadow: 0 0 0 3px var(--primary-light); }

.modal-actions { display: flex; justify-content: flex-end; gap: 12px; }

/* 動畫 */
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
</style>