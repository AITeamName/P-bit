// 문구 JSON 검사 (세 파일의 키 목록이 같은지, 기본 언어에 빈 문구가 없는지)
// npm run lint에서 실행되어 CI에서도 잘못된 문구 파일이 머지되지 않게 막음
import { readFileSync } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const DEFAULT_LOCALE = 'ko'
const LOCALES = ['ko', 'en', 'ja']

// 언어별 문구 객체를 받아 문제 목록을 반환 (없으면 빈 목록)
export function findMessageProblems(catalogs) {
  const problems = []
  const baseKeys = Object.keys(catalogs[DEFAULT_LOCALE])
  for (const locale of LOCALES) {
    const keys = Object.keys(catalogs[locale])
    const missing = baseKeys.filter((key) => !keys.includes(key))
    const extra = keys.filter((key) => !baseKeys.includes(key))
    if (missing.length > 0) problems.push(`${locale}.json에 없는 키: ${missing.join(', ')}`)
    if (extra.length > 0) problems.push(`${locale}.json에만 있는 키: ${extra.join(', ')}`)
  }
  const empty = baseKeys.filter((key) => catalogs[DEFAULT_LOCALE][key] === '')
  if (empty.length > 0) problems.push(`${DEFAULT_LOCALE}.json에 비어 있는 문구: ${empty.join(', ')}`)
  return problems
}

function main() {
  const catalogs = Object.fromEntries(
    LOCALES.map((locale) => [locale, JSON.parse(readFileSync(new URL(`../src/i18n/${locale}.json`, import.meta.url), 'utf8'))]),
  )
  const problems = findMessageProblems(catalogs)
  if (problems.length > 0) {
    for (const problem of problems) console.error(problem)
    process.exit(1)
  }
}

// 직접 실행했을 때만 검사 (테스트에서 가져다 쓸 때는 실행하지 않음)
if (path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) main()
