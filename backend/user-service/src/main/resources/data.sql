-- Initial User Service Seed Data
-- Using INSERT IGNORE (or SELECT WHERE NOT EXISTS equivalent) to avoid conflicts on restart

-- 1. Insert Global HR User
INSERT INTO users (employee_id, first_name, last_name, email, role, location, department, business_unit, designation, active, created_at, updated_at)
SELECT 'EMP001', 'Alice', 'Smith', 'alice.smith@virtusa.com', 'GLOBAL_HR', 'SINGAPORE', 'Human Resources', 'Corporate', 'Chief HR Officer', true, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE employee_id = 'EMP001');

-- 2. Insert Hyderabad HR User
INSERT INTO users (employee_id, first_name, last_name, email, role, location, department, business_unit, designation, active, created_at, updated_at)
SELECT 'EMP002', 'Bob', 'Jones', 'bob.jones@virtusa.com', 'HR', 'HYDERABAD', 'Human Resources', 'India BU', 'HR Manager', true, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE employee_id = 'EMP002');

-- 3. Insert Hyderabad Employee User
INSERT INTO users (employee_id, first_name, last_name, email, role, location, department, business_unit, designation, active, created_at, updated_at)
SELECT 'EMP003', 'Charlie', 'Brown', 'charlie.brown@virtusa.com', 'EMPLOYEE', 'HYDERABAD', 'Engineering', 'Digital BU', 'Software Engineer', true, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE employee_id = 'EMP003');

-- 4. Insert London Employee User
INSERT INTO users (employee_id, first_name, last_name, email, role, location, department, business_unit, designation, active, created_at, updated_at)
SELECT 'EMP004', 'Diana', 'Prince', 'diana.prince@virtusa.com', 'EMPLOYEE', 'LONDON', 'Engineering', 'UK BU', 'Senior Architect', true, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE employee_id = 'EMP004');
