INSERT INTO employees (id, name, email, password_hash, is_active, created_at, updated_at)
VALUES (
  100,
  'Test User',
  'test@example.com',
  '$2a$04$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  true,
  NOW(),
  NOW()
);

INSERT INTO employee_roles (employee_id, role_id) VALUES (100, 2);
