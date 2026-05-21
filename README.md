# E-Commerce Backend

A production-grade e-commerce REST API built with Spring Boot 3.3.x and Java 21.

## Tech Stack

- **Java 21** — Latest LTS
- **Spring Boot 3.3.12** — Web, Data JPA, Security, Validation, AOP
- **PostgreSQL** — Primary database
- **JWT (jjwt 0.11.5)** — Stateless authentication
- **SpringDoc OpenAPI 2.6.0** — Swagger UI
- **Lombok** — Boilerplate reduction
- **Maven** — Build tool

## Prerequisites

- Java 21+
- PostgreSQL running on `localhost:5432`
- Maven (or use the bundled `mvnw.cmd`)

## Setup

### 1. Create the database

```sql
CREATE DATABASE ecommerce_db;
```

### 2. Configure environment (optional)

Copy `.env.example` to `.env` and customize if needed:

```bash
JWT_SECRET=VGhpc0lzQVN1ZmZpY2llbnRseUxvbmdCYXNlNjRTZWNyZXRLZXlGb3JKV1RIUzI1Ng==
JWT_EXPIRATION=86400000
```

The app works without `.env` — defaults are in `application.yaml`.

### 3. Run the application

```bash
./mvnw.cmd spring-boot:run
```

The dev profile (`application-dev.yml`) is active by default:
- `ddl-auto: create-drop` — schema is created on startup
- `show-sql: true` with bind parameter logging
- Local PostgreSQL at `localhost:5432` with `postgres/postgres`

### 4. Admin account (seeded automatically)

| Email | Password | Role |
|---|---|---|
| admin@test.com | Admin123! | ADMIN |

Seeded on startup in dev profile. Skips if already exists.

## API Endpoints

### Authentication — `/api/auth` (public)

| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Register a new user | No |
| POST | `/api/auth/login` | Login, returns JWT token | No |

### Products — `/api/products` (GET public, mutations require ADMIN)

| Method | Path | Description | Auth |
|---|---|---|---|
| GET | `/api/products` | List products (paginated, filterable) | No |
| GET | `/api/products/{id}` | Get product by ID | No |
| POST | `/api/products` | Create a product | ADMIN |
| PUT | `/api/products/{id}` | Update a product | ADMIN |
| DELETE | `/api/products/{id}` | Delete a product | ADMIN |

**Query parameters for `GET /api/products`:**
- `page` — page number (0-based), default `0`
- `size` — page size, default `20`
- `sort` — sort field, e.g. `price,asc`
- `category` — filter by category
- `minPrice` — minimum price filter
- `maxPrice` — maximum price filter

### Orders — `/api/orders` (authenticated)

| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/api/orders` | Place an order | Any authed user |
| GET | `/api/orders/my` | List current user's orders (paginated) | Any authed user |
| GET | `/api/orders/{id}` | Get order by ID (own or ADMIN) | Any authed user |
| PATCH | `/api/orders/{id}/status` | Update order status | ADMIN |

**Order status transitions:**
```
PENDING → CONFIRMED → SHIPPED → DELIVERED
PENDING → CANCELLED
CONFIRMED → CANCELLED
```

Stock is restored when an order is cancelled.

### Users — `/api/users` (ADMIN only)

| Method | Path | Description | Auth |
|---|---|---|---|
| POST | `/api/users` | Create a user | ADMIN |
| GET | `/api/users` | List all users | ADMIN |
| GET | `/api/users/{id}` | Get user by ID | ADMIN |
| PUT | `/api/users/{id}` | Update a user | ADMIN |
| DELETE | `/api/users/{id}` | Delete a user | ADMIN |

## Authentication Flow

1. **Register** `POST /api/auth/register` with `{ "email": "...", "password": "..." }`
2. **Login** `POST /api/auth/login` with same payload → receive JWT token
3. Include token in subsequent requests: `Authorization: Bearer <token>`

## Postman Testing

Two files are in the project root:

- **`ecommerce-api.postman_collection.json`** — Full test collection (70 tests)
- **`ecommerce-local.postman_environment.json`** — Local environment variables

**Run from Collection Runner** (not individual requests) to maintain token persistence via pre-request scripts.

## Swagger UI

Available at `http://localhost:8080/swagger-ui.html` when the app is running.

Click **Authorize** and paste your Bearer token to test secured endpoints.

## Project Structure

```
src/main/java/com/aghayev/ecommerce/
├── config/          — Security, JWT filter, Swagger, AOP, data initializer
├── controller/      — REST controllers
├── dto/             — Request/response DTOs + ApiResponse wrapper
├── entity/          — JPA entities
├── exception/       — Custom exceptions + global handler
├── mapper/          — Entity ↔ DTO mappers
├── repository/      — Spring Data JPA repositories
├── service/         — Business logic
└── specification/   — JPA Specification for dynamic filtering
```

## Response Format

All endpoints return a unified `ApiResponse<T>`:

```json
{
  "success": true,
  "data": { ... },
  "message": "Operation completed",
  "timestamp": "2026-05-21T12:00:00"
}
```

Error responses include `fieldErrors` for validation failures.
