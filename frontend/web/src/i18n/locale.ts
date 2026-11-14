// 언어 선택과 {br} 줄바꿈 처리 (JSON을 읽지 않는 순수 함수, 단위 테스트 대상)

// 지원 언어 코드: 한국어 ko, 영어 en, 일본어 ja (브라우저 언어 코드(navigator.language)와 같은 표기)
export const LOCALES = ['ko', 'en', 'ja'] as const
export type Locale = (typeof LOCALES)[number]

// 번역이 없거나 지원하지 않는 언어일 때 쓰는 언어
export const DEFAULT_LOCALE: Locale = 'ko'

// 문구에서 줄바꿈 자리를 나타내는 표시
const BREAK = '{br}'

// 문구가 모두 채워진 언어인지 (비어 있는 문구가 하나라도 있으면 번역 준비 안 됨)
export function isComplete(messages: Record<string, string>): boolean {
  return Object.values(messages).every((text) => text !== '')
}

// 브라우저 언어 목록(우선순위 순)에서 쓸 언어 선택
// 준비된 언어만 선택, 하나도 없으면 DEFAULT_LOCALE (예상 밖 언어에서도 화면이 깨지지 않게 하는 안전장치)
export function pickLocale(languages: readonly string[], ready: Record<Locale, boolean>): Locale {
  for (const language of languages) {
    const base = language.toLowerCase().split('-')[0] // ko-KR -> ko
    const found = LOCALES.find((locale) => locale === base)
    if (found && ready[found]) return found
  }
  return DEFAULT_LOCALE
}

// {br}을 줄바꿈 문자로 바꾼 글자 (placeholder처럼 글자만 받는 곳용)
export function toPlainText(text: string): string {
  return splitBreaks(text).join('\n')
}

// {br} 기준으로 문구 나누기 ("안녕하세요{br}반갑습니다" -> ["안녕하세요", "반갑습니다"])
export function splitBreaks(text: string): string[] {
  return text.split(BREAK)
}
