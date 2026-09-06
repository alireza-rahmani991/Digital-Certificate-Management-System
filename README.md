# Digital Certificate Management System

a spring boot REST API for managing X.509 digital certificates.

## Features

- uploaded certificates are automatically parsed and stored in a postgresql database
- metadata of the certificates are extracted and can be retrieved
- can retrieve certificates by id 
- can search and filter certificates with pagination and sorting
- can revoke certificates
- certificates validity are updated daily
- list certificates expiring within a given number of days

## Built with

- Java 21
- Spring Boot 4.1.1 (Web MVC, Data JPA, Validation)
- PostgreSQL
- Flyway (database migrations)
- springdoc-openapi (Swagger UI)
- Testcontainers + JUnit 5 for integration testing
- Docker / Docker Compose

# Getting started 

## Prerequisites

- Docker and Docker Compose (recommended, no local Java/Postgres install needed), **or**
- Java 21 and a local PostgreSQL instance if running without Docker

### 1.Clone the repository

```bash
git clone https://github.com/alireza-rahmani991/Digital-Certificate-Management-System
cd Digital-Certificate-Management-System
```

### 2. Configure environment variables

Copy the example env file and fill in your own values:

```bash
cp .env.example .env
```

`.env` is listed in `.gitignore` and is never committed — see [Environment Variables](#environment-variables) below for what each value means.

### 3. Run with Docker Compose

```bash
docker compose up --build
```

This starts a PostgreSQL container and the application container. The API will be available at `http://localhost:8080`.

### 4. Run locally without Docker (alternative)

Point `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` at a PostgreSQL instance you're running yourself, export them as environment variables, then:

```bash
./mvnw spring-boot:run
```

## Environment variables

| Variable | Description | Example |
|---|---|---|
| `DB_URL` | JDBC URL for the PostgreSQL database | `jdbc:postgresql://db:5432/digital_certificate` |
| `DB_USERNAME` | Database username | *(choose your own username)* |
| `DB_PASSWORD` | Database password | *(choose your own password)* |

> When running via `docker compose`, use `db` as the host in `DB_URL` (the service name in `docker-compose.yml`). When running locally against your own Postgres, use `localhost` or your DB host instead.

## API Documentation

Once the app is running, interactive API docs (Swagger UI) are available at:

```
http://localhost:8080/swagger-ui/index.html
```

Raw OpenAPI spec:

```
http://localhost:8080/v3/api-docs
```

## API Overview

Base path: `/api/certificates`

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/certificates` | Upload a certificate file (multipart form field: `certificate`) |
| `GET` | `/api/certificates` | Search/list certificates (paginated, filterable) |
| `GET` | `/api/certificates/{id}` | Get a certificate by ID |
| `DELETE` | `/api/certificates/{id}` | Delete a certificate |
| `PATCH` | `/api/certificates/{id}/revoke` | Revoke a certificate |
| `GET` | `/api/certificates/expiring?days={n}` | List certificates expiring within `n` days |
| `GET` | `/api/certificates/statistics` | Get aggregate certificate statistics |

## Running Tests

```bash
./mvnw test
```

Integration tests use Testcontainers, so Docker must be running locally to execute the full test suite.