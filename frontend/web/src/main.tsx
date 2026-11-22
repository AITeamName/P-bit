// 앱 시작점 (브라우저가 가장 먼저 실행하는 파일)
import { StrictMode } from 'react' // 개발 중 숨은 문제를 찾아 주는 React 도구
import { createRoot } from 'react-dom/client' // 화면을 그릴 뿌리 자리를 만드는 함수
import './index.css' // 전체 기본 스타일 (폰트, 색상 변수)
import App from './App.tsx' // 앱 최상위 화면 부품

// index.html의 <div id="root"> 안에 앱 렌더링
// 끝의 !: root 요소가 반드시 있다는 표시 (없으면 바로 오류)
createRoot(document.getElementById('root')!).render(
  // StrictMode: 개발 모드에서 일부 코드를 두 번 실행해 숨은 문제 확인 (배포본은 영향 없음)
  <StrictMode>
    <App />
  </StrictMode>,
)
