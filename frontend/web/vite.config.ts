import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
  server: {
    // 백엔드 CORS가 http://localhost:5173 하나만 허용한다 (WEB_ORIGIN 기본값).
    // 포트를 고정하고, 이미 사용 중이면 다른 포트로 넘어가지 않고 실패하게 한다.
    port: 5173,
    strictPort: true,
  },
})
