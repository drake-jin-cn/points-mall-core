CREATE TABLE points_ledger (
    id            BIGSERIAL   PRIMARY KEY,
    employee_id   BIGINT      NOT NULL REFERENCES employees(id)    ON DELETE CASCADE,
    rule_id       BIGINT      REFERENCES points_rules(id) ON DELETE SET NULL,
    delta         INT         NOT NULL,       -- positive = earn, negative = spend
    balance_after INT         NOT NULL,
    description   TEXT,
    ref_type      VARCHAR(50),               -- 'attendance' | 'order' | 'manual' | 'system'
    ref_id        BIGINT,                    -- FK to the triggering entity (nullable)
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ledger_employee_id ON points_ledger(employee_id);
CREATE INDEX idx_ledger_created_at  ON points_ledger(created_at);
