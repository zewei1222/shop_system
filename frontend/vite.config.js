import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        vue(),
    ],
    resolve: {
        alias: {
            // 這一行告訴 Vite: "@" 就等於 "./src"
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    }
})