// 개발 서버와 빌드 도구(Vite) 설정
import react from '@vitejs/plugin-react' // React 코드를 브라우저가 이해하는 코드로 변환하는 플러그인
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
  server: {
    // 백엔드 CORS가 http://localhost:5173 하나만 허용 (WEB_ORIGIN 기본값)
    // 포트 고정, 이미 사용 중이면 다른 포트로 넘어가지 않고 실패
    port: 5173,
    strictPort: true,
  },
})
