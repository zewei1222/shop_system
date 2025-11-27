<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const categories = ref([])
const newCategoryName = ref('')
const loading = ref(false)

// 1. 取得分類
const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await axios.get('/category')
    categories.value = res.data
  } catch (err) {
    console.error(err)
    alert("無法讀取分類列表")
  } finally {
    loading.value = false
  }
}

// 2. 新增分類
const handleAdd = async () => {
  if(!newCategoryName.value.trim()) return
  try {
    await axios.post('/category', { name: newCategoryName.value })
    newCategoryName.value = ''
    fetchCategories()
    alert("新增分類成功")
  } catch (err) {
    alert("新增失敗：" + (err.response?.data?.message || err.message))
  }
}

// 3. 刪除分類
const handleDelete = async (id) => {
  if(!confirm("確定刪除此分類？與此分類關聯的商品可能會受影響！")) return
  try {
    await axios.delete(`/category/${id}`)
    fetchCategories()
  } catch (err) {
    alert("刪除失敗：" + (err.response?.data?.message || err.message))
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<template>
  <div class="page-container">
    <h2>🏷️ 分類管理</h2>

    <div class="add-section">
      <input
          v-model="newCategoryName"
          placeholder="輸入新分類名稱..."
          @keyup.enter="handleAdd"
      >
      <button class="btn btn-primary" @click="handleAdd">新增</button>
    </div>

    <div v-if="loading">載入中...</div>
    <div v-else class="list-container">
      <table class="simple-table">
        <thead>
        <tr>
          <th>ID</th>
          <th>分類名稱</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="c in categories" :key="c.id">
          <td>{{ c.id }}</td>
          <td>{{ c.name }}</td>
          <td>
            <button class="btn btn-danger btn-sm" @click="handleDelete(c.id)">刪除</button>
          </td>
        </tr>
        </tbody>
      </table>
      <div v-if="categories.length === 0" class="empty">暫無分類</div>
    </div>
  </div>
</template>

<style scoped>
.page-container { max-width: 800px; margin: 0 auto; padding: 20px; }
h2 { margin-bottom: 20px; color: var(--text-main); }

.add-section { display: flex; gap: 10px; margin-bottom: 30px; }
.add-section input { flex: 1; padding: 10px; border: 1px solid var(--border); border-radius: 6px; }

.simple-table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: var(--shadow); }
th, td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
th { background: #f7fafc; font-weight: bold; }
.empty { text-align: center; padding: 20px; color: #888; }

.btn { padding: 8px 16px; border: none; border-radius: 6px; cursor: pointer; color: white; }
.btn-primary { background: #3182ce; }
.btn-danger { background: #e53e3e; }
.btn-sm { padding: 4px 8px; font-size: 0.8rem; }
</style>