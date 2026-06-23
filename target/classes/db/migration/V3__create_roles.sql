CREATE TABLE roles (
    id          BIGSERIAL   PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE employee_roles (
    employee_id BIGINT      NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    role_id     BIGINT      NOT NULL REFERENCES roles(id)     ON DELETE CASCADE,
    granted_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (employee_id, role_id)
);

INSERT INTO roles (name, description) VALUES
    ('admin',    'Full system administrator'),
    ('employee', 'Regular employee');
