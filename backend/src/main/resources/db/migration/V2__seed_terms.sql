-- 약관 초기 데이터 (KAN-33)
-- 문구는 임시. 확정되면 새 Flyway 버전으로 새 version 행을 추가한다.
-- content는 마크다운.
INSERT INTO terms (code, version, title, required, content, effective_at) VALUES
  ('SERVICE',   '1.0', '서비스 이용약관',       true,  E'## 서비스 이용약관\n\n[임시] 확정 전 문구입니다.',       '2026-11-01 00:00:00+00'),
  ('PRIVACY',   '1.0', '개인정보 수집 및 이용', true,  E'## 개인정보 수집 및 이용\n\n[임시] 확정 전 문구입니다.', '2026-11-01 00:00:00+00'),
  ('MARKETING', '1.0', '마케팅 정보 수신',      false, E'## 마케팅 정보 수신\n\n[임시] 확정 전 문구입니다.',      '2026-11-01 00:00:00+00');
