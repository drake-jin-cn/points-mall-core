-- points-mall-core/src/main/resources/db/migration/V7__add_password_hash_not_null.sql
--
-- V2 created password_hash as nullable (OAuth-only users have no password).
-- Now that all employees are seeded with passwords, enforce NOT NULL.
--
-- ⚠️  T013 Amendment Required: When GitHub OAuth (T013) creates OAuth-only employees,
--     this constraint MUST be re-evaluated before that task begins. Options:
--       (a) Revert to nullable   (b) Store a sentinel hash for OAuth users
ALTER TABLE employees
  ALTER COLUMN password_hash SET NOT NULL;
