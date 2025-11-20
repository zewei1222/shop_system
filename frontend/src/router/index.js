import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        { path: '/login', component: LoginView },
        { path: '/register', component: RegisterView },
        {
            // HomeView 是「父路由」，負責顯示側邊欄
            path: '/',
            component: () => import('../views/HomeView.vue'),
            // children 是「子路由」，會顯示在 HomeView 的 <router-view> 裡面
            children: [
                {
                    path: '',  // 網址是 / 時，預設顯示商品
                    component: () => import('../views/dashboard/ProductManager.vue')
                },
                {
                    path: 'users', // 網址是 /users 時
                    component: () => import('../views/dashboard/UserManager.vue')
                },
                {
                    path: 'categories', // 網址是 /categories 時
                    component: () => import('../views/dashboard/CategoryManager.vue')
                }
            ]
        }
    ]
})

// 全域路由守衛：防止未登入或權限不足的使用者亂闖
router.beforeEach((to, from, next) => {
    const publicPages = ['/login', '/register'];
    const authRequired = !publicPages.includes(to.path);
    const storedUser = localStorage.getItem('currentUser');
    const user = storedUser ? JSON.parse(storedUser) : null;

    // 1. 沒登入 -> 踢回 Login
    if (authRequired && !user) {
        return next('/login');
    }

    // 2. 非管理員想進管理頁面 -> 踢回首頁
    const adminPages = ['/users', '/categories'];
    // 注意：這裡要判斷 path 是否結尾包含 users 或 categories
    if (authRequired && user && user.role !== 'ROLE_ADMIN') {
        if (to.path.includes('/users') || to.path.includes('/categories')) {
            alert('權限不足！');
            return next('/');
        }
    }

    next();
})

export default router