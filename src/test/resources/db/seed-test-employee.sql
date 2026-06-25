INSERT INTO employees (id, name, email, password_hash, is_active, created_at, updated_at)
VALUES (
  100,
  'Test User',
  'test@example.com',
  '$2a$04$3VsQDAD7ersdrBAHSOXdo.JnNo695.rnQePyOkl0RhFXb.PpZvr3a',
  true,
  NOW(),
  NOW()
) ON CONFLICT (id) DO NOTHING;

INSERT INTO employees (id, name, email, password_hash, is_active, created_at, updated_at)
VALUES (
  101,
  'Inactive User',
  'inactive@example.com',
  '$2a$04$3VsQDAD7ersdrBAHSOXdo.JnNo695.rnQePyOkl0RhFXb.PpZvr3a',
  false,
  NOW(),
  NOW()
) ON CONFLICT (id) DO NOTHING;

INSERT INTO employee_roles (employee_id, role_id) VALUES (100, 2) ON CONFLICT DO NOTHING;
INSERT INTO employee_roles (employee_id, role_id) VALUES (101, 2) ON CONFLICT DO NOTHING;
