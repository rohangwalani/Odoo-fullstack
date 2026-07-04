# HRMS Backend Developer 1 Roadmap - Functionality Log

This document details the functionality implemented in each professional commit for the Authentication, Company Management & Employee Management Module.

## Branch: `feature/auth-module`

---

### Commit: `chore: switch to PostgreSQL and add Lombok` *(Note: Setup was adjusted per feedback)*
- **Files Modified:** `pom.xml`, `application.properties`
- **Functionality:** 
  - Initially configured PostgreSQL, but reverted back to MySQL per user feedback.
  - Successfully added Lombok (`org.projectlombok:lombok`) to simplify boilerplate code (getters/setters, constructors) across the entities.
  - Retained existing Mailtrap and Redis configurations.

---

### Commit: `refactor(entity): update Company and Employee entities with Lombok`
- **Files Modified:** `pom.xml`, `Company.java`, `Employee.java`, `AuthService.java`
- **Functionality:** 
  - **Lombok Integration:** Removed hundreds of lines of boilerplate getters/setters/constructors by utilizing `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, and `@Builder`.
  - **Entity Refactoring:** Updated the fields to exactly match the requested schema (e.g., `companyLogo`, `createdAt`, `updatedAt`, `isTemporaryPassword`). 
  - Added `@UpdateTimestamp` for automatic update tracking.
  - Adjusted builder warnings for boolean fields (`temporaryPassword`, `twoFactorEnabled`) by adding `@Builder.Default`.
  - Fixed compilation errors in `AuthService` caused by field renaming.

---

### Commit: `feat(auth): implement authentication and company registration APIs`
- **Files Modified:** `AuthController.java`, `AuthService.java`, `ChangePasswordRequest.java`, `ForgotPasswordRequest.java`, `ResetPasswordRequest.java`
- **Functionality:** 
  - **Company Registration:** `POST /api/auth/company/signup` allows a company to register. Validates uniqueness, uploads logos, creates the company, and automatically provisions an Admin employee account.
  - **Login:** `POST /api/auth/login` supports login via email or auto-generated `loginId`, rate limits attempts, and returns a JWT token.
  - **Logout:** `POST /api/auth/logout` added to explicitly handle user logout.
  - **Password Management:** Implemented `PUT /api/auth/change-password`, `POST /api/auth/forgot-password`, and `POST /api/auth/reset-password` endpoints, complete with secure validation and UUID-based reset tokens.

---

### Commit: `feat(profile): implement profile management and avatar upload APIs`
- **Files Modified:** `EmployeeController.java`, `ProfileController.java`
- **Functionality:** 
  - **Profile Controller Separation:** Moved profile-specific endpoints from `EmployeeController` into a dedicated `ProfileController` at `/api/profile` as requested by the architecture map.
  - **Profile APIs:** 
    - `GET /api/profile`: Retrieves the authenticated user's profile.
    - `PUT /api/profile`: Updates the authenticated user's profile (address, phone).
    - `PUT /api/profile/avatar`: Uploads a multipart image (PNG/JPG up to 5MB via `FileStorageService`) and sets it as the `profilePicture`.

---

### Commit: `feat(seed): add data seeder and testing documentation`
- **Files Modified:** `DataSeeder.java`, `dummy_data.md`
- **Functionality:**
  - **Database Seeding:** Implemented a `CommandLineRunner` that runs on application startup. If the database is empty, it automatically provisions a dummy `Company` ("Tech Innovators Inc"), a dummy Admin ("Alice Admin"), and two regular Employees ("Bob Builder", "Charlie Chaplin").
  - **Testing Data Markdown:** Provided a comprehensive reference file (`dummy_data.md`) listing all generated IDs, emails, and passwords to instantly test endpoints without needing to go through the signup flow manually.

---

### Pre-existing Functionality Retained/Utilized
- **Employee Generators:** `EmployeeIdGenerator.java` was already accurately implementing the `CompanyCode + Initials + Year + Serial` logic (e.g. `TEAL20230001`). `PasswordGenerator.java` generates the required `Emp@XXXXX` format.
- **Employee CRUD:** Implemented in `EmployeeController` with proper `@PreAuthorize("hasRole('ADMIN')")` security.
- **JWT Security:** Configured in `SecurityConfig.java`, `JwtUtils.java`, and `JwtAuthenticationFilter.java` using Spring Security 6 components.
- **Email Service:** Spring Mail implementation using Mailtrap in `EmailService.java`.
