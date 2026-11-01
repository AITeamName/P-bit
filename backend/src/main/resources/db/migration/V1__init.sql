CREATE TABLE users (
  id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email                VARCHAR(255) NOT NULL UNIQUE,
  password_hash        VARCHAR(100),
  nickname             VARCHAR(20)  NOT NULL UNIQUE,
  provider             VARCHAR(10)  NOT NULL DEFAULT 'LOCAL' CHECK (provider IN ('LOCAL','GOOGLE')),
  provider_id          VARCHAR(100),
  domain               VARCHAR(20)  CHECK (domain IN ('MARKETING','STUDY','CODING','CREATIVE','ETC')),
  self_reported_level  VARCHAR(20)  CHECK (self_reported_level IN ('BEGINNER','INTERMEDIATE','ADVANCED')),
  current_level        VARCHAR(5)   CHECK (current_level IN ('LV1','LV2','LV3')),
  level_measured_at    TIMESTAMPTZ,
  onboarding_completed BOOLEAN      NOT NULL DEFAULT false,
  language             VARCHAR(5)   NOT NULL DEFAULT 'ko',
  role                 VARCHAR(10)  NOT NULL DEFAULT 'USER' CHECK (role IN ('USER','ADMIN')),
  created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
  deleted_at           TIMESTAMPTZ
);
CREATE UNIQUE INDEX uq_users_provider ON users (provider, provider_id) WHERE provider_id IS NOT NULL;

CREATE TABLE terms (
  id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code         VARCHAR(20)  NOT NULL CHECK (code IN ('SERVICE','PRIVACY','MARKETING')),
  version      VARCHAR(10)  NOT NULL,
  title        VARCHAR(100) NOT NULL,
  required     BOOLEAN      NOT NULL,
  content      TEXT         NOT NULL,
  effective_at TIMESTAMPTZ  NOT NULL,
  UNIQUE (code, version)
);

CREATE TABLE user_term_agreements (
  id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id   UUID    NOT NULL REFERENCES users(id),
  term_id   BIGINT  NOT NULL REFERENCES terms(id),
  agreed    BOOLEAN NOT NULL,
  agreed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (user_id, term_id)
);

CREATE TABLE refresh_tokens (
  id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id     UUID        NOT NULL REFERENCES users(id),
  token_hash  VARCHAR(64) NOT NULL UNIQUE,
  client_type VARCHAR(10) NOT NULL CHECK (client_type IN ('WEB','EXTENSION')),
  expires_at  TIMESTAMPTZ NOT NULL,
  revoked_at  TIMESTAMPTZ,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_refresh_user ON refresh_tokens (user_id);

CREATE TABLE extension_auth_codes (
  code       VARCHAR(64) PRIMARY KEY,
  user_id    UUID        NOT NULL REFERENCES users(id),
  state      VARCHAR(64) NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL,
  used_at    TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_ext_state ON extension_auth_codes (state);

CREATE TABLE coaching_sessions (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id        UUID        NOT NULL REFERENCES users(id),
  mode           VARCHAR(12) NOT NULL CHECK (mode IN ('DIAGNOSE','REVIEW','RETROSPECT')),
  is_onboarding  BOOLEAN     NOT NULL DEFAULT false,
  channel        VARCHAR(10) NOT NULL CHECK (channel IN ('SIDEBAR','WEB')),
  domain         VARCHAR(20) NOT NULL CHECK (domain IN ('MARKETING','STUDY','CODING','CREATIVE','ETC')),
  task_id        VARCHAR(64),
  status         VARCHAR(12) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS','COMPLETED','ABANDONED')),
  current_round  INT         NOT NULL DEFAULT 0,
  focus_element  VARCHAR(30),
  rubric_version VARCHAR(20),
  language       VARCHAR(5)  NOT NULL DEFAULT 'ko',
  started_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  completed_at   TIMESTAMPTZ
);
CREATE INDEX idx_sessions_user_time ON coaching_sessions (user_id, started_at DESC);

CREATE TABLE prompt_events (
  id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  event_uuid       UUID        NOT NULL UNIQUE DEFAULT gen_random_uuid(),
  session_id       UUID        NOT NULL REFERENCES coaching_sessions(id),
  user_id          UUID        NOT NULL REFERENCES users(id),
  event_type       VARCHAR(15) NOT NULL CHECK (event_type IN ('INITIAL','REWRITE','PRACTICE_FILL','ASSEMBLED')),
  turn             INT         NOT NULL,
  prompt_text      TEXT        NOT NULL,
  prompt_char_len  INT         NOT NULL,
  revision_count   INT         NOT NULL DEFAULT 0,
  element_flags    JSONB,
  span_labels      JSONB,
  ambiguity_hits   JSONB,
  element_set_size SMALLINT,
  fulfilled_count  SMALLINT,
  fulfillment_rate NUMERIC(4,3),
  measured_level   VARCHAR(5),
  level_mutable    BOOLEAN     NOT NULL DEFAULT false,
  axis2_score      SMALLINT,
  degraded         BOOLEAN     NOT NULL DEFAULT false,
  mode             VARCHAR(12) NOT NULL CHECK (mode IN ('DIAGNOSE','REVIEW','RETROSPECT')),
  channel          VARCHAR(10) NOT NULL,
  domain           VARCHAR(20) NOT NULL,
  task_id          VARCHAR(64),
  rubric_version   VARCHAR(20) NOT NULL,
  judge_model      VARCHAR(50),
  language         VARCHAR(5)  NOT NULL DEFAULT 'ko',
  request_id       UUID,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_events_metric ON prompt_events (user_id, created_at DESC)
  WHERE event_type = 'INITIAL' AND mode IN ('DIAGNOSE','REVIEW') AND degraded = false;
CREATE INDEX idx_events_session   ON prompt_events (session_id, turn);
CREATE INDEX idx_events_user_time ON prompt_events (user_id, created_at DESC);

CREATE TABLE before_after_records (
  id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  session_id             UUID   NOT NULL UNIQUE REFERENCES coaching_sessions(id),
  user_id                UUID   NOT NULL REFERENCES users(id),
  before_event_id        BIGINT NOT NULL REFERENCES prompt_events(id),
  after_event_id         BIGINT REFERENCES prompt_events(id),
  before_prompt_text     TEXT   NOT NULL,
  after_prompt_text      TEXT,
  after_authored_by      VARCHAR(20) NOT NULL CHECK (after_authored_by IN ('SYSTEM_ASSEMBLED','USER_REWRITTEN','ASSEMBLY_FAILED')),
  selected_attributes    JSONB,
  drilldown_values       JSONB,
  drilldown_input_type   VARCHAR(10) CHECK (drilldown_input_type IN ('CHOICE','FREE_TEXT')),
  attribute_option_order JSONB,
  answer_before          TEXT,
  answer_after           TEXT,
  user_preferred_side    VARCHAR(6) CHECK (user_preferred_side IN ('BEFORE','AFTER')),
  axis3_reason_text      TEXT,
  reason_summary         TEXT,
  result_snapshot        JSONB,
  excluded_from_analysis BOOLEAN     NOT NULL DEFAULT false,
  mode                   VARCHAR(12) NOT NULL,
  domain                 VARCHAR(20) NOT NULL,
  rubric_version         VARCHAR(20) NOT NULL,
  created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_ba_user_time ON before_after_records (user_id, created_at DESC);

CREATE TABLE level_histories (
  id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id          UUID         NOT NULL REFERENCES users(id),
  session_id       UUID         NOT NULL REFERENCES coaching_sessions(id),
  event_id         BIGINT       NOT NULL REFERENCES prompt_events(id),
  level            VARCHAR(5)   NOT NULL CHECK (level IN ('LV1','LV2','LV3')),
  fulfillment_rate NUMERIC(4,3) NOT NULL,
  is_onboarding    BOOLEAN      NOT NULL DEFAULT false,
  domain           VARCHAR(20)  NOT NULL,
  rubric_version   VARCHAR(20)  NOT NULL,
  measured_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_level_user_time ON level_histories (user_id, measured_at DESC);

CREATE TABLE coached_elements (
  id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id    UUID        NOT NULL REFERENCES users(id),
  session_id UUID        NOT NULL REFERENCES coaching_sessions(id),
  element    VARCHAR(30) NOT NULL,
  source     VARCHAR(10) NOT NULL CHECK (source IN ('ATTRIBUTE','PROBE','PRACTICE')),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_coached_user_el ON coached_elements (user_id, element, created_at);
