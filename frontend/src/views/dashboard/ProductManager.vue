<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

// --- 資料定義 ---
const router = useRouter()
const currentUser = ref(null)

const products = ref([])
const categoryList = ref([])
const loading = ref(true)
const errorMsg = ref('')
const selectedIds = ref([])
const showModal = ref(false)
const currentProductId = ref(null)

// 分頁與搜尋
const currentPage = ref(0)
const totalPages = ref(0)
const keyword = ref('')

// 表單資料
const productForm = ref({
  name: '',
  description: '',
  price: 0,
  stock: 0,
  categoryName: ''
})

// --- 彈窗控制 ---
const openCreateModal = () => {
  currentProductId.value = null
  productForm.value = { name: '', description: '', price: 0, stock: 0, categoryName: '' }
  showModal.value = true
}

const openEditModal = (p) => {
  currentProductId.value = p.id
  productForm.value = {
    name: p.name,
    description: p.description,
    price: p.price,
    stock: p.stock,
    categoryName: p.categoryName
  }
  showModal.value = true
}

// 彈窗遮罩點擊判斷 (防止誤觸)
const isOverlayClick = ref(false)
const handleOverlayMousedown = (e) => { if (e.target === e.currentTarget) isOverlayClick.value = true }
const handleOverlayMouseup = (e) => {
  if (isOverlayClick.value && e.target === e.currentTarget) showModal.value = false
  isOverlayClick.value = false
}

// 鍵盤事件 (Esc 關閉)
const handleKeydown = (e) => {
  if (showModal.value && e.key === 'Escape') {
    showModal.value = false
  }
}

const handleSubmit = () => {
  if (currentProductId.value) {
    updateProduct()
  } else {
    createProduct()
  }
}

// --- API 動作 ---

// 1. 抓取商品列表 (支援分頁與搜尋)
const getProducts = async (page = 0) => {
  loading.value = true
  errorMsg.value = ''

  if (!currentUser.value || !currentUser.value.id) {
    loading.value = false
    return
  }

  try {
    const response = await axios.get('/product', {
      params: {
        userId: currentUser.value.id,
        page: page,
        size: 10, // 固定每頁 10 筆
        keyword: keyword.value
      }
    })

    // 更新資料與分頁資訊
    products.value = response.data.content
    currentPage.value = response.data.number
    totalPages.value = response.data.totalPages

    selectedIds.value = [] // 換頁或重新搜尋時清空勾選
  } catch (err) {
    console.error(err)
    errorMsg.value = '無法取得商品列表'
  } finally {
    loading.value = false
  }
}

// 搜尋功能
const handleSearch = () => {
  getProducts(0) // 搜尋從第 0 頁開始
}

// 換頁功能
const changePage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    getProducts(page)
  }
}

// 2. 新增商品
const createProduct = async () => {
  if (!productForm.value.name || !productForm.value.categoryName) {
    alert('名稱和分類是必填的！')
    return
  }

  try {
    const payload = { ...productForm.value, ownerId: currentUser.value.id }
    await axios.post('/product', payload)
    alert('新增成功！')
    showModal.value = false
    getProducts(0) // 回到第一頁
  } catch (err) {
    alert(err.response?.data || '新增失敗')
  }
}

// 3. 編輯商品
const updateProduct = async () => {
  try {
    await axios.put(`/product/${currentProductId.value}`, productForm.value, {
      params: { userId: currentUser.value.id }
    })
    alert('編輯成功！')
    showModal.value = false
    getProducts(currentPage.value) // 停留在當前頁
  } catch (err) {
    alert(err.response?.data || "編輯失敗")
  }
}

// 4. 單筆刪除
const deleteProduct = async (id) => {
  if (!confirm("確定要刪除這個商品嗎？")) return

  try {
    await axios.delete(`/product/${id}`, {
      params: { userId: currentUser.value.id }
    })
    alert('刪除成功！')
    getProducts(currentPage.value)
  } catch (err) {
    alert(err.response?.data || '刪除失敗')
  }
}

// 5. 批次刪除
const deleteProductBatch = async () => {
  if (selectedIds.value.length === 0) return
  if (!confirm(`確定刪除這 ${selectedIds.value.length} 筆商品？`)) return

  try {
    await axios.delete('/product/batch', {
      data: selectedIds.value,
      params: { userId: currentUser.value.id }
    })
    alert('批次刪除成功！')
    getProducts(currentPage.value)
  } catch (err) {
    alert(err.response?.data || '刪除失敗')
  }
}

// 6. 抓取分類列表
const getCategories = async () => {
  try {
    const response = await axios.get('/category')
    categoryList.value = response.data
  } catch (err) {
    console.error("分類讀取失敗", err)
  }
}

// 初始化
onMounted(() => {
  const storedUser = localStorage.getItem('currentUser')
  if (!storedUser) {
    router.push('/login')
    return
  }
  currentUser.value = JSON.parse(storedUser)
  getProducts()
  getCategories()

  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <div class="container">
    <h1>🛍️ 商品管理系統</h1>

    <div class="toolbar">
      <button
          class="btn btn-danger"
          :disabled="selectedIds.length === 0"
          @click="deleteProductBatch"
      >
        🗑️ 批次刪除 ({{ selectedIds.length }})
      </button>

      <div class="search-box">
        <input
            type="text"
            v-model="keyword"
            placeholder="搜尋商品名稱..."
            @keyup.enter="handleSearch"
        >
        <button class="btn btn-search" @click="handleSearch">🔍</button>
      </div>

      <button class="btn btn-primary" @click="openCreateModal">
        + 新增商品
      </button>
    </div>

    <div v-if="errorMsg" class="error">{{ errorMsg }}</div>
    <div v-if="loading" class="loading">資料讀取中...</div>

    <div v-else class="table-container">
      <table>
        <thead>
        <tr>
          <th width="40" style="text-align: center;">✓</th>
          <th width="50">ID</th>
          <th>商品名稱</th>
          <th>描述</th>
          <th>價格</th>
          <th>庫存</th>
          <th>分類</th>
          <th>賣家</th>
          <th width="160">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="p in products" :key="p.id" :class="{ 'selected-row': selectedIds.includes(p.id) }">
          <td style="text-align: center;">
            <input type="checkbox" :value="p.id" v-model="selectedIds" class="checkbox">
          </td>

          <td class="id-col">#{{ p.id }}</td>

          <td class="name-col" :title="p.name">
            <div class="text-truncate">{{ p.name }}</div>
          </td>

          <td class="desc-col" :title="p.description">
            <div class="text-clamp-2">{{ p.description }}</div>
          </td>

          <td class="price">${{ p.price }}</td>
          <td>{{ p.stock }}</td>
          <td>{{ p.categoryName }}</td>
          <td><span class="seller-badge">{{ p.ownerName }}</span></td>
          <td>
            <button class="btn btn-edit" @click="openEditModal(p)">編輯</button>
            <button class="btn btn-danger btn-sm" @click="deleteProduct(p.id)">刪除</button>
          </td>
        </tr>
        </tbody>
      </table>

      <div v-if="products.length === 0" class="empty-state">
        目前沒有任何商品。
      </div>
    </div>

    <div class="pagination" v-if="totalPages > 0">
      <button
          class="btn-page"
          :disabled="currentPage === 0"
          @click="changePage(currentPage - 1)">
        &lt; 上一頁
      </button>

      <span class="page-info">
        第 {{ currentPage + 1 }} 頁 / 共 {{ totalPages }} 頁
      </span>

      <button
          class="btn-page"
          :disabled="currentPage === totalPages - 1"
          @click="changePage(currentPage + 1)">
        下一頁 &gt;
      </button>
    </div>

    <div v-if="showModal" class="modal-overlay"
         @mousedown="handleOverlayMousedown"
         @mouseup="handleOverlayMouseup">

      <div class="modal-content">
        <h2>{{ currentProductId ? "修改商品" : "新增商品" }}</h2>

        <div class="form-group">
          <label>商品名稱</label>
          <input type="text" v-model="productForm.name" placeholder="商品名稱..." @keyup.enter="handleSubmit">
        </div>

        <div class="form-group">
          <label>描述</label>
          <textarea v-model="productForm.description" placeholder="商品描述..."></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>價格</label>
            <input type="number" v-model="productForm.price" @keyup.enter="handleSubmit">
          </div>
          <div class="form-group">
            <label>庫存</label>
            <input type="number" v-model="productForm.stock" @keyup.enter="handleSubmit">
          </div>
        </div>

        <div class="form-group">
          <label>分類名稱</label>
          <select v-model="productForm.categoryName" @keyup.enter="handleSubmit">
            <option disabled value="">請選擇一個分類</option>
            <option v-for="c in categoryList" :key="c.id" :value="c.name">
              {{c.name}}
            </option>
          </select>
        </div>

        <div class="modal-actions">
          <button class="btn" @click="showModal = false">取消 (Esc)</button>
          <button class="btn btn-primary" @click="handleSubmit">
            {{currentProductId?"確定修改":"確認新增"}} (Enter)
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.container {
  max-width: 1000px;
  margin: 0 auto;
  padding-bottom: 50px;
}

h1 {
  text-align: center;
  font-weight: 700;
  margin-bottom: 30px;
  font-size: 2rem;
  color: var(--text-main);
}

/* --- 工具列 --- */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

/* 搜尋框 */
.search-box {
  display: flex;
  gap: 8px;
}
.search-box input {
  width: 200px;
  padding: 8px 12px;
  background-color: var(--bg-body);
  color: var(--text-main);
  border: 1px solid var(--border);
  border-radius: 6px;
}

/* --- 按鈕樣式 --- */
.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
  color: var(--text-main);
  background-color: var(--bg-card);
  border: 1px solid var(--border);
}

.btn-primary {
  background-color: #3182ce;
  color: white;
  border: none;
}
.btn-primary:hover { background-color: #2b6cb0; }

.btn-danger {
  background-color: #e53e3e;
  color: white;
  border: none;
}
.btn-danger:hover { background-color: #c53030; }
.btn-danger:disabled {
  background-color: #cbd5e0;
  color: #718096;
  cursor: not-allowed;
}

.btn-edit {
  background-color: #ecc94b;
  color: #744210;
  border: none;
  margin-right: 8px;
}
.btn-edit:hover { background-color: #d69e2e; }

.btn-search {
  background-color: var(--bg-card);
  border: 1px solid var(--border);
  padding: 8px 12px;
}
.btn-search:hover { background-color: var(--hover-bg); }

.btn-sm { padding: 8px 12px; }

/* --- 表格樣式 --- */
.table-container {
  box-shadow: var(--shadow);
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--border);
  background-color: var(--bg-card);
}

table { width: 100%; border-collapse: collapse; }
thead { background-color: var(--th-bg); }

th {
  text-align: left;
  padding: 16px;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  color: var(--text-sec);
  border-bottom: 1px solid var(--border);
}

td {
  padding: 16px;
  font-size: 0.95rem;
  border-bottom: 1px solid var(--border);
  vertical-align: middle;
  color: var(--text-main);
}

tr:hover { background-color: var(--hover-bg); }
.selected-row { background-color: var(--select-bg) !important; }

.name-col { font-weight: 500; }
.desc-col { color: var(--text-sec); font-size: 0.9rem; }
.price { color: #48bb78; font-weight: 600; font-family: 'Courier New', monospace; }
.id-col { color: var(--text-sec); font-size: 0.85rem; }

.seller-badge {
  background-color: var(--badge-bg);
  color: var(--badge-text);
  padding: 4px 8px;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.checkbox { width: 18px; height: 18px; cursor: pointer; accent-color: #3182ce; }

/* --- 分頁控制器 --- */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  gap: 15px;
}

.page-info {
  font-size: 0.9rem;
  color: var(--text-sec);
  font-weight: bold;
}

.btn-page {
  padding: 8px 16px;
  background-color: var(--bg-card);
  border: 1px solid var(--border);
  color: var(--text-main);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-page:hover:not(:disabled) {
  background-color: var(--hover-bg);
  border-color: #3182ce;
  color: #3182ce;
}

.btn-page:disabled { opacity: 0.5; cursor: not-allowed; }

/* --- 狀態顯示 --- */
.error {
  color: #fc8181; background-color: #2d2d2d; padding: 16px;
  border-radius: 6px; border: 1px solid #e53e3e;
  text-align: center; margin-bottom: 20px;
}
.loading, .empty-state { text-align: center; color: var(--text-sec); padding: 40px; }

/* --- 彈窗樣式 --- */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background-color: rgba(0, 0, 0, 0.75);
  display: flex; justify-content: center; align-items: center;
  z-index: 9999;
}

.modal-content {
  background-color: var(--bg-card);
  color: var(--text-main);
  padding: 30px;
  border-radius: 12px;
  width: 400px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.5);
  border: 1px solid var(--border);
}

.modal-content h2 { margin-top: 0; margin-bottom: 20px; color: var(--text-main); }

.form-group { margin-bottom: 15px; }
.form-row { display: flex; gap: 15px; }
.form-row .form-group { flex: 1; }

label {
  display: block; margin-bottom: 5px; font-weight: bold; color: var(--text-sec); font-size: 0.9rem;
}

input, textarea, select {
  width: 100%; padding: 10px;
  border: 1px solid var(--border); border-radius: 6px;
  background-color: var(--bg-body); color: var(--text-main);
  font-size: 1rem; box-sizing: border-box;
}

textarea { height: 80px; resize: vertical; }

.modal-actions {
  display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;
}

/* --- 表格欄位寬度控制 (關鍵) --- */
/* 1. 給予欄位最大寬度，超出才能截斷 */
th:nth-child(3), td:nth-child(3) { max-width: 150px; } /* 商品名稱欄 */
th:nth-child(4), td:nth-child(4) { max-width: 250px; } /* 描述欄 (給寬一點) */

/* --- 樣式 A: 單行省略 (適用於名稱) --- */
.text-truncate {
  white-space: nowrap;       /* 強制不換行 */
  overflow: hidden;          /* 超出隱藏 */
  text-overflow: ellipsis;   /* 顯示 ... */
  width: 100%;
  display: block;
}

/* --- 樣式 B: 多行省略 (適用於描述) --- */
.text-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;     /* 限制顯示 2 行 */
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.5;          /* 行高 */
  max-height: 3em;           /* 行高 1.5 * 2 行 = 3em，確保高度固定 */
  font-size: 0.9rem;
  color: var(--text-sec);    /* 描述文字顏色淡一點，增加層次感 */
}

/* --- 其他欄位微調 --- */
.name-col {
  font-weight: 600;
  color: var(--text-main);
}
</style>