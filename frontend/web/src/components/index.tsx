import type { AnchorHTMLAttributes, ButtonHTMLAttributes, InputHTMLAttributes } from 'react'
import './components.css'

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> &
  (
    | { size?: 'l'; variant?: 'solid' | 'lined' }
    | { size: 'm'; variant?: never }
    | { size: 's'; variant?: never; disabled?: never }
  )

export function Button({ size = 'l', variant, className = '', type = 'button', ...rest }: ButtonProps) {
  const look = size === 's' ? 'btn-s' : `btn-${size} btn-${variant ?? 'solid'}`
  return <button type={type} className={`btn ${look} ${className}`.trim()} {...rest} />
}

export function Link({ className = '', ...rest }: AnchorHTMLAttributes<HTMLAnchorElement>) {
  return <a className={`link ${className}`.trim()} {...rest} />
}

type TextFieldProps = InputHTMLAttributes<HTMLInputElement> & {
  label?: string
  message?: string
  status?: 'success' | 'error'
}

export function TextField({ label, message, status, id, className = '', ...rest }: TextFieldProps) {
  return (
    <div className={`field ${status ? `field-${status}` : ''} ${className}`.trim()}>
      {label && (
        <label className="field-label" htmlFor={id}>
          {label}
        </label>
      )}
      <input id={id} className="field-input" aria-invalid={status === 'error' || undefined} {...rest} />
      {status && message && <p className="field-message">{message}</p>}
    </div>
  )
}

export function Checkbox({ className = '', ...rest }: Omit<InputHTMLAttributes<HTMLInputElement>, 'type'>) {
  return <input type="checkbox" className={`checkbox ${className}`.trim()} {...rest} />
}

export function Header() {
  return (
    <header className="header">
      <div className="header-logo" />
      <span className="header-title">P-bit</span>
    </header>
  )
}
