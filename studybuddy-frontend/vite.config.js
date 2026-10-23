import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 实验二：Vite 配置
// 1) 开发服务器固定 5173 端口，与后端 CORS 白名单一致
// 2) server.proxy 把 /api 代理到后端 8080，开发阶段同源，绕开浏览器同源策略
//    （与后端 CORS 配置二选一即可；生产环境推荐 Nginx 反向代理 + 后端精确白名单）
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    host: '127.0.0.1',
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    sourcemap: false
  }
})
