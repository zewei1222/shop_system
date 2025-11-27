import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'

// ★ 1. 新增：JWT 解析工具 (跟 HomeView 裡的一樣，或是你可以抽成 utils.js)
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

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        { path: '/login', component: LoginView },
        { path: '/register', component: RegisterView },
        {
            // HomeView 是 Layout (包含側邊欄、Header)
            path: '/',
            component: () => import('../views/HomeView.vue'),
            // 這裡設定子路由，這些組件會渲染在 HomeView 的 <router-view> 裡
            children: [
                {
                    path: '', // 預設首頁 (商品列表)
                    name: 'ProductManager',
                    // 請確認你的檔案路徑是否正確，之前我們寫的 ProductView 應該放在這裡
                    component: () => import('../views/dashboard/ProductManager.vue')
                },
                {
                    path: 'users',
                    name: 'UserManager',
                    component: () => import('../views/dashboard/UserManager.vue')
                },
                {
                    path: 'categories',
                    name: 'CategoryManager',
                    component: () => import('../views/dashboard/CategoryManager.vue')
                }
            ]
        }
    ]
})

// ★ 2. 修正路由守衛：改為檢查 Token
router.beforeEach((to, from, next) => {
    const publicPages = ['/login', '/register'];
    const authRequired = !publicPages.includes(to.path);

    // 從 LocalStorage 拿 Token
    const token = localStorage.getItem('token');
    let userPayload = null;

    if (token) {
        userPayload = parseJwt(token);

        // (進階) 檢查 Token 是否過期
        // exp 是 UNIX 時間戳 (秒)，Date.now() 是毫秒
        if (userPayload && userPayload.exp && Date.now() >= userPayload.exp * 1000) {
            alert("登入已過期，請重新登入");
            localStorage.removeItem('token');
            return next('/login');
        }
    }

    // 1. 需要權限但沒有 Token -> 踢回 Login
    if (authRequired && !token) {
        return next('/login');
    }

    // 2. 已登入但想去 Login/Register -> 踢回首頁 (優化體驗)
    if (!authRequired && token) {
        return next('/');
    }

    // 3. 權限檢查 (Role Check)
    // 假設 Token 解析出來的結構有 sub (username) 和 role (或是 authorities)
    // 根據你的 User Entity，我們之前把 Role 放在 Token 的 payload 裡了嗎？
    // 如果沒有，我們只能先跳過這個檢查，或者依賴後端擋。
    // *假設* Token 裡有 role 欄位 (通常需要在 JwtService 裡特別加進去)
    /* const adminPages = ['/users', '/categories'];
    if (adminPages.includes(to.path) && userPayload.role !== 'ROLE_ADMIN') {
        alert('權限不足！');
        return next('/');
    }
    */

    next();
})

export default router