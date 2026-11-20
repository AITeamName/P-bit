// 화면 문구 관리 (문구는 ko.json, en.json, ja.json에 있고 키는 세 파일이 같아야 함)
// 문구 안의 {br}은 줄바꿈 자리 (화면 표시는 Text.tsx)
import en from './en.json'
import ja from './ja.json'
import ko from './ko.json'
import { DEFAULT_LOCALE, emptyKeys, isComplete, LOCALES, pickLocale, splitBreaks, toPlainText } from './locale.ts'
import type { Locale } from './locale.ts'

// 문구 키 목록 (ko.json 기준, 다른 언어 파일에 키가 빠지면 타입 오류, 남는 키는 npm run lint의 check:messages 검사)
export type MessageKey = keyof typeof ko

const catalogs: Record<Locale, Record<MessageKey, string>> = { ko, en, ja }

// 기본 언어에 빈 문구가 있으면 원인을 콘솔에 남기고 그 문구는 키 이름으로 표시
// (앱 전체가 멈추지 않으면서 빈 문구가 눈에 띔, 배포 전에는 npm run lint의 check:messages가 막음)
const emptyDefaultKeys = emptyKeys(catalogs[DEFAULT_LOCALE])
if (emptyDefaultKeys.length > 0) {
  console.error(`${DEFAULT_LOCALE}.json에 비어 있는 문구가 있습니다`, emptyDefaultKeys)
}

// 언어별로 문구가 모두 채워졌는지 (en, ja는 비어 있어서 false)
const ready = Object.fromEntries(LOCALES.map((locale) => [locale, isComplete(catalogs[locale])])) as Record<Locale, boolean>

// 현재 언어: 브라우저 언어가 준비된 언어일 때만 사용, 아니면 ko
// 서버 문구와 코칭 결과가 다국어를 지원하기 전까지 en, ja는 채우지 않음 (채우면 화면은 그 언어, 서버 문구는 한국어로 섞임)
export const locale = pickLocale(navigator.languages, ready)
document.documentElement.lang = locale // 화면 읽기 프로그램과 번역 기능이 쓰는 문서 언어 표시

// 현재 언어의 문구 (비어 있으면 키 이름)
function text(key: MessageKey): string {
  const value = catalogs[locale][key]
  return value === '' ? key : value
}

// 문구를 글자로 반환 ({br}은 줄바꿈 문자로 바뀜, placeholder처럼 글자만 받는 곳에 사용)
export function t(key: MessageKey): string {
  return toPlainText(text(key))
}

// 문구를 {br} 기준 줄 목록으로 반환 (Text 부품이 줄 사이에 <br />를 넣을 때 사용)
export function lines(key: MessageKey): string[] {
  return splitBreaks(text(key))
}
