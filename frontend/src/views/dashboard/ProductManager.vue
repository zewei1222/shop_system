<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { showSuccess, showError, showConfirm } from '@/utils/swal'

// --- 資料定義 ---
const router = useRouter()
const currentUser = ref(null)

const products = ref([])
const categoryList = ref([])
const loading = ref(true)
const selectedIds = ref([])
const showModal = ref(false)
const currentProductId = ref(null)

// 分頁與搜尋
const currentPage = ref(0)
const totalPages = ref(0)
const keyword = ref('')

// 排序狀態
const sortBy = ref('id')
const sortDir = ref('desc')

// 表單資料
const productForm = ref({
  name: '', description: '', price: 0, stock: 0, categoryId: null
})

// --- API 動作 ---
const getProducts = async (page = 0) => {
  loading.value = true
  try {
    const response = await axios.get('/product', {
      params: {
        page: page,
        size: 10,
        keyword: keyword.value,
        sortBy: sortBy.value,
        sortDir: sortDir.value
      }
    })
    products.value = response.data.content
    currentPage.value = response.data.number
    totalPages.value = response.data.totalPages
    selectedIds.value = []
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleSort = (field) => {
  if (sortBy.value === field) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortBy.value = field
    sortDir.value = 'asc'
  }
  getProducts(0)
}

const getSortIcon = (field) => {
  if (sortBy.value !== field) return '↕'
  return sortDir.value === 'asc' ? '↑' : '↓'
}

const handleSearch = () => { getProducts(0) }
const changePage = (page) => {
  if (page >= 0 && page < totalPages.value) getProducts(page)
}

// --- CRUD ---
const openCreateModal = () => {
  currentProductId.value = null
  productForm.value = { name: '', description: '', price: 0, stock: 0, categoryId: null }
  showModal.value = true
}
const openEditModal = (p) => {
  currentProductId.value = p.id
  productForm.value = { name: p.name, description: p.description, price: p.price, stock: p.stock, categoryId: p.categoryId }
  showModal.value = true
}
const handleSubmit = () => { currentProductId.value ? updateProduct() : createProduct() }

const handleError = (err, defaultMsg) => {
  console.error(err)
  let msg = defaultMsg
  if (err.response && err.response.data) {
    const data = err.response.data
    if (typeof data === 'object') msg = data.message || data.error || JSON.stringify(data, null, 2)
    else msg = data
  }
  showError(msg)
}

const createProduct = async () => {
  if (!productForm.value.name || !productForm.value.categoryId) { await showError('名稱和分類是必填的！'); return }
  try {
    await axios.post('/product', { ...productForm.value })
    await showSuccess('新增成功！')
    showModal.value = false
    getProducts(0)
  } catch (err) { handleError(err, '新增失敗') }
}

const updateProduct = async () => {
  try {
    await axios.put(`/product/${currentProductId.value}`, productForm.value)
    await showSuccess('編輯成功！')
    showModal.value = false
    getProducts(currentPage.value)
  } catch (err) { handleError(err, '編輯失敗') }
}

const deleteProduct = async (id) => {
  const isConfirmed = await showConfirm("刪除後將無法復原！")
  if (!isConfirmed) return
  try {
    await axios.delete(`/product/${id}`)
    await showSuccess('刪除成功')
    getProducts(currentPage.value)
  } catch (err) { handleError(err, '刪除失敗') }
}

const deleteProductBatch = async () => {
  if (selectedIds.value.length === 0) return
  const isConfirmed = await showConfirm(`確定刪除這 ${selectedIds.value.length} 筆商品？`)
  if (!isConfirmed) return
  try {
    await axios.delete('/product/batch', { data: selectedIds.value })
    await showSuccess('批次刪除成功！')
    getProducts(currentPage.value)
  } catch (err) { handleError(err, '批次刪除失敗') }
}

const getCategories = async () => {
  try {
    const response = await axios.get('/category')
    categoryList.value = response.data
  } catch (err) { console.error("分類讀取失敗", err) }
}

// 互動
const isOverlayClick = ref(false)
const handleOverlayMousedown = (e) => { if (e.target === e.currentTarget) isOverlayClick.value = true }
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
const isNumber = (evt) => {
  const charCode = (evt.which) ? evt.which : evt.keyCode;
  if ((charCode > 31 && (charCode < 48 || charCode > 57)) && charCode !== 46) evt.preventDefault();
}
const isInteger = (evt) => {
  const charCode = (evt.which) ? evt.which : evt.keyCode;
  if (charCode > 31 && (charCode < 48 || charCode > 57)) evt.preventDefault();
}

onMounted(() => {
  const token = localStorage.getItem('token')
  const storedUser = localStorage.getItem('currentUser')
  if (!token) { router.push('/login'); return }
  if (storedUser) currentUser.value = JSON.parse(storedUser)
  getProducts()
  getCategories()
  document.addEventListener('keydown', handleKeydown)
})
onUnmounted(() => { document.removeEventListener('keydown', handleKeydown) })
</script>

<template>
  <div class="container">
    <h1>🛍️ 商品管理系統</h1>

    <div class="toolbar">
      <button class="btn btn-danger" :disabled="selectedIds.length === 0" @click="deleteProductBatch">
        🗑️ 批次刪除 ({{ selectedIds.length }})
      </button>
      <div class="search-box">
        <input type="text" v-model="keyword" placeholder="搜尋商品名稱..." @keyup.enter="handleSearch">
        <button class="btn btn-search" @click="handleSearch">🔍</button>
      </div>
      <button class="btn btn-primary" @click="openCreateModal">+ 新增商品</button>
    </div>

    <div v-if="loading" class="loading">資料讀取中...</div>

    <div v-else class="table-container">
      <table>
        <thead>
        <tr>
          <th class="col-check" style="text-align: center;">✓</th>
          <th class="col-id sortable" @click="handleSort('id')">ID <span class="sort-icon">{{ getSortIcon('id') }}</span></th>
          <th class="col-name sortable" @click="handleSort('name')">商品名稱 <span class="sort-icon">{{ getSortIcon('name') }}</span></th>
          <th class="col-desc">描述</th>
          <th class="col-price sortable" @click="handleSort('price')">價格 <span class="sort-icon">{{ getSortIcon('price') }}</span></th>
          <th class="col-stock sortable" @click="handleSort('stock')">庫存 <span class="sort-icon">{{ getSortIcon('stock') }}</span></th>
          <th class="col-category sortable" @click="handleSort('categoryName')">分類 <span class="sort-icon">{{ getSortIcon('categoryName') }}</span></th>
          <th class="col-owner sortable" @click="handleSort('ownerName')">賣家 <span class="sort-icon">{{ getSortIcon('ownerName') }}</span></th>
          <th class="col-action">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="p in products" :key="p.id" :class="{ 'selected-row': selectedIds.includes(p.id) }">
          <td style="text-align: center;">
            <input type="checkbox" :value="p.id" v-model="selectedIds" class="checkbox">
          </td>
          <td>#{{ p.id }}</td>
          <td><div class="text-truncate">{{ p.name }}</div></td>
          <td><div class="text-clamp-2">{{ p.description }}</div></td>
          <td class="price">${{ p.price }}</td>
          <td>{{ p.stock }}</td>
          <td>{{ p.categoryName }}</td>
          <td><span class="seller-badge">{{ p.ownerName }}</span></td>
          <td>
            <div class="action-buttons">
              <button class="btn btn-edit btn-sm" @click="openEditModal(p)">編輯</button>
              <button class="btn btn-danger btn-sm" @click="deleteProduct(p.id)">刪除</button>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
      <div v-if="products.length === 0" class="empty-state">目前沒有任何商品。</div>
    </div>

    <div class="pagination" v-if="totalPages > 0">
      <button class="btn-page" :disabled="currentPage === 0" @click="changePage(currentPage - 1)">&lt; 上一頁</button>
      <span class="page-info">第 {{ currentPage + 1 }} 頁 / 共 {{ totalPages }} 頁</span>
      <button class="btn-page" :disabled="currentPage === totalPages - 1" @click="changePage(currentPage + 1)">下一頁 &gt;</button>
    </div>

    <div v-if="showModal" class="modal-overlay" @mousedown="handleOverlayMousedown" @mouseup="handleOverlayMouseup">
      <div class="modal-content">
        <h2>{{ currentProductId ? "修改商品" : "新增商品" }}</h2>
        <div class="form-group">
          <label>商品名稱</label>
          <input type="text" v-model="productForm.name" placeholder="商品名稱..." maxlength="100" @keyup.enter="handleSubmit">
        </div>
        <div class="form-group">
          <label>描述</label>
          <textarea v-model="productForm.description" placeholder="商品描述..." maxlength="500"></textarea>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>價格</label>
            <input type="number" v-model="productForm.price" min="0" max="10000000" @keypress="isNumber($event)" @keyup.enter="handleSubmit">
          </div>
          <div class="form-group">
            <label>庫存</label>
            <input type="number" v-model="productForm.stock" min="0" step="1" @keypress="isInteger($event)" @keyup.enter="handleSubmit">
          </div>
        </div>
        <div class="form-group">
          <label>分類</label>
          <select v-model="productForm.categoryId" @keyup.enter="handleSubmit">
            <option :value="null" disabled>請選擇一個分類</option>
            <option v-for="c in categoryList" :key="c.id" :value="c.id">{{c.name}}</option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="btn btn-cancel" @click="showModal = false">取消 (Esc)</button>
          <button class="btn btn-primary" @click="handleSubmit">{{currentProductId?"確定修改":"確認新增"}} (Enter)</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container { max-width: 1200px; margin: 0 auto; padding: 40px 20px; }
h1 { font-size: 1.8rem; font-weight: 700; margin-bottom: 30px; color: var(--text-main); display: flex; align-items: center; gap: 10px; }

/* Toolbar */
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; background: var(--bg-card); padding: 16px; border-radius: var(--radius); box-shadow: var(--shadow-sm); border: 1px solid var(--border); }
.search-box { display: flex; gap: 10px; flex: 1; max-width: 400px; }
.search-box input { width: 100%; padding: 10px 16px; border-radius: 8px; border: 1px solid var(--border); background-color: var(--bg-body); color: var(--text-main); transition: all 0.2s; }
.search-box input:focus { outline: none; border-color: var(--primary); box-shadow: 0 0 0 3px var(--primary-light); }

/* Table */
.table-container { background-color: var(--bg-card); border-radius: var(--radius); box-shadow: var(--shadow); border: 1px solid var(--border); overflow-x: auto; /* 允許橫向捲動以防爆版 */ }
table { width: 100%; border-collapse: collapse; min-width: 900px; /* 確保表格不會過度壓縮 */ }
thead { background-color: var(--th-bg); border-bottom: 1px solid var(--border); }

/* ★★★ 修正點：表格欄位寬度強制設定 (解決跑版問題) ★★★ */
th {
  padding: 16px;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  color: var(--text-sec);
  white-space: nowrap; /* 禁止表頭換行，解決文字掉下來的問題 */
  text-align: center; /* ★ 修改：強制置中 */
}

/* 各欄位寬度設定 */
.col-check { width: 50px; text-align: center; }
.col-id { width: 80px; min-width: 80px; }
.col-name { width: 80px; min-width: 80px; }
.col-desc { min-width: 80px; } /* 描述欄位自適應，但給最小寬度 */
.col-price { width: 80px; min-width: 80px; }
.col-stock { width: 80px; min-width: 80px; }
.col-category { width: 80px; min-width: 80px; }
.col-owner { width: 80px; min-width: 80px; }
.col-action { width: 100px; min-width: 100px; }

td {
  padding: 16px;
  font-size: 0.95rem;
  border-bottom: 1px solid var(--border);
  color: var(--text-main);
  vertical-align: middle;
  text-align: center; /* ★ 修改：強制置中 */
}

/* (選用) 讓名稱和描述保持靠左，其他置中 */
.col-name, .col-desc,
td:nth-child(3), td:nth-child(4) {
  text-align: left;
}

tbody tr { transition: background-color 0.2s; }
tbody tr:hover { background-color: var(--bg-hover); }
.selected-row { background-color: var(--select-bg) !important; }

/* Badges & Helpers */
.seller-badge { background-color: var(--primary-light); color: var(--primary); padding: 4px 10px; border-radius: 20px; font-size: 0.75rem; font-weight: 600; white-space: nowrap;}
.text-truncate { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; width: 100%; display: block; }
.text-clamp-2 { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; text-overflow: ellipsis; line-height: 1.5; max-height: 3em; font-size: 0.9rem; color: var(--text-sec); }

/* Buttons */
.btn { padding: 8px 16px; border: none; border-radius: 6px; font-weight: 500; cursor: pointer; font-size: 0.9rem; transition: all 0.2s; display: inline-flex; align-items: center; justify-content: center; }
.btn:active { transform: scale(0.98); }
.btn-primary { background-color: var(--primary); color: white; box-shadow: 0 2px 4px rgba(79, 70, 229, 0.2); }
.btn-primary:hover { background-color: var(--primary-hover); }
.btn-danger { background-color: var(--bg-body); color: var(--danger); border: 1px solid var(--border); }
.btn-danger:hover { background-color: #fee2e2; border-color: var(--danger); }
.btn-danger:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-edit { background-color: var(--bg-body); color: var(--text-sec); border: 1px solid var(--border); }
.btn-edit:hover { background-color: var(--bg-hover); color: var(--primary); border-color: var(--primary); }
.btn-search { background-color: var(--bg-card); border: 1px solid var(--border); color: var(--text-main); }
.btn-search:hover { background-color: var(--bg-hover); }
.btn-cancel { background: transparent; border: 1px solid var(--border); color: var(--text-main); }
.btn-cancel:hover { background: var(--bg-hover); }
.btn-sm { padding: 6px 12px; font-size: 0.85rem; height: 32px; white-space: nowrap; }

/* 操作按鈕容器 */
.action-buttons { display: flex; gap: 8px; justify-content: flex-start; }

/* Modal */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.6); backdrop-filter: blur(4px); display: flex; justify-content: center; align-items: center; z-index: 9999; animation: fadeIn 0.2s ease-out; }
.modal-content { background-color: var(--bg-card); color: var(--text-main); padding: 32px; border-radius: 16px; width: 450px; box-shadow: var(--shadow-lg); border: 1px solid var(--border); animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1); }
.modal-content h2 { text-align: center; margin-bottom: 24px; font-size: 1.5rem; }
.form-group { margin-bottom: 20px; }
.form-row { display: flex; gap: 15px; }
.form-row .form-group { flex: 1; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 600; font-size: 0.9rem; color: var(--text-sec); }
.form-group input, .form-group textarea, .form-group select { width: 100%; padding: 12px; border-radius: 8px; border: 1px solid var(--border); background-color: var(--bg-body); color: var(--text-main); font-size: 1rem; transition: border-color 0.2s; box-sizing: border-box; }
.form-group input:focus, .form-group textarea:focus, .form-group select:focus { outline: none; border-color: var(--primary); box-shadow: 0 0 0 3px var(--primary-light); }
textarea { height: 100px; resize: vertical; }
.modal-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }

/* Pagination */
.pagination { display: flex; justify-content: center; gap: 10px; margin-top: 30px; }
.page-info { font-size: 0.9rem; color: var(--text-sec); font-weight: 600; align-self: center; }
.btn-page { background: var(--bg-card); border: 1px solid var(--border); color: var(--text-main); padding: 8px 16px; border-radius: 8px; transition: all 0.2s; }
.btn-page:hover:not(:disabled) { border-color: var(--primary); color: var(--primary); }
.btn-page:disabled { opacity: 0.5; cursor: not-allowed; }

.loading, .empty-state { text-align: center; color: var(--text-sec); padding: 40px; }
.checkbox { width: 18px; height: 18px; cursor: pointer; accent-color: var(--primary); }

/* Sortable */
.sortable { cursor: pointer; user-select: none; transition: background-color 0.2s; }
.sortable:hover { background-color: var(--bg-hover); color: var(--primary); }
.sort-icon { margin-left: 4px; font-size: 0.8rem; opacity: 0.7; }

@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
</style>