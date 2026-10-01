# Architecture

[Português](../pt-BR/architecture.md) · [README](../../README.md)

The browser requests `/api/v1/stores` from the Angular origin. The Angular development server proxies `/api/**` to Spring Boot. Boot owns validation, transactions, security and PostgreSQL access. No browser code uses Docker DNS names.

One deployable backend is organized by business capability under `com.multistorecommerce`. The implemented `store` module has `api` (HTTP controller), `application` (transactional service, summary DTO and dev seeding), `domain` (JPA store mapping, no HTTP dependencies), and `infrastructure` (Spring Data repository). Cross-cutting security and error handling live in `configuration`. No generic CRUD framework or empty future module trees are introduced.

The `stores` table has a UUID primary key, unique lowercase hyphenated slug (80 characters), nonblank name (160 characters), and non-null active flag. The read query filters active rows and sorts by unique slug for deterministic ordering. Flyway creates schema; Hibernate only validates it. SQL migrations are immutable after application. Seed SQL is outside Flyway locations, runs only with `dev` plus `DEV_SEED_ENABLED=true`, and inserts fixed fictional UUIDs idempotently without overwriting existing rows. Disabling it never deletes data.

Security is stateless, with no login, generated users or tokens. Only GET stores and minimal health probes are public; OpenAPI is enabled and permitted only in `dev`. Unmatched routes and write methods return 403. CSRF is disabled for this read-only API; revisit it before introducing cookie authentication or mutations. CORS permits explicit configured origins without credentials. API failures use ProblemDetail; unexpected details are logged server-side but replaced with a generic message in responses. Liveness checks application lifecycle; readiness also checks the database. Health details and other actuator endpoints are not exposed.

Angular uses standalone components, routing, HttpClient, signals and a small typed service. A discriminated state represents loading, failure and results. There is no global state library or UI framework. Requests are cancelled when the component is destroyed. The UI is currently English; project documentation is bilingual.

Planned modules: identity/access, catalog, inventory, cart, order, payment and delivery. Notifications and reporting may follow. The store module is the only implemented business capability. Authentication, inventory reservation and payment consistency strategies remain undesigned; see [roadmap](roadmap.md). There is no separate-business tenancy.

Accepted decisions: [modular monolith](../adr/0001-modular-monolith.md), [monorepo](../adr/0002-monorepo.md), [database](../adr/0003-postgresql-flyway.md), [local infrastructure](../adr/0004-compose-persistence.md).
