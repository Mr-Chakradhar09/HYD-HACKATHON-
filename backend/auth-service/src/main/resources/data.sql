-- Initial credentials seeding
-- Passwords are BCrypt hashes
INSERT INTO credentials (employee_id, email, password, role, location, enabled, account_non_locked, created_at, updated_at)
VALUES 
('EMP001', 'globalhr@virtusa.com', '$2a$10$ne9kQu9x4OWzk0nNw9DKGe8fsw7jF9XYebj8IWVnR4vgZwsywH4Qy', 'GLOBAL_HR', 'Global', true, true, NOW(), NOW())
ON DUPLICATE KEY UPDATE password=VALUES(password);

INSERT INTO credentials (employee_id, email, password, role, location, enabled, account_non_locked, created_at, updated_at)
VALUES 
('EMP002', 'hr.us@virtusa.com', '$2a$10$ne9kQu9x4OWzk0nNw9DKGe8fsw7jF9XYebj8IWVnR4vgZwsywH4Qy', 'HR', 'US', true, true, NOW(), NOW())
ON DUPLICATE KEY UPDATE password=VALUES(password);

INSERT INTO credentials (employee_id, email, password, role, location, enabled, account_non_locked, created_at, updated_at)
VALUES 
('EMP003', 'employee.us@virtusa.com', '$2a$10$ne9kQu9x4OWzk0nNw9DKGe8fsw7jF9XYebj8IWVnR4vgZwsywH4Qy', 'EMPLOYEE', 'US', true, true, NOW(), NOW())
ON DUPLICATE KEY UPDATE password=VALUES(password);

INSERT INTO credentials (employee_id, email, password, role, location, enabled, account_non_locked, created_at, updated_at)
VALUES 
('EMP101', 'newemployee@virtusa.com', '$2a$10$7wwETKJRu6Hh5HQO7hXgvO44D9sYQqE0VgQssbT/6BaCR0L6TdXyG', 'EMPLOYEE', 'UK', true, true, NOW(), NOW())
ON DUPLICATE KEY UPDATE password=VALUES(password);
