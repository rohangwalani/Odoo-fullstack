# HRMS Authentication Module - Dummy Data

To make testing easier, a DataSeeder runs automatically on startup if the database is empty. It provisions a dummy company, an admin, and regular employees.

## 1. Company
- **Company Name:** Tech Innovators Inc
- **Email:** admin@techinnovators.com
- **Phone:** 1234567890
- **Password:** Admin@123 (Used for Company Login, if applicable)

## 2. Admin Employee
Use this account to test Employee Management and Profile endpoints for Admin role.
- **Name:** Alice Admin
- **Email:** alice@techinnovators.com
- **Login ID:** TEAL20230001 (Auto-generated based on Company + Name + Year + Serial)
- **Password:** Password@123
- **Role:** ADMIN

## 3. Regular Employees
Use these accounts to test Profile endpoints and restricted API access.

**Employee 1**
- **Name:** Bob Builder
- **Email:** bob@techinnovators.com
- **Login ID:** TEBO20230002
- **Password:** Password@123
- **Role:** EMPLOYEE
- **Department:** Engineering

**Employee 2**
- **Name:** Charlie Chaplin
- **Email:** charlie@techinnovators.com
- **Login ID:** TECH20230003
- **Password:** Password@123
- **Role:** EMPLOYEE
- **Department:** Marketing

---
*Note: All passwords are set to `Password@123` and `temporaryPassword = false` for easier testing without going through the reset flow.*
