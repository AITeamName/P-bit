// 여러 화면이 함께 쓰는 공통 부품 (버튼, 링크, 입력창, 체크박스, 머리말)
// 모양은 components.css, 이 파일은 받은 값에 따라 붙일 CSS 이름만 결정
import { useId } from 'react' // 겹치지 않는 고유 id를 만드는 React 도구
import type { AnchorHTMLAttributes, ButtonHTMLAttributes, InputHTMLAttributes } from 'react'
import './components.css'

// 버튼이 받는 값 (기본 버튼 속성 + 크기와 모양)
// 크기별로 고를 수 있는 모양이 다름
//  l (큰 버튼): solid (꽉 찬) 또는 lined (테두리만)
//  m (중간), s (작은 버튼): 모양 선택 불가 (variant를 넘기면 오류)
type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> &
  (
    | { size?: 'l'; variant?: 'solid' | 'lined' }
    | { size: 'm'; variant?: never }
    | { size: 's'; variant?: never }
  )

// 아래 기본값은 모두 부품의 기본 모양과 동작을 정함
//  size = 'l': 크기를 안 정하면 큰 버튼
//  className = '': 추가 CSS 이름이 없을 때 "undefined" 글자가 섞이지 않게 방지
//  type = 'button': 폼 안에서 눌러도 제출되지 않는 안전한 기본값 (제출 버튼은 type="submit"을 직접 지정)
export function Button({ size = 'l', variant, className = '', type = 'button', ...rest }: ButtonProps) {
  // 작은 버튼은 모양이 하나뿐, 큰/중간 버튼은 variant가 없으면 꽉 찬(solid) 모양 (기본 모양)
  const look = size === 's' ? 'btn-s' : `btn-${size} btn-${variant ?? 'solid'}`
  // ...rest: 위에서 꺼내지 않은 나머지 속성(onClick, disabled 등)을 버튼에 그대로 전달
  return <button type={type} className={`btn ${look} ${className}`.trim()} {...rest} />
}

// 글자 링크 부품 (모양만 입히고 나머지 속성은 그대로 전달)
export function Link({ className = '', ...rest }: AnchorHTMLAttributes<HTMLAnchorElement>) {
  return <a className={`link ${className}`.trim()} {...rest} />
}

// 입력창이 받는 값 (기본 입력창 속성 + 레이블, 안내 문구, 상태)
type TextFieldProps = InputHTMLAttributes<HTMLInputElement> & {
  label?: string // 입력창 위의 이름
  message?: string // 입력창 아래의 안내 문구
  status?: 'success' | 'error' // 성공(초록), 오류(빨강), 없으면 평소 모양
}

// 레이블 + 입력창 + 안내 문구 묶음 부품
export function TextField({ label, message, status, id, className = '', ...rest }: TextFieldProps) {
  const autoId = useId() // 자동 생성한 고유 id
  // id를 안 줘도 레이블과 입력창이 연결되도록 자동 id 사용 (필요한 기본값)
  const inputId = id ?? autoId
  const messageId = `${inputId}-message` // 안내 문구에 붙일 id
  return (
    // 상태가 있으면 field-success 또는 field-error 이름이 붙어 색이 바뀜
    <div className={`field ${status ? `field-${status}` : ''} ${className}`.trim()}>
      {/* 레이블이 있을 때만 표시, htmlFor가 입력창 id와 같아서 레이블을 눌러도 입력창에 커서 이동 */}
      {label && (
        <label className="field-label" htmlFor={inputId}>
          {label}
        </label>
      )}
      <input
        id={inputId}
        className="field-input"
        // 오류 상태일 때만 "잘못된 입력값"임을 스크린 리더(화면 읽어 주는 프로그램)에 전달
        aria-invalid={status === 'error' || undefined}
        // 안내 문구가 있으면 입력창과 연결 (스크린 리더가 입력창과 함께 읽음)
        aria-describedby={message ? messageId : undefined}
        {...rest}
      />
      {/* 안내 문구가 있으면 상태와 무관하게 표시, 색만 상태에 따라 달라짐 */}
      {message && (
        <p id={messageId} className="field-message">
          {message}
        </p>
      )}
    </div>
  )
}

// 체크박스 부품 (모양은 CSS, type은 항상 checkbox라서 받지 않음)
export function Checkbox({ className = '', ...rest }: Omit<InputHTMLAttributes<HTMLInputElement>, 'type'>) {
  return <input type="checkbox" className={`checkbox ${className}`.trim()} {...rest} />
}

// 화면 맨 위 머리말 (로고 자리와 서비스 이름)
export function Header() {
  return (
    <header className="header">
      <div className="header-logo" />
      <span className="header-title">P-bit</span>
    </header>
  )
}
