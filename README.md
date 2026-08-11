# SecureAuth

A secure, reusable authentication & authorization module for web applications, built with Spring Boot and PostgreSQL. Developed as part of a cybersecurity internship at Proxym-IT.

## Features

- Secure signup and login
- JWT authentication (short-lived access token + long-lived refresh token, with rotation)
- Role-based access control (RBAC)
- Password hashing with BCrypt
- Brute-force protection (account lockout after repeated failed logins)
- Security audit logging (logins, failures, lockouts)
- CORS and HTTP security headers configured
- Database schema fully versioned via Flyway migrations

## Tech Stack

- **Backend:** Spring Boot 4.1, Spring Security 7, Java 21
- **Database:** PostgreSQL 17 (via Docker)
- **Auth:** JWT (jjwt), BCrypt
- **Migrations:** Flyway
- **Build:** Maven

## Getting Started

### Prerequisites

- JDK 21
- Docker Desktop
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
```

Generate a proper JWT secret with:
```bash
openssl rand -base64 32
```

### 3. Start the database

```bash
docker compose up -d
```

### 4. Run the application

Via IntelliJ: run `SecureauthApplication`, with the `.env` file loaded as environment variables (see the EnvFile plugin, or set them manually in your Run Configuration).

Via terminal:
```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

Flyway will automatically create the schema and seed the `USER`/`ADMIN` roles on first run.

## API Endpoints

| Method | Endpoint | Auth required | Description |
|---|---|---|---|
| POST | `/api/auth/signup` | No | Register a new account |
| POST | `/api/auth/login` | No | Authenticate and receive tokens |
| POST | `/api/auth/refresh` | No (valid refresh token) | Rotate a refresh token for a new token pair |
| POST | `/api/auth/logout` | No (valid refresh token) | Revoke a refresh token |
| GET | `/api/users/me` | Yes | Get the authenticated user's info |
| GET | `/api/admin/users` | Yes (ADMIN role) | List all users (admin only) |

A ready-to-import Postman collection covering all endpoints, including a brute-force lockout test sequence, is available at `postman/SecureAuth.postman_collection.json`.

## Security Design Notes

- Passwords are hashed with BCrypt and never logged or returned by the API.
- Refresh tokens are stored in the database as SHA-256 hashes, never in raw form.
- Refresh tokens rotate on every use — reusing an old, already-exchanged refresh token is rejected and the token revoked.
- Accounts lock for 15 minutes after 5 failed login attempts, in line with OWASP guidance.
- All security-relevant events (signup, login success/failure, lockout) are recorded in an audit log with timestamp and IP address.
- Database schema is managed exclusively through Flyway migrations (`src/main/resources/db/migration`) — no automatic schema generation in the running application.

## Project Structure

```
com.secureauth
├── auth/
│   ├── controller/   REST endpoints
│   ├── service/      Business logic (auth, JWT, login attempts)
│   ├── model/         JPA entities
│   ├── repository/    Spring Data JPA repositories
│   ├── dto/            Request/response objects
│   ├── security/       Security config, JWT filter, user details service
│   └── exception/      Custom exceptions and global error handling
└── audit/               Security event logging (cross-cutting concern)
```

## Status

This project is under active development as part of a 1-month internship. See the project's progress reports for current completion status.