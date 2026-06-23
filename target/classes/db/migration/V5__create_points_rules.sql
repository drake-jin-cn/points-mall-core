CREATE TABLE points_rules (
    id           BIGSERIAL   PRIMARY KEY,
    rule_code    VARCHAR(50) NOT NULL UNIQUE, -- e.g. 'CHECKIN_DAILY', 'ATTENDANCE_FULL_MONTH'
    description  TEXT        NOT NULL,
    points_value INT         NOT NULL,        -- positive = earn, negative = deduct
    is_active    BOOLEAN     NOT NULL DEFAULT TRUE,
    valid_from   DATE,
    valid_until  DATE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
