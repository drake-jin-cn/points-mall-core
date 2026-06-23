CREATE TABLE employees (
    id            BIGSERIAL    PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),                   -- nullable: OAuth-only users have no password
    department_id BIGINT REFERENCES departments(id) ON DELETE SET NULL,
    github_id     VARCHAR(100) UNIQUE,
    avatar_url    TEXT,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_employees_email         ON employees(email);
CREATE INDEX idx_employees_department_id ON employees(department_id);
