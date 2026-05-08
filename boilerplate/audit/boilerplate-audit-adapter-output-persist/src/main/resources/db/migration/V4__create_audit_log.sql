CREATE TABLE audit_log (
  id              UUID         NOT NULL PRIMARY KEY,
  subject_user_id UUID         NOT NULL,
  event_type      VARCHAR(50)  NOT NULL,
  payload         JSONB        NOT NULL,
  occurred_at     TIMESTAMPTZ  NOT NULL,
  recorded_at     TIMESTAMPTZ  NOT NULL,
  version         BIGINT       NOT NULL DEFAULT 0,
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_log_subject_recorded    ON audit_log (subject_user_id, recorded_at DESC);
CREATE INDEX idx_audit_log_event_type_recorded ON audit_log (event_type, recorded_at DESC);
