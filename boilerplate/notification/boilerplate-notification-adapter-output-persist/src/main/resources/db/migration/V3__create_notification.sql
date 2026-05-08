CREATE TABLE notifications
(
  id             UUID        NOT NULL PRIMARY KEY,
  recipient_id   UUID        NOT NULL,
  channel        VARCHAR(20) NOT NULL,
  status         VARCHAR(20) NOT NULL,
  subject        VARCHAR(200) NOT NULL,
  body           TEXT        NOT NULL,
  failure_reason TEXT,
  sent_at        TIMESTAMPTZ,
  version        BIGINT      NOT NULL DEFAULT 0,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notifications_recipient ON notifications (recipient_id, created_at DESC);
