// 화면 문구 관리 (문구는 ko.json, en.json, ja.json에 있고 키는 세 파일이 같아야 함)
// 문구 안의 {br}은 줄바꿈 자리 (화면 표시는 Text.tsx)
import en from './en.json'
import ja from './ja.json'
import ko from './ko.json'
import { DEFAULT_LOCALE, isComplete, LOCALES, pickLocale, splitBreaks, toPlainText } from './locale.ts'
import type { Locale } from './locale.ts'

// 문구 키 목록 (ko.json 기준, 다른 언어 파일에 키가 빠지거나 남으면 타입 오류)
export type MessageKey = keyof typeof ko

const catalogs: Record<Locale, Record<MessageKey, string>> = { ko, en, ja }

// 기본 언어에 빈 문구가 있으면 대체할 곳이 없으므로 시작 때 바로 오류
if (!isComplete(catalogs[DEFAULT_LOCALE])) {
  throw new Error(`${DEFAULT_LOCALE}.json에 비어 있는 문구가 있습니다`)
}

// 언어별로 문구가 모두 채워졌는지 (en, ja는 비어 있어서 false)
const ready = Object.fromEntries(LOCALES.map((locale) => [locale, isComplete(catalogs[locale])])) as Record<Locale, boolean>

// 현재 언어: 브라우저 언어가 준비된 언어일 때만 사용, 아니면 ko
const locale = pickLocale(navigator.languages, ready)
document.documentElement.lang = locale // 화면 읽기 프로그램과 번역 기능이 쓰는 문서 언어 표시

// 문구를 글자로 반환 ({br}은 줄바꿈 문자로 바뀜, placeholder처럼 글자만 받는 곳에 사용)
export function t(key: MessageKey): string {
  return toPlainText(catalogs[locale][key])
}

// 문구를 {br} 기준 줄 목록으로 반환 (Text 부품이 줄 사이에 <br />를 넣을 때 사용)
export function lines(key: MessageKey): string[] {
  return splitBreaks(catalogs[locale][key])
}
