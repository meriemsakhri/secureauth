# SecureAuth

A secure, reusable authentication & authorization module for web applications, built with Spring Boot and PostgreSQL, with a React frontend. Developed as part of a cybersecurity internship at Proxym-IT.

## Features

- Secure signup and login
- JWT authentication (short-lived access token + long-lived refresh token, with rotation)
- Role-based access control (RBAC)
- Password hashing with BCrypt
- Brute-force protection (account lockout after repeated failed logins)
- Password reset via one-time, expiring token, delivered by real email (Brevo)
- Self-service password change (requires current password)
- Security audit logging (logins, failures, lockouts, password changes/resets)
- CORS and HTTP security headers configured
- Database schema fully versioned via Flyway migrations
- React frontend: login, signup, password recovery, dashboard, profile, and an admin panel

## Tech Stack

**Backend:** Spring Boot 4.1, Spring Security 7, Java 21, PostgreSQL 17 (via Docker), JWT (jjwt), BCrypt, Flyway, Spring Mail (Brevo SMTP)
**Frontend:** React (Vite), React Router, Axios

## Getting Started

### Prerequisites

- JDK 21
- Docker Desktop
- Node.js (for the frontend)
- Maven (or use the included `mvnw` wrapper)

### 1. Clone the repository

```bash
git clone https://github.com/meriemsakhri/secureauth.git
cd secureauth
```

### 2. Create your local environment file

Create a `.env` file in the project root (this file is git-ignored and never committed):

```
POSTGRES_DB=secureauth_db
POSTGRES_USER=secureauth_admin
POSTGRES_PASSWORD=your_local_dev_password
POSTGRES_PORT=5432

DB_NAME=secureauth_db
DB_USERNAME=secureauth_admin
DB_PASSWORD=your_local_dev_password
DB_PORT=5432

JWT_SECRET=your_own_base64_256bit_secret
JWT_ACCESS_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=604800000

BREVO_SMTP_USERNAME=your_brevo_smtp_login
BREVO_SMTP_PASSWORD=your_brevo_smtp_key
MAIL_FROM=your_verified_sender_email

FRONTEND_URL=http://localhost:5173
```

Generate a proper JWT secret with:
```bash
openssl rand -base64 32
```

Email sending requires a free [Brevo](https://www.brevo.com) account with a verified sender and SMTP credentials.

### 3. Start the database

```bash
docker compose up -d
```

### 4. Run the backend

Via IntelliJ: run `SecureauthApplication`, with the `.env` file loaded as environment variables (see the EnvFile plugin, or set them manually in your Run Configuration).

Via terminal:
```bash
./mvnw spring-boot:run
```

The API runs at `http://localhost:8080`. Flyway automatically creates the schema and seeds the `USER`/`ADMIN` roles on first run.

### 5. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The app runs at `http://localhost:5173`.

## API Endpoints

| Method | Endpoint | Auth required | Description |
|---|---|---|---|
| POST | `/api/auth/signup` | No | Register a new account |
| POST | `/api/auth/login` | No | Authenticate and receive tokens |
| POST | `/api/auth/refresh` | No (valid refresh token) | Rotate a refresh token for a new token pair |
| POST | `/api/auth/logout` | No (valid refresh token) | Revoke a refresh token |
| POST | `/api/auth/forgot-password` | No | Request a password reset email |
| POST | `/api/auth/reset-password` | No (valid reset token) | Set a new password using a reset token |
| GET | `/api/users/me` | Yes | Get the authenticated user's info |
| PUT | `/api/users/me/password` | Yes | Change your own password (requires current password) |
| GET | `/api/admin/users` | Yes (ADMIN role) | List all users (admin only) |

A ready-to-import Postman collection covering the core endpoints, including a brute-force lockout test sequence, is available at `postman/SecureAuth.postman_collection.json`.

## Security Design Notes

- Passwords are hashed with BCrypt and never logged or returned by the API.
- Refresh tokens and password reset tokens are stored in the database as SHA-256 hashes, never in raw form.
- Refresh tokens rotate on every use — reusing an old, already-exchanged refresh token is rejected and the token revoked.
- Password reset tokens are single-use and expire after 15 minutes.
- The forgot-password endpoint always returns the same response whether or not the email exists, to prevent account enumeration.
- Accounts lock for 15 minutes after 5 failed login attempts, in line with OWASP guidance.
- Password change requires the current password and is separate from the reset flow.
- All security-relevant events (signup, login success/failure, lockout, password reset/change) are recorded in an audit log with timestamp and IP address.
- Database schema is managed exclusively through Flyway migrations (`src/main/resources/db/migration`) — no automatic schema generation in the running application.
- Access and refresh tokens are stored in `localStorage` on the frontend. This is a documented trade-off for project scope; an httpOnly cookie would be more resistant to XSS-based token theft in a production deployment.

## Project Structure

```
com.secureauth
├── auth/
│   ├── controller/   REST endpoints
│   ├── service/      Business logic (auth, JWT, login attempts, email)
│   ├── model/         JPA entities
│   ├── repository/    Spring Data JPA repositories
│   ├── dto/            Request/response objects
│   ├── security/       Security config, JWT filter, user details service
│   └── exception/      Custom exceptions and global error handling
└── audit/               Security event logging (cross-cutting concern)

frontend/src/
├── api/                Axios instance and API call functions
├── context/             Global auth state (AuthContext)
├── components/          Reusable UI (Navbar, ProtectedRoute, PasswordInput)
└── pages/                Login, Signup, Dashboard, Profile, Admin, etc.
```

## Status

This project is under active development as part of a 1-month internship. See the project's weekly progress reports for current completion status.