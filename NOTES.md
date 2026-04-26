We are building an e-commerce Spring Boot monolith project together.
Here is the plan we agreed on:

TECH STACK: Spring Boot 3.3.x, Java 21, PostgreSQL, Maven
PACKAGE: com.aghayev.ecommerce
PROFILE SETUP: application.yml (base), application-dev.yml (committed),
no prod yet

PHASE PLAN:
Phase 1 (Weeks 1-2): Monolith - REST, JPA, Security, Exception handling
Phase 2 (Week 3):    Security upgrade + AOP + Logging
Phase 3 (Week 4):    WebClient + Redis caching
Phase 4 (Week 5):    Testing (JUnit, Mockito, Testcontainers)
Phase 5 (Week 6):    RabbitMQ + MongoDB + Docker
Phase 6 (Weeks 7-8): Microservices split + Elasticsearch

CURRENT STATUS: [you fill this in]
Day X of Phase 1 — just finished [what you built]

RULES WE AGREED ON:
- Never expose entity directly, always use DTOs
- ApiResponse<T> wrapper on every endpoint
- BigDecimal for price, never Double
- Snapshot price in OrderItem
- application-dev.yml is committed, .env and prod are not
- Every day ends with a meaningful commit