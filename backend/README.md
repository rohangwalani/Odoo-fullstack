# HackBase Backend

> A production-grade Spring Boot authentication backend built for the Odoo Hackathon.
> Covers user registration, JWT-ready login, two-factor authentication (2FA) via email OTP,
> rate limiting with Redis, HTML transactional emails, and BCrypt password security.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Tech Stack](#2-tech-stack)
3. [Architecture & Package Structure](#3-architecture--package-structure)
4. [Commit History — What Was Built & Why](#4-commit-history--what-was-built--why)
5. [Environment Setup](#5-environment-setup)
6. [Running the Application](#6-running-the-application)
7. [API Endpoints Reference](#7-api-endpoints-reference)
8. [2FA Flow — How It Works](#8-2fa-flow--how-it-works)
9. [Rate Limiting — How It Works](#9-rate-limiting--how-it-works)
10. [Input Validation Rules](#10-input-validation-rules)
11. [HTML Email Templates](#11-html-email-templates)
12. [Testing with Postman](#12-testing-with-postman)
13. [Verifying Redis in Real Time](#13-verifying-redis-in-real-time)
14. [Database Schema](#14-database-schema)
15. [Security Decisions](#15-security-decisions)

---

## 1. Project Overview

HackBase is a RESTful authentication backend that provides:

| Feature | Implementation |
|---|---|
| User Registration | BCrypt password hashing, duplicate detection |
| Login | Email + password with optional 2FA |
| Two-Factor Authentication | 6-digit OTP via email, stored in Redis with 2-min TTL |
| Rate Limiting | Redis counters — 5 login attempts / 5 min, 3 OTP attempts / 2 min |
| Forgot/Reset Password | Secure UUID token, 15-min expiry |
| Input Validation | OWASP-aligned regex for username, password strength |
| Transactional Emails | Responsive HTML emails via Mailtrap (dev) |
| CORS | Configured for React (3000) and Vite (5173) frontends |

---

## 2. Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 3.3.0 |
| ORM | Spring Data JPA + Hibernate | 6.5 |
| Database | MySQL | 8.x |
| Cache / OTP Store | Redis (Lettuce client) | 7.x |
| Email | Spring Mail + Mailtrap SMTP | — |
| Security | Spring Security + BCrypt | — |
| Validation | Jakarta Validation (Bean Validation 3) | — |
| Build | Maven | 3.x |

---

## 3. Architecture & Package Structure

```
backend/
├── src/main/java/com/hackathon/backend/
│   ├── BackendApplication.java          # Main entry point
│   │
│   ├── auth/                            # All authentication concerns
│   │   ├── AuthController.java          # REST endpoints for /api/auth/**
│   │   ├── AuthService.java             # Login, register, 2FA, OTP verify
│   │   ├── dto/
│   │   │   ├── AuthResponse.java        # Unified API response DTO
│   │   │   ├── LoginRequest.java        # Login input DTO
│   │   │   ├── RegisterRequest.java     # Registration input DTO (with regex)
│   │   │   └── VerifyOtpRequest.java    # 2FA OTP submission DTO
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java    # @RestControllerAdvice — maps exceptions to HTTP
│   │       └── RateLimitExceededException.java # Custom exception → HTTP 429
│   │
│   ├── config/
│   │   └── SecurityConfig.java          # Spring Security, BCrypt, CORS
│   │
│   ├── controller/
│   │   ├── HealthController.java        # GET /api/health
│   │   └── PasswordResetController.java # POST /api/password/forgot + /reset
│   │
│   ├── model/
│   │   └── User.java                    # JPA entity (users table)
│   │
│   ├── repository/
│   │   └── UserRepository.java          # findByEmail, findByUsername, findByResetToken
│   │
│   └── service/
│       ├── EmailService.java            # HTML email sender (MimeMessage)
│       ├── RateLimitService.java        # Redis-backed rate limiting
│       └── TwoFactorService.java        # SecureRandom OTP + Redis TTL
│
├── src/main/resources/
│   ├── application.properties           # All config (no secrets hardcoded)
│   └── schema.sql                       # Reference schema (NOT auto-run)
│
├── .env.example                         # Required env vars template
├── api-collection.json                  # Postman collection v2
└── pom.xml                              # Maven dependencies
```

### Design Principles

- **Thin controller, fat service**: Controllers only validate input and delegate to services.
- **DTO separation**: Input DTOs (`RegisterRequest`, `LoginRequest`) are decoupled from the `User` entity.
- **Single responsibility**: `TwoFactorService` owns only OTP lifecycle; `RateLimitService` owns only counters; `EmailService` owns only email delivery.
- **Non-blocking email**: Email failures do not roll back business operations (registration, login, password reset).

---

## 4. Commit History — What Was Built & Why

### `747b208` — Initial boilerplate
Initial Spring Boot project scaffold. Sets up the project groupId (`com.hackathon`), packaging, and Spring Boot parent (3.3.0).

### `cad7ba4` — User Entity, Repository, Service, Controller
**Files:** `User.java`, `UserRepository.java`, `UserService.java`, `UserController.java`

Creates the core JPA `User` entity with fields: `id`, `username`, `email`, `password`, `createdAt`. Sets up `UserRepository` with `findByEmail` and `findByUsername`. Exposes a basic `/api/users` controller.

### `deb5766` — Forgot Password flow + BCrypt
**Files:** `PasswordResetController.java`, `SecurityConfig.java` (initial)

Implements the forgot-password + reset-password flow:
- `POST /api/password/forgot`: Generates a UUID reset token, sets 15-min expiry, sends email.
- `POST /api/password/reset`: Validates token, updates password with BCrypt hash.
- Wires `BCryptPasswordEncoder` as the `PasswordEncoder` bean.

### `bab5e4e` — Environment variables for DB credentials
**Files:** `application.properties`

Switches DB credentials from hardcoded values to `${DB_USER}`, `${DB_PASS}`, `${DB_PORT}` environment variable placeholders. First security improvement.

### `a49a452` — Scaffold auth package
**Files:** `auth/` package structure

Creates the dedicated `auth` package to isolate authentication logic from other concerns. Establishes the separation: `auth/AuthController`, `auth/AuthService`, `auth/dto/`, `auth/exception/`.

### `b6b77e4` — Configure MySQL on port 4408 + Mailtrap SMTP
**Files:** `application.properties`

Reconfigures datasource URL to use `DB_PORT=4408` (non-standard port used by Odoo's MySQL service). Wires Mailtrap sandbox SMTP (`sandbox.smtp.mailtrap.io:2525`) for email testing without sending real emails.

### `6fe9c03` — AuthService, AuthController, GlobalExceptionHandler
**Files:** `AuthService.java`, `AuthController.java`, `GlobalExceptionHandler.java`, `AuthResponse.java`, `RegisterRequest.java`, `LoginRequest.java`

Core authentication implementation:
- **`AuthService.register()`**: Validates uniqueness, hashes password with BCrypt, saves user, sends welcome email.
- **`AuthService.login()`**: Finds user by email, verifies BCrypt hash, returns user details.
- **`AuthResponse`**: Unified response DTO with static factory methods (`success()`, `failure()`).
- **`GlobalExceptionHandler`**: `@RestControllerAdvice` mapping `IllegalArgumentException` → 409/401, `MethodArgumentNotValidException` → 400 with field-level errors.

### `f3830c3` — Verify MySQL connection, create database
**Files:** (runtime verification)

Confirms MySQL connection on port 4408, creates `hackathon_db` schema, and verifies Hibernate DDL auto-creation of the `users` table.

### `a2a0a53` — Postman test suite v1
**Files:** `api-collection.json`

First version of the Postman collection with automated test scripts for register, login, duplicate detection, and health check.

### `8c96f2b` — MySQL port fix, disable schema.sql, add seed data
**Files:** `application.properties`, `schema.sql`, `SecurityConfig.java`

- Sets `spring.sql.init.mode=never` — prevents Spring from auto-running `schema.sql` (Hibernate's `ddl-auto=update` handles DDL).
- Adds dummy seed data to `schema.sql` for manual reference.
- Adds explicit `permitAll()` for `/api/password/**` in `SecurityConfig`.

### `3bfffba` — Remove redundant CorsConfig.java
**Files:** `CorsConfig.java` (deleted)

Removes the `WebMvcConfigurer`-based CORS configuration that was causing duplicate `Access-Control-Allow-Origin` headers. CORS is correctly handled entirely by Spring Security's `corsConfigurationSource()` bean in `SecurityConfig`.

### `ff349ed` — **SECURITY: Remove hardcoded credentials**
**Files:** `application.properties`, `.env.example`

**Critical security fix:**
- Removes `${DB_PASS:jumble@123}`, `${MAIL_USER:...}`, `${MAIL_PASS:...}` hardcoded fallback defaults.
- Now `DB_PASS`, `MAIL_USER`, `MAIL_PASS` have **no default** — the app will fail to start if they're not set. This prevents accidental credential exposure in public repositories.
- Adds `spring.data.redis.host` and `spring.data.redis.port` configuration for upcoming 2FA.
- Creates `.env.example` documenting every required environment variable with quick-start commands for Git Bash and PowerShell.

### `7f5a074` — **Regex validations for registration**
**Files:** `RegisterRequest.java`

Strengthens input validation with OWASP-aligned rules:

| Field | Rule | Regex |
|---|---|---|
| `username` | Letters, digits, underscores only; 3–50 chars | `^[a-zA-Z0-9_]+$` |
| `password` | Min 8 chars; requires uppercase, lowercase, digit, special char; max 72 (bcrypt limit) | `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#^+=])...` |

### `b1efca8` — Add Redis dependency
**Files:** `pom.xml`

Adds `spring-boot-starter-data-redis` which bundles the Lettuce async Redis client. No additional configuration needed — Spring Boot auto-configures `StringRedisTemplate` using `spring.data.redis.host` and `spring.data.redis.port`.

### `f96d4ee` — Add `twoFactorEnabled` to User entity
**Files:** `User.java`

Adds the `two_factor_enabled` column to the `users` table:
```java
@Column(name = "two_factor_enabled", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
private boolean twoFactorEnabled = false;
```
Hibernate's `ddl-auto=update` automatically adds the column on next startup — no manual migration needed. Default is `false` (opt-in).

### `c2a2566` — TwoFactorService, RateLimitService, exception, VerifyOtpRequest
**Files:** `TwoFactorService.java`, `RateLimitService.java`, `RateLimitExceededException.java`, `VerifyOtpRequest.java`

The core infrastructure for 2FA:

**`TwoFactorService`:**
- Uses `java.security.SecureRandom` (CSPRNG) to generate 6-digit codes (100000–999999).
- Stores codes in Redis with key `otp:{userId}` and TTL of 2 minutes.
- `verifyOtp()` deletes the key on match (one-time use).

**`RateLimitService`:**
- Uses Redis `INCR` for atomic counting.
- Login: key `rate:login:{email}`, max 5, window 5 min.
- OTP: key `rate:otp:{userId}`, max 3, window 2 min.
- TTL is set only on the first increment (fixed window, not sliding).

**`RateLimitExceededException`:** Maps to HTTP 429 via `GlobalExceptionHandler`.

**`VerifyOtpRequest`:** DTO with `userId` (Long, `@NotNull`) and `otp` (String, `@Pattern(^[0-9]{6}$)`).

### `80d9e9a` — Integrate 2FA into login flow
**Files:** `AuthResponse.java`, `GlobalExceptionHandler.java`, `AuthService.java`, `AuthController.java`

**`AuthService.login()` new flow:**
1. Check `isLoginRateLimited(email)` → throw `RateLimitExceededException` if exceeded.
2. Find user — record attempt and throw generic error if not found (prevents email enumeration).
3. Verify BCrypt hash — record attempt if wrong.
4. Clear rate limit counter on credential success.
5. If `twoFactorEnabled`: generate OTP, send 2FA email, return `AuthResponse.pending2FA(userId)`.
6. If not: return full success response directly.

**`AuthService.verifyOtp()`:** Validates OTP rate limit, checks OTP against Redis, returns full login response on match.

**`AuthController` new endpoints:**
- `POST /api/auth/verify-otp` — OTP submission
- `POST /api/auth/2fa/enable` — enable 2FA
- `POST /api/auth/2fa/disable` — disable 2FA

### `b070d0c` — HTML email templates
**Files:** `EmailService.java`

Replaces `SimpleMailMessage` (plain text) with `MimeMessage` + `MimeMessageHelper` for HTML email delivery.

Three fully styled templates with consistent HackBase brand:
- **Welcome**: Account details card, 2FA security tip, indigo branding.
- **Password Reset**: CTA button, raw link fallback, 15-min expiry warning.
- **2FA OTP**: Large monospace OTP display (`483 921`), red "never share" warning, yellow 2-min expiry.

### `93730e0` — Postman collection v2 (2FA + rate limit tests)
**Files:** `api-collection.json`

Complete rebuild of the Postman collection with automated test scripts covering:
- Registration validation (weak password, invalid username, duplicate).
- Login without 2FA.
- Full 2FA flow (enable → login → OTP → disable → login again).
- Rate limit brute-force simulation.
- HTML email verification via Mailtrap.

---

## 5. Environment Setup

### 5.1 Prerequisites

| Tool | Required | Notes |
|---|---|---|
| Java 21 | ✅ | `java -version` |
| Maven 3.x | ✅ | `mvn -version` |
| MySQL 8.x | ✅ | Running on port **4408** |
| Redis 7.x | ✅ | Running on port 6379 |
| Mailtrap account | ✅ | Free at [mailtrap.io](https://mailtrap.io) |

### 5.2 Start Redis

**Option A — Docker (recommended):**
```bash
docker run -d --name hackbase-redis -p 6379:6379 redis:alpine
```

**Option B — WSL (Windows Subsystem for Linux):**
```bash
sudo apt install redis-server
sudo service redis-server start
redis-cli ping   # should print PONG
```

**Option C — Windows Native Redis:**
Download from [github.com/microsoftarchive/redis/releases](https://github.com/microsoftarchive/redis/releases)
```powershell
redis-server.exe
```

### 5.3 Get Mailtrap Credentials

1. Go to [mailtrap.io](https://mailtrap.io) → **Email Testing** → **My Inbox**
2. Click **SMTP Settings** tab
3. Copy **Username** and **Password**

### 5.4 Set Environment Variables

**Git Bash / Linux / macOS:**
```bash
export DB_PASS=your_mysql_password
export MAIL_USER=your_mailtrap_username
export MAIL_PASS=your_mailtrap_password
```

**PowerShell:**
```powershell
$env:DB_PASS="your_mysql_password"
$env:MAIL_USER="your_mailtrap_username"
$env:MAIL_PASS="your_mailtrap_password"
```

> All other variables have safe defaults (`DB_PORT=4408`, `DB_NAME=hackathon_db`, `DB_USER=root`, `REDIS_HOST=localhost`, `REDIS_PORT=6379`). Override only if needed.

---

## 6. Running the Application

```bash
# Git Bash (all-in-one)
export DB_PASS=jumble@123 && export MAIL_USER=youruser && export MAIL_PASS=yourpass && mvn spring-boot:run

# PowerShell (all-in-one)
$env:DB_PASS="jumble@123"; $env:MAIL_USER="youruser"; $env:MAIL_PASS="yourpass"; mvn spring-boot:run
```

**Expected startup output:**
```
Tomcat started on port 8080 (http)
Started BackendApplication in ~3 seconds
```

**Verify:**
```bash
curl http://localhost:8080/api/health
# → {"status":"running","database":"connected"}
```

---

## 7. API Endpoints Reference

### Base URL: `http://localhost:8080`

#### Health

| Method | Endpoint | Description | Response |
|---|---|---|---|
| GET | `/api/health` | Server + DB status | `200 OK` |

#### Registration & Login

| Method | Endpoint | Description | Success | Failure |
|---|---|---|---|---|
| POST | `/api/auth/register` | Register new user | `201 Created` | `400` (validation) / `409` (duplicate) |
| POST | `/api/auth/login` | Login with credentials | `200 OK` | `401` (bad creds) / `429` (rate limited) |

**Login response when 2FA is DISABLED:**
```json
{
  "success": true,
  "message": "Login successful. Welcome back, john_doe!",
  "userId": 1,
  "username": "john_doe",
  "email": "john@example.com"
}
```

**Login response when 2FA is ENABLED:**
```json
{
  "success": false,
  "requires2FA": true,
  "pendingUserId": 1,
  "message": "Two-factor authentication required. A 6-digit code has been sent to your email."
}
```

#### 2FA Endpoints

| Method | Endpoint | Body | Description | Response |
|---|---|---|---|---|
| POST | `/api/auth/verify-otp` | `{userId, otp}` | Submit 2FA code | `200` / `401` / `429` |
| POST | `/api/auth/2fa/enable` | `{userId}` | Enable 2FA for user | `200 OK` |
| POST | `/api/auth/2fa/disable` | `{userId}` | Disable 2FA for user | `200 OK` |

#### Password Reset

| Method | Endpoint | Body | Description | Response |
|---|---|---|---|---|
| POST | `/api/password/forgot` | `{email}` | Request reset link (email sent) | Always `200` |
| POST | `/api/password/reset` | `{token, newPassword}` | Reset password with token | `200` / `401` |

---

## 8. 2FA Flow — How It Works

```
User                    AuthController         AuthService          TwoFactorService       Redis             EmailService
 |                           |                      |                      |                  |                   |
 |-- POST /login ----------->|                      |                      |                  |                   |
 |                           |-- login(req) ------->|                      |                  |                   |
 |                           |                      |-- isRateLimited? --->|                  |                   |
 |                           |                      |<-- false ------------|                  |                   |
 |                           |                      |-- findByEmail(DB) -->|                  |                   |
 |                           |                      |-- matchPassword() -->|                  |                   |
 |                           |                      |-- twoFactorEnabled? →YES               |                   |
 |                           |                      |-- generateAndStoreOtp(userId) -------->|                   |
 |                           |                      |                      |-- SET otp:1 →6digits, TTL=2min ---->|
 |                           |                      |-- sendTwoFactorEmail() --------------------------------------------->|
 |<-- {requires2FA:true,     |                      |                      |                  |                   |
 |     pendingUserId:1} -----|                      |                      |                  |                   |
 |                           |                      |                      |                  |                   |
 |    (User checks Mailtrap) |                      |                      |                  |                   |
 |                           |                      |                      |                  |                   |
 |-- POST /verify-otp ------>|                      |                      |                  |                   |
 |   {userId:1, otp:"483921"}|-- verifyOtp(req) --->|                      |                  |                   |
 |                           |                      |-- isOtpRateLimited? ->|                  |                   |
 |                           |                      |<-- false -------------|                  |                   |
 |                           |                      |-- recordOtpAttempt() →|                 |                   |
 |                           |                      |-- verifyOtp(1,"483921") ------------->|                     |
 |                           |                      |                      |-- GET otp:1 ---->|                   |
 |                           |                      |                      |<-- "483921" ------|                  |
 |                           |                      |                      |-- DEL otp:1 ---->|  (one-time use)  |
 |                           |                      |                      |<-- OK ------------|                  |
 |                           |                      |-- clearOtpAttempts()->|                  |                   |
 |<-- {success:true,         |                      |                      |                  |                   |
 |     userId, username} ----|                      |                      |                  |                   |
```

### Key Security Properties

| Property | Detail |
|---|---|
| OTP generator | `java.security.SecureRandom` — cryptographically secure PRNG |
| OTP space | 6 digits = 1,000,000 possibilities (vs 10,000 for 4-digit) |
| TTL | 2 minutes — OTP auto-expires in Redis |
| One-time use | OTP key deleted from Redis on successful verification |
| Brute-force protection | Max 3 wrong OTP attempts before 429 lockout |
| Email privacy | Login returns same error for wrong email vs wrong password |

---

## 9. Rate Limiting — How It Works

### How Redis Counters Work

```
First bad login for john@example.com:
  INCR rate:login:john@example.com  →  1  (key created, TTL = 5 minutes set)

Second bad login:
  INCR rate:login:john@example.com  →  2  (TTL not reset)

...5th bad login:
  INCR rate:login:john@example.com  →  5

6th attempt:
  GET rate:login:john@example.com   →  "5" >= 5  →  throw RateLimitExceededException → HTTP 429

After 5 minutes:
  Key expires automatically in Redis → counter resets → user can try again
```

### Rate Limit Configuration

| Endpoint | Redis Key | Max Attempts | Window | Reset |
|---|---|---|---|---|
| POST `/api/auth/login` | `rate:login:{email}` | 5 | 5 minutes | On successful login |
| POST `/api/auth/verify-otp` | `rate:otp:{userId}` | 3 | 2 minutes | On successful OTP |

### Verify Rate Limits in Redis CLI

```bash
# Check login attempt counter
redis-cli GET rate:login:john_doe@example.com

# Check remaining TTL on the window (seconds)
redis-cli TTL rate:login:john_doe@example.com

# Check OTP attempt counter
redis-cli GET rate:otp:1

# View active OTP for user ID 1
redis-cli GET otp:1

# Check OTP TTL (should be ≤ 120 seconds)
redis-cli TTL otp:1

# Clear a rate limit manually (for testing)
redis-cli DEL rate:login:john_doe@example.com
```

---

## 10. Input Validation Rules

### Registration (`POST /api/auth/register`)

| Field | Rule | Error Message |
|---|---|---|
| `username` | Required, 3–50 chars | "Username is required" |
| `username` | Only `[a-zA-Z0-9_]` | "Username may only contain letters, digits, and underscores" |
| `email` | Valid email format | "Please provide a valid email address" |
| `password` | Required | "Password is required" |
| `password` | Max 72 chars (bcrypt limit) | "Password must not exceed 72 characters" |
| `password` | Min 8 chars + uppercase + lowercase + digit + special `@$!%*?&#^+=` | "Password must be at least 8 characters and include uppercase, lowercase, digit, and special character" |

**Valid password examples:** `Secret@123`, `MyP@ss1!`, `Hackathon#2026`

**Invalid examples:**

| Password | Reason |
|---|---|
| `simple123` | No uppercase, no special char |
| `SECRET@1` | No lowercase |
| `Secret1` | No special char |
| `Secret@` | No digit |
| `Sec@1` | Less than 8 chars |

### Login (`POST /api/auth/login`)

| Field | Rule |
|---|---|
| `email` | Required, valid email format |
| `password` | Required (no strength check — allows existing users to log in) |

### OTP Verification (`POST /api/auth/verify-otp`)

| Field | Rule |
|---|---|
| `userId` | Required (Long, `@NotNull`) |
| `otp` | Required, exactly 6 digits (`^[0-9]{6}$`) |

---

## 11. HTML Email Templates

All emails share a consistent brand design:

- **Background**: `#F0F4FF` (soft blue-lavender)
- **Card**: White with `border-radius:16px` and subtle box-shadow
- **Brand color**: `#4F46E5` (Indigo-600)
- **Font**: System font stack (`-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto`)

### Welcome Email (`sendWelcomeEmail`)
- Triggered by: `POST /api/auth/register` (success)
- Contains: Username greeting, account details card with indigo border, green 2FA security tip callout
- Subject: `Welcome to HackBase! ⚡`

### Password Reset Email (`sendPasswordResetEmail`)
- Triggered by: `POST /api/password/forgot`
- Contains: Indigo "Reset My Password" CTA button, raw link fallback, yellow 15-min expiry warning
- Subject: `Reset your HackBase password`

### 2FA OTP Email (`sendTwoFactorEmail`)
- Triggered by: `POST /api/auth/login` (when `twoFactorEnabled=true`)
- Contains: Large monospace OTP code (formatted `483 921`), red "Never share" warning, yellow 2-min expiry
- Subject: `Your HackBase login code: 483921`

> **View emails:** All emails go to Mailtrap sandbox inbox. Go to [mailtrap.io](https://mailtrap.io) → Email Testing → My Inbox.

---

## 12. Testing with Postman

### Import the Collection

1. Open Postman
2. Click **Import** (top-left)
3. Choose **File** → select `api-collection.json` from the `backend/` folder
4. The collection **HackBase Auth API** appears in the sidebar

### Run Order

Run requests in this exact order for best results:

```
1. Health Check                          — verify server is up
2. Register — Happy Path                 — creates user, captures registeredUserId
3. Login — Happy Path (2FA Disabled)     — verify direct login works
4. Enable 2FA for User                   — uses {{registeredUserId}} variable
5. Login with 2FA Enabled                — captures pendingUserId, CHECK MAILTRAP
6. Verify OTP                            — paste OTP from Mailtrap, replace placeholder
7. Disable 2FA                           — re-enables direct login
8. Login after Disable                   — verify direct login works again
```

### Automated Variables

The collection uses two collection-level variables that auto-populate:

| Variable | Set by | Used by |
|---|---|---|
| `{{registeredUserId}}` | Register — Happy Path (test script) | Enable 2FA, Disable 2FA |
| `{{pendingUserId}}` | Login with 2FA Enabled (test script) | Verify OTP, OTP Rate Limit test |

### Rate Limit Testing

```
In Postman → "Rate Limiting" folder:

Login Brute Force:
  Run "Rate Limit — Login Brute Force" 6 times in a row
  → Attempts 1–5: 401 Unauthorized
  → Attempt 6: 429 Too Many Requests ✅

OTP Brute Force:
  First enable 2FA and login to get pendingUserId
  Run "Rate Limit — OTP Brute Force" 4 times
  → Attempts 1–3: 401 Unauthorized
  → Attempt 4: 429 Too Many Requests ✅
```

---

## 13. Verifying Redis in Real Time

Open a terminal with Redis CLI while testing:

```bash
# Monitor all Redis commands live
redis-cli MONITOR

# Or check specific keys after testing:

# See all OTP keys
redis-cli KEYS "otp:*"

# See all rate limit keys
redis-cli KEYS "rate:*"

# Full inspection of OTP for user 1
redis-cli GET otp:1
redis-cli TTL otp:1    # remaining seconds (max 120)

# Full inspection of login rate limit for an email
redis-cli GET "rate:login:john_doe@example.com"
redis-cli TTL "rate:login:john_doe@example.com"

# Reset everything for a clean test run
redis-cli FLUSHDB      # WARNING: clears ALL keys in current DB
```

---

## 14. Database Schema

Hibernate manages the schema via `ddl-auto=update`. Reference SQL (in `schema.sql`):

```sql
CREATE TABLE users (
    id                 BIGINT         NOT NULL AUTO_INCREMENT,
    username           VARCHAR(255)   NOT NULL UNIQUE,
    email              VARCHAR(255)   NOT NULL UNIQUE,
    password           VARCHAR(255)   NOT NULL,
    created_at         DATETIME(6)    NOT NULL,
    reset_token        VARCHAR(255),
    reset_token_expiry DATETIME(6),
    two_factor_enabled TINYINT(1)     NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
```

### Column Notes

| Column | Type | Notes |
|---|---|---|
| `password` | VARCHAR(255) | BCrypt hash (60 chars), max input 72 chars |
| `reset_token` | VARCHAR(255) | UUID v4, null when no active reset |
| `reset_token_expiry` | DATETIME | 15 minutes from request time |
| `two_factor_enabled` | TINYINT(1) | 0=false, 1=true; default 0 |

---

## 15. Security Decisions

### Why SecureRandom over Math.random()?
`Math.random()` uses a **predictable pseudo-random sequence** seeded from system time. An attacker who knows the approximate seed could predict future OTPs. `java.security.SecureRandom` uses OS-level entropy sources (hardware RNG, `/dev/urandom`) — outputs are **computationally infeasible to predict**.

### Why 6 digits instead of 4?
| Digits | Combinations | Brute-force probability (1 guess) |
|---|---|---|
| 4 | 10,000 | 1/10,000 = 0.01% |
| 6 | 1,000,000 | 1/1,000,000 = 0.0001% |

6 digits with a 3-attempt limit and 2-minute window makes brute-force practically impossible.

### Why the same error for wrong email vs wrong password?
`"Invalid email or password."` — Both cases return the exact same message. This prevents **email enumeration attacks** where an attacker queries which emails are registered by observing different error messages.

### Why record rate limit attempts BEFORE DB password check?
Rate limiting is checked **before querying the database**. This prevents timing-based attacks where an attacker could use response time differences to infer whether an email exists.

### Why bcrypt max 72 chars?
BCrypt internally truncates input to 72 bytes. Passwords longer than 72 characters are silently truncated. The `@Size(max=72)` annotation makes this limit explicit to the user and prevents unexpected behavior.

### Why fixed-window rate limiting instead of sliding-window?
Fixed window is simpler, requires fewer Redis operations, and is sufficient for a hackathon. A sliding window would require `ZADD`/`ZRANGE` operations. For production, consider [Bucket4j](https://github.com/bucket4j/bucket4j) or similar.

---

## License

Built for the Odoo Hackathon 2026. Educational and demonstration purposes.
