-- Dummy HR Data for Testing Dashboard
-- We use INSERT IGNORE to prevent crashing if the data already exists

-- 1. Insert a dummy employee (if not exists)
INSERT IGNORE INTO users (id, email, password, username, role) VALUES (999, 'hr_test@example.com', '$2a$10$xyz', 'HR Test Employee', 'ROLE_EMPLOYEE');

-- 2. Insert Payroll for the dummy employee
INSERT IGNORE INTO payroll (id, user_id, basic_salary, allowances, deductions, net_salary) VALUES (999, 999, 50000.0, 5000.0, 2000.0, 53000.0);

-- 3. Insert some fake leave requests
INSERT IGNORE INTO leave_requests (id, user_id, leave_type, from_date, to_date, remarks, status) VALUES (998, 999, 'SICK', '2026-07-01', '2026-07-02', 'Fever', 'APPROVED');
INSERT IGNORE INTO leave_requests (id, user_id, leave_type, from_date, to_date, remarks, status) VALUES (999, 999, 'PAID', '2026-07-15', '2026-07-20', 'Vacation', 'PENDING');

-- Note: Attendance is highly date-dependent (e.g., "today"), so it is best to test Check-In via Postman directly to see it on the dashboard.
