# Client Portal

A freelance client management REST API built with Java 21 and Spring Boot. Manage projects, invoices, and file attachments — with JWT authentication, email notifications, PDF generation, and role-based access control.

## Tech Stack

- **Java 21** + **Spring Boot 3.4**
- **PostgreSQL 17** with Flyway migrations
- **Spring Security** + JWT (jjwt)
- **Spring Mail** for email notifications
- **OpenPDF** for invoice PDF generation
- **Docker** + Docker Compose
- **Swagger / OpenAPI** (`/swagger-ui.html`)

## Features

- JWT register & login
- Project CRUD (PENDING / IN_PROGRESS / COMPLETED)
- Invoice CRUD with PDF download (`GET .../invoices/{id}/pdf`)
- File upload & download per project
- Email notifications: invoice created, project status changed, overdue invoice reminder (daily scheduler)
- Admin endpoints: view all users, projects, and invoices (`ROLE_ADMIN` only)
- Role-based access: `ADMIN` and `CLIENT`

## Running Locally

### Prerequisites

- Docker & Docker Compose

### 1. Clone and configure

```bash
git clone https://github.com/PeterTomko99/clientportal.git
cd clientportal
cp .env.example .env
```

Edit `.env` and set at minimum:

```
JWT_SECRET=<256-bit base64 secret>
```

For email notifications, also fill in `MAIL_USERNAME` and `MAIL_PASSWORD` (Gmail App Password works).

### 2. Start

```bash
docker compose up --build
```

The API is available at `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Running without Docker

Requires Java 21 and a running PostgreSQL instance.

```bash
cp .env.example .env   # fill in values
./mvnw spring-boot:run
```

## API Overview

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | — | Register a new user |
| POST | `/api/auth/login` | — | Login, returns JWT |
| GET/POST | `/api/projects` | JWT | List / create projects |
| GET/PUT/DELETE | `/api/projects/{id}` | JWT | Get / update / delete project |
| GET/POST | `/api/projects/{id}/invoices` | JWT | List / create invoices |
| GET | `/api/projects/{id}/invoices/{iid}/pdf` | JWT | Download invoice as PDF |
| GET/POST | `/api/projects/{id}/files` | JWT | List / upload files |
| GET | `/api/projects/{id}/files/{fid}/download` | JWT | Download file |
| GET | `/api/admin/users` | JWT + ADMIN | List all users |
| GET | `/api/admin/projects` | JWT + ADMIN | List all projects |
| GET | `/api/admin/invoices` | JWT + ADMIN | List all invoices |

Full interactive docs at `/swagger-ui.html`.

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `clientportal` | Database name |
| `DB_USER` | `postgres` | Database user |
| `DB_PASSWORD` | `postgres` | Database password |
| `SERVER_PORT` | `8080` | App port |
| `JWT_SECRET` | — | 256-bit base64 secret (required) |
| `JWT_EXPIRATION_MS` | `86400000` | Token TTL in ms (default 24h) |
| `UPLOAD_DIR` | `uploads` | File upload directory |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP host |
| `MAIL_PORT` | `587` | SMTP port |
| `MAIL_USERNAME` | — | SMTP username |
| `MAIL_PASSWORD` | — | SMTP password / App Password |
| `MAIL_FROM` | `noreply@clientportal.com` | From address |

## Project Structure

```
src/main/java/com/PeterTomko/clientportal/
├── config/          # Security, OpenAPI config
├── controller/      # REST controllers + AdminController
├── dto/             # Request/response DTOs
├── entity/          # JPA entities (User, Project, Invoice, FileAttachment)
├── exception/       # Global exception handler
├── repository/      # Spring Data JPA repositories
├── scheduler/       # Overdue invoice scheduler
├── security/        # JWT filter, util, UserPrincipal
└── service/         # Business logic + EmailService + PdfService
```
