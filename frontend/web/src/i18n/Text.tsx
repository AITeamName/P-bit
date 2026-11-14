// 문구를 화면에 표시하는 부품 ({br} 자리에 실제 줄바꿈 <br />)
import { Fragment } from 'react'
import { lines } from './messages.ts'
import type { MessageKey } from './messages.ts'

export default function Text({ id }: { id: MessageKey }) {
  return (
    <>
      {lines(id).map((line, index) => (
        // 줄 순서가 바뀌지 않는 고정 문구라서 index를 key로 사용
        <Fragment key={index}>
          {index > 0 && <br />}
          {line}
        </Fragment>
      ))}
    </>
  )
}
