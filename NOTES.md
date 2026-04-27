# E-Commerce Spring Boot Project

> This file is the single source of truth for our project.
> When starting a new conversation, paste this file and say "continue from where we left off".
> Update the CURRENT STATUS section at the end of every coding session.

---

## Project Overview

A production-grade e-commerce backend built as a monolith first, then progressively enhanced
with caching, messaging, testing, and finally split into microservices.
The goal is both a strong portfolio project and deep understanding of each concept.

**Tech Stack**
- Java 21
- Spring Boot 3.3.x
- Maven
- PostgreSQL
- Base package: `com.aghayev.ecommerce`

---

## Non-Negotiable Rules

These apply to every line of code in this project. If you are reviewing code, check these first.

- Never expose JPA entities directly from controllers — always use DTOs
- Every controller method returns `ResponseEntity<ApiResponse<T>>` — no exceptions
- `BigDecimal` for all money/price fields — never `Double` or `Float`
- Snapshot `unitPrice` in `OrderItem` at order time — never reference live product price
- Constructor injection only — never `@Autowired` on fields
- No hardcoded secrets anywhere — use `${ENV_VAR}` placeholders in config
- `application-dev.yml` is committed (localhost credentials, harmless)
- `.env` and `application-prod.yml` are never committed (real secrets)
- Every session ends with a meaningful conventional commit: `feat:`, `fix:`, `refactor:`, `docs:`

---

## Project Configuration

- Base config in `application.yml` — shared by all profiles, no secrets
- Dev config in `application-dev.yml` — localhost PostgreSQL, show-sql enabled, DEBUG logging
- No prod config yet — created only when deploying to a real server
- `.env.example` committed with variable names but no real values
- `.gitignore` blocks `.env` and `application-prod.yml`

---

## Package Structure

```
src/main/java/com/aghayev/ecommerce/
├── config/
├── controller/
├── dto/
├── entity/
├── exception/
├── repository/
└── service/
```

---

## API Response Standard

Every endpoint — success and error — returns a unified `ApiResponse<T>` wrapper with fields:
success, data, message, timestamp. Error responses also include field-level validation errors where applicable.

---

## Entities Overview

### User
- id (UUID), email (unique + indexed), password (BCrypt hashed), role (enum: USER/ADMIN), createdAt, updatedAt

### Product
- id (UUID), name, description, price (BigDecimal — never Double), stockQuantity, category (indexed), createdAt, updatedAt

### Order
- id (UUID), user (ManyToOne LAZY), status (enum: PENDING/CONFIRMED/SHIPPED/DELIVERED/CANCELLED), totalAmount (BigDecimal), createdAt

### OrderItem
- id (UUID), order (ManyToOne), product (ManyToOne), quantity, unitPrice (BigDecimal — snapshotted at order time, never a live reference)

---

## Phase Overview

---

### Phase 1 — Monolith (REST + JPA + Security + Exception Handling)

Build a fully working monolith. Every step must be complete before moving to the next. This is the foundation everything else builds on.

**Step 1 — Project Setup**
Create the Spring Boot project with correct dependencies, full package structure, application profiles, and Git repository. Nothing gets built until the project starts clean.

**Step 2 — User Module**
User entity with UUID, email, hashed password, role enum, and timestamps. Repository with findByEmail. Separate request and response DTOs — password never in the response. CRUD endpoints (no register yet, that comes with auth).

**Step 3 — Product Module**
Product entity with BigDecimal price and database indexes on category and price. Paginated list endpoint. Bean validation on request DTO so invalid data never reaches the service layer.

**Step 4 — Order Module**
Order and OrderItem entities with proper JPA relationships. The placeOrder() method is the core business logic: validate stock, deduct quantity, snapshot the price, calculate total, and save everything atomically in one transaction.

**Step 5 — Exception Handling**
ApiResponse wrapper applied to every endpoint. Custom exceptions for each business rule violation. GlobalExceptionHandler mapping every exception type to the correct HTTP status with a consistent response format.

**Step 6 — Logging + AOP**
Structured logging across all service methods with correct log levels. Custom @LogExecutionTime annotation backed by an AOP aspect that measures method execution time. Separate logging configuration per profile.

**Step 7 — JWT Authentication**
JwtService for token generation and validation. UserDetailsService backed by the user repository. Register and login endpoints. JwtAuthenticationFilter that validates the Bearer token and sets the SecurityContext on every incoming request.

**Step 8 — Security Config + Role-Based Access**
SecurityFilterChain with public and protected route rules. Role-based @PreAuthorize on sensitive endpoints. 401 and 403 responses must return ApiResponse format — not Spring's default error page.

**Step 9 — Pagination, Filtering, Swagger**
Pageable on list endpoints with a PageResponse wrapper. Product filtering by category and price range using JPA Specification. Swagger UI configured to send Bearer tokens for testing secured endpoints.

**Step 10 — Order Status State Machine**
Strict valid transition rules enforced in the service layer. Stock restored on cancellation. Ownership check so users can only touch their own orders. PATCH status endpoint restricted to ADMIN.

**Step 11 — Configuration Management**
@ConfigurationProperties class replacing all scattered @Value annotations. Full audit of any remaining hardcoded values. .env.example committed with all required variable names.

**Step 12 — Phase 1 Review**
Full end-to-end Postman test covering every flow and every error path. N+1 query audit with show-sql enabled. README written with local setup instructions and endpoint list.

---

### Phase 2 — Consuming APIs + Redis Caching

Build on the working monolith without introducing new services yet. The goal is to learn inter-module REST calls and caching in isolation before the complexity of microservices.

**Step 1 — WebClient Setup**
Configure a WebClient bean. The order module calls the product module via WebClient to verify stock — this mirrors the real inter-service call that will exist in Phase 5. Errors handled gracefully as business exceptions.

**Step 2 — Redis Integration**
Cache the product catalog using the cache-aside pattern with TTL. Invalidate the cache whenever a product is created, updated, or deleted. The goal is to understand caching behavior and invalidation strategy before adding more moving parts.

**Step 3 — Review**
Verify cache hits and misses via logs. Confirm invalidation works correctly after a product update.

---

### Phase 3 — Testing

Add a proper test suite to the existing monolith. No new features — tests only. The goal is to understand what good tests look like before the codebase gets more complex.

**Step 1 — Unit Tests**
JUnit and Mockito for every service class. All dependencies mocked. Happy path and every exception path covered.

**Step 2 — Integration Tests**
Testcontainers for real PostgreSQL and Redis — not H2 in-memory. Full request/response cycle tested including authentication headers.

**Step 3 — Review**
Every custom exception path has a test. Security rules verified: unauthenticated access, wrong role, accessing another user's resource.

---

### Phase 4 — RabbitMQ + MongoDB + Docker

Introduce async messaging, a second database, and containerization. Still one application — the goal is to learn each technology in a familiar codebase before the microservices split.

**Step 1 — RabbitMQ Setup**
Add RabbitMQ and define exchanges, queues, and routing keys as constants before writing any producers or consumers.

**Step 2 — Order Events**
Publish order.created and order.cancelled events as plain JSON POJOs after the relevant service operations.

**Step 3 — Consumers**
Payment consumer processes order.created and publishes payment.completed. Notification consumer listens to both events and logs what a real notification would contain.

**Step 4 — Reliability**
Dead letter queue for messages that exhaust retries. Exponential backoff on consumer failure. Idempotent consumers that handle duplicate delivery safely. This is the part most tutorials skip — it is not optional here.

**Step 5 — MongoDB**
Order audit trail written to MongoDB on every status change. User activity log for logins and order placements. This demonstrates choosing the right database for the right data shape.

**Step 6 — Docker**
Dockerfile for the application. docker-compose.yml with all infrastructure services and health checks. The full system starts with a single command.

**Step 7 — Review**
Full async flow tested end-to-end. Consumer failure simulated to verify the dead letter queue works.

---

### Phase 5 — Microservices Split + Elasticsearch

Split the monolith into independent services. This is done last because the monolith must be solid, tested, and well-understood before splitting. Splitting a broken monolith produces a broken distributed system.

**Step 1 — Plan Service Boundaries**
Write down service ownership before touching any code. Each service owns its data and no other service reads its database directly. This decision is made on paper first.

**Step 2 — Extract user-service**
Standalone Spring Boot application with its own PostgreSQL schema. Owns all authentication and user management.

**Step 3 — Extract product-service**
Standalone Spring Boot application with its own PostgreSQL schema. Publishes inventory events when stock changes.

**Step 4 — Extract order-service**
Standalone Spring Boot application with its own schema. Calls user-service and product-service via WebClient. Publishes order events to RabbitMQ.

**Step 5 — Extract payment-service and notification-service**
Both are purely event-driven — they consume from RabbitMQ only, no synchronous REST calls in. payment-service publishes payment.completed after processing.

**Step 6 — Elasticsearch**
Added to docker-compose. product-service syncs product data to an Elasticsearch index via events. New search endpoint with full-text search and category and price range filters.

**Step 7 — Portfolio Polish**
ARCHITECTURE.md with a full system diagram. README per service. Single docker-compose at root starts the entire system. Swagger documented for every service.

---

## Full Progress Checklist

---

### Phase 1 — Monolith (REST + JPA + Security + Exception Handling)

- [x] Step 1 — Project Setup
    - [x] Create Spring Boot project with correct dependencies
    - [x] Create full package structure
    - [x] Configure application.yml and application-dev.yml
    - [x] Init Git repo, push to GitHub, write .gitignore

- [x] Step 2 — User Module
    - [x] User entity with all fields, UUID primary key, timestamps
    - [x] UserRepository with findByEmail() method
    - [x] UserRequestDto and UserResponseDto (no password in response)
    - [x] UserService and UserController with CRUD endpoints

- [x] Step 3 — Product Module
    - [x] Product entity with BigDecimal price and database indexes
    - [x] ProductRepository with findByCategory() query
    - [x] ProductService and ProductController with paginated list
    - [x] ProductRequestDto with @Valid bean validation

- [x] Step 4 — Order Module
    - [x] Order entity with ManyToOne LAZY to User and status enum
    - [x] OrderItem entity with snapshotted unitPrice and CascadeType.ALL
    - [x] placeOrder() business logic with @Transactional
    - [x] OrderController with place, get by id, get my orders endpoints

- [x] Step 5 — Exception Handling
    - [x] ApiResponse<T> wrapper applied to all endpoints
    - [x] Custom exceptions: ResourceNotFoundException, BadRequestException, InsufficientStockException
    - [x] GlobalExceptionHandler with @RestControllerAdvice
    - [x] All existing controllers updated to return ApiResponse<T>

- [x] Step 6 — Logging + AOP
    - [x] Structured logging across all service methods with correct log levels
    - [x] Custom @LogExecutionTime annotation with @Around AOP aspect
    - [x] Separate logging config per profile (dev vs prod)

- [ ] Step 7 — JWT Authentication
    - [ ] JwtService with generate, validate, extract username
    - [ ] UserDetailsService backed by UserRepository
    - [ ] AuthController with register and login endpoints
    - [ ] JwtAuthenticationFilter extending OncePerRequestFilter

- [ ] Step 8 — Security Config + Role-Based Access
    - [ ] SecurityFilterChain with public and protected route rules
    - [ ] JwtAuthenticationFilter registered in filter chain
    - [ ] @PreAuthorize on sensitive endpoints by role
    - [ ] 401 and 403 return ApiResponse format

- [ ] Step 9 — Pagination, Filtering, Swagger
    - [ ] Pageable on product list and order list endpoints
    - [ ] PageResponse wrapper with metadata
    - [ ] Product filtering by category, minPrice, maxPrice
    - [ ] springdoc-openapi with Bearer token support in Swagger UI

- [ ] Step 10 — Order Status State Machine
    - [ ] Valid transition enforcement with exception on invalid moves
    - [ ] Stock restored on CANCELLED
    - [ ] PATCH status endpoint for ADMIN only
    - [ ] Ownership check: users can only touch their own orders

- [ ] Step 11 — Configuration Management
    - [ ] @ConfigurationProperties class replacing all @Value annotations
    - [ ] All hardcoded values moved to config
    - [ ] .env.example written and committed

- [ ] Step 12 — Phase 1 Review
    - [ ] Full end-to-end Postman test of all flows
    - [ ] All error paths tested
    - [ ] N+1 query audit and fixes
    - [ ] README written with setup and endpoint list

---

### Phase 2 — Consuming APIs + Redis Caching

- [ ] Step 1 — WebClient Setup
    - [ ] WebClient bean configured
    - [ ] Order module calls product module via WebClient for stock check
    - [ ] WebClient errors surfaced as business exceptions

- [ ] Step 2 — Redis Integration
    - [ ] Redis dependency and connection configured
    - [ ] Product list and individual lookups cached with TTL
    - [ ] Cache invalidated on product create, update, delete

- [ ] Step 3 — Review
    - [ ] Cache hit/miss verified via logs
    - [ ] Invalidation verified after product update

---

### Phase 3 — Testing

- [ ] Step 1 — Unit Tests
    - [ ] JUnit + Mockito for all service classes
    - [ ] Happy path and all exception paths covered

- [ ] Step 2 — Integration Tests
    - [ ] Testcontainers for PostgreSQL and Redis
    - [ ] @SpringBootTest controller tests with auth headers

- [ ] Step 3 — Review
    - [ ] Every custom exception path has a test
    - [ ] Security rules tested: unauthenticated, wrong role, wrong user's resource

---

### Phase 4 — RabbitMQ + MongoDB + Docker

- [ ] Step 1 — RabbitMQ Setup
    - [ ] RabbitMQ dependency and connection configured
    - [ ] Exchanges, queues, routing keys defined

- [ ] Step 2 — Order Events
    - [ ] order.created event published on successful order
    - [ ] order.cancelled event published on cancellation
    - [ ] Events are plain POJOs serialized as JSON

- [ ] Step 3 — Consumers
    - [ ] Payment consumer listening to order.created
    - [ ] Notification consumer listening to order.created and payment.completed

- [ ] Step 4 — Reliability
    - [ ] Dead letter queue for exhausted retries
    - [ ] Retry with exponential backoff
    - [ ] Idempotent consumer handling for duplicate messages

- [ ] Step 5 — MongoDB
    - [ ] MongoDB dependency and connection configured
    - [ ] Order audit log written on every status change
    - [ ] User activity log for logins and order placements

- [ ] Step 6 — Docker
    - [ ] Dockerfile for the application
    - [ ] docker-compose.yml with all infrastructure and health checks
    - [ ] Full system starts with single docker-compose up command

- [ ] Step 7 — Review
    - [ ] Full async flow tested end-to-end
    - [ ] Dead letter queue verified under simulated failure

---

### Phase 5 — Microservices Split + Elasticsearch

- [ ] Step 1 — Plan Service Boundaries
    - [ ] Service ownership documented before any code split
    - [ ] No shared database rule confirmed per service

- [ ] Step 2 — Extract user-service
    - [ ] Standalone Spring Boot app with own PostgreSQL schema
    - [ ] Owns all auth and user management endpoints

- [ ] Step 3 — Extract product-service
    - [ ] Standalone Spring Boot app with own PostgreSQL schema
    - [ ] Publishes inventory events on stock changes

- [ ] Step 4 — Extract order-service
    - [ ] Standalone Spring Boot app with own PostgreSQL schema
    - [ ] Calls user-service and product-service via WebClient
    - [ ] Publishes order events to RabbitMQ

- [ ] Step 5 — Extract payment-service and notification-service
    - [ ] Both purely event-driven — consume RabbitMQ only
    - [ ] payment-service publishes payment.completed after processing

- [ ] Step 6 — Elasticsearch
    - [ ] Elasticsearch added to docker-compose
    - [ ] product-service syncs data to Elasticsearch via events
    - [ ] Full-text search endpoint with category and price filters

- [ ] Step 7 — Portfolio Polish
    - [ ] ARCHITECTURE.md with full system diagram
    - [ ] README per service
    - [ ] Single docker-compose at root starts entire system
    - [ ] Swagger documented for each service

---

## Current Status

**Current Phase:** Phase 1 — Monolith

**Last completed:** Phase 1 Step 6 — Structured logging, @LogExecutionTime, profile-based logging config

**Next task:** Phase 1 Step 7 — JWT Authentication

**Blockers or notes:**
- Logging uses explicit business logs in services and AOP only for execution-time measurement
- Shared logging defaults are in `application.yaml`; profile-specific levels are in `application-dev.yml` and `application-prod.yml`

