CREATE TABLE attendance_records (
    id           BIGSERIAL   PRIMARY KEY,
    employee_id  BIGINT      NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    check_in_at  TIMESTAMPTZ NOT NULL,
    check_out_at TIMESTAMPTZ,
    work_date    DATE        NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'normal', -- normal | late | early_leave | absent
    notes        TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_attendance_employee_date ON attendance_records(employee_id, work_date);
