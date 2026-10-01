# multi-store-commerce

[Português brasileiro](README.pt-BR.md)

A full-stack portfolio project for a fictional bakery network. **Implemented: the runnable store-directory foundation.** One Spring Boot modular monolith, one Angular application, and PostgreSQL. Stores belong to one network; unrelated-business tenancy is outside this scope.

The backend migrates its schema with Flyway and exposes `GET /api/v1/stores`: active stores ordered by unique slug, explicit `{id, slug, name}` DTOs, and `[]` when empty. Angular includes loading, empty, success, error and retry states. Security denies unmatched routes. Health probes, local OpenAPI, optional fictional seed data and automated tests are included.

## Start

Install Docker with Compose v2, then from the repository root:

```sh
cp -n .env.example .env
# Edit .env: example credentials are for local development only.
docker compose up --build
```

Do not overwrite an existing `.env`. First builds download dependencies. The three services become ready in order: PostgreSQL, backend, frontend.

- Application: http://localhost:4200/stores
- API: http://localhost:8080/api/v1/stores
- Swagger UI (dev): http://localhost:8080/swagger-ui/index.html
- OpenAPI (dev): http://localhost:8080/v3/api-docs
- Probes: http://localhost:8080/actuator/health/liveness and http://localhost:8080/actuator/health/readiness

Host ports are configurable; internal ports remain 4200, 8080 and 5432. PostgreSQL is configured at `localhost:${POSTGRES_HOST_PORT:-5432}` for [DBeaver access](docs/en/docker.md#dbeaver-connection).

## Build and test

With JDK 21 and Node.js 22.23.3 installed:

```sh
(cd backend && ./mvnw verify)
(cd backend && ./mvnw verify -Pintegration) # Requires Docker; real PostgreSQL
(cd frontend && npm ci && npm run build && npm test)
docker compose config --quiet
```

See [setup and testing](docs/en/setup.md), [Docker and configuration](docs/en/docker.md), [architecture](docs/en/architecture.md), [version sources](docs/en/versions.md), and [verification results](docs/en/verification.md).

## Stack

| Component | Version |
| --- | --- |
| Java / Eclipse Temurin | 21 LTS / 21.0.12.1+1 |
| Spring Boot | 3.5.16 |
| Maven / Wrapper | 3.9.11 / 3.3.4 |
| springdoc OpenAPI | 2.8.17 |
| Angular / CLI & build | 21.2.25 / 21.2.24 |
| Node.js | 22.23.3 LTS |
| TypeScript / RxJS | 5.9.3 / 7.8.2 |
| PostgreSQL | 17.11 (Debian Bookworm) |

Spring Boot manages Hibernate, JDBC, Flyway (including PostgreSQL support), JUnit, Mockito and Testcontainers versions. Application dependencies are pinned through the parent POM and npm lockfile; application base images and PostgreSQL use immutable digests.

## Organization

```text
backend/             Maven Wrapper, Spring application, migrations, tests, Dockerfile
frontend/            Angular standalone application, tests, npm lock, Dockerfile
docs/en/             English guides and preserved roadmap
docs/pt-BR/          Brazilian Portuguese guides and preserved roadmap
docs/adr/            Accepted architecture decisions
docker-compose.yml   Three development services
.env.example         Documented local configuration
```

`infrastructure/` is intentionally deferred until supporting configuration has a concrete responsibility. Current infrastructure lives in Compose and application Dockerfiles.

## Planned work and limits

Identity/access, store-scoped authorization, customers, catalog, inventory, cart, checkout, orders, Stripe test card payments/webhooks, delivery, notifications, reporting and administration remain **planned**. There are no placeholder endpoints or fake users. RabbitMQ, Redis and WebSockets are future candidates without dependencies or services. See the [preserved domain roadmap](docs/en/roadmap.md).

This increment has no write API, pagination, authentication flow, production frontend server or production deployment claim. The frontend uses the development server; backend changes require a rebuild. Data remains in `.dockerized-postgres` after containers are removed. Disabling seed data does not delete previously inserted rows.

**Recommended next increment:** a read-only catalog with shared products and explicit store-specific availability, migrations and integration tests. Design authentication and store-scoped authorization before adding administrative writes.

## Author and license

Júnio Rosa · [LinkedIn](https://www.linkedin.com/in/j%C3%BAnio-rosa-94b5731b2/)

[MIT License](LICENSE)
