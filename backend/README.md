# HackBase HRMS Backend

> A production-grade Spring Boot authentication & Employee Management backend built for the Odoo Hackathon.
> Covers company registration, JWT-ready login, employee CRUD, auto ID/password generation, two-factor authentication (2FA) via email OTP, rate limiting with Redis, HTML transactional emails, and BCrypt password security.

---

## 1. Project Overview

HackBase HRMS Backend provides:

| Feature | Implementation |
|---|---|
| Company Registration | Accepts multipart/form-data with logo. Auto-creates the Admin employee |
| Employee CRUD | Admins can create and manage employees. Auto-generates IDs (e.g. OIJODO20260001) and temporary passwords. |
| Login & Security | Email/LoginId + password with JWT and optional 2FA |
| File Uploads | Local `uploads/` directory for storing company logos and profile pictures |
| Two-Factor Authentication | 6-digit OTP via email, stored in Redis with 2-min TTL |
| Rate Limiting | Redis counters — 5 login attempts / 5 min, 3 OTP attempts / 2 min |
| Forgot/Reset Password | Secure UUID token, 15-min expiry |
| Transactional Emails | Responsive HTML emails via Mailtrap (Welcome, OTP, Password Reset) |

---

## 2. Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 3.3.0 |
| ORM | Spring Data JPA + Hibernate | 6.5 |
| Database | MySQL | 8.x |
| Cache / OTP Store | Redis (Lettuce client) | 7.x |
| Security | Spring Security + BCrypt + JWT | — |

---

## 3. Environment Setup

### 3.1 Prerequisites
- Java 21, Maven 3.x
- MySQL 8.x running on port **4408**
- Redis 7.x running on port 6379
- Mailtrap account

### 3.2 Start Redis
```bash
docker run -d --name hackbase-redis -p 6379:6379 redis:alpine
```

### 3.3 Set Environment Variables
```bash
export DB_PASS=your_mysql_password
export MAIL_USER=your_mailtrap_username
export MAIL_PASS=your_mailtrap_password
```

## 4. Running the Application
```bash
mvn spring-boot:run
```

Verify it's running:
```bash
curl http://localhost:8080/api/health
```

---

## 5. API Endpoints Reference

### Auth
- `POST /api/auth/company/signup` (multipart/form-data)
- `POST /api/auth/login` (JSON)
- `POST /api/auth/verify-otp` (JSON)
- `POST /api/auth/2fa/enable` (JSON)
- `POST /api/auth/2fa/disable` (JSON)

### Employee Management
- `POST /api/employees` (Admin only)
- `GET /api/employees` (Admin only)
- `GET /api/employees/{id}` (Self or Admin)
- `PUT /api/employees/{id}/admin` (Admin only)
- `PUT /api/employees/{id}/profile` (Self - multipart/form-data)
- `DELETE /api/employees/{id}` (Admin only)

### Password
- `POST /api/password/forgot`
- `POST /api/password/reset`

---

## 6. Testing with Postman
Import `api-collection.json` into Postman. The collection has pre-configured requests for Company Signup, Login, Employee Creation, and Profile Updates.

## License
Built for the Odoo Hackathon 2026. Educational and demonstration purposes.
