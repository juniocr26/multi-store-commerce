# Setup and testing

[Português](../pt-BR/setup.md) · [Docker workflow](docker.md)

Docker-only startup does not require host Java or Node. Direct development requires JDK 21 (`JAVA_HOME`), Node 22.23.3 (`frontend/.nvmrc`), npm, and PostgreSQL 17. The Maven Wrapper downloads Maven 3.9.11 on first use. On Windows use `mvnw.cmd` and equivalent shell environment syntax.

## Direct application launch

Compose intentionally does not publish PostgreSQL. Use a separately configured local PostgreSQL database for this mode. Create its database/user first, then export real local values. Compose `.env` does not automatically configure direct processes.

```sh
# PostgreSQL 17 accessible at localhost:5432, with an existing commerce database/user.
export APP_ENV=dev
export DB_URL=jdbc:postgresql://localhost:5432/commerce
export POSTGRES_USER=commerce
export POSTGRES_PASSWORD='your-local-development-password'
export ALLOWED_ORIGINS=http://localhost:4200
export DEV_SEED_ENABLED=true
cd backend
./mvnw spring-boot:run
```

```sh
# Another terminal, from the repository root:
cd frontend
npm ci
npm start
# If backend runs on a different host port:
API_PROXY_TARGET=http://localhost:8081 npm start
```

`DB_URL` defines the direct database name; `POSTGRES_DB` is an initialization variable for the container only. Direct backend listens on 8080; Spring's standard `SERVER_PORT` can change it. `BACKEND_HOST_PORT` only affects Compose publishing. Browser requests stay relative; proxy target is used by the Node server.

## Verification

```sh
(cd backend && ./mvnw verify)
(cd backend && ./mvnw verify -Pintegration)
(cd frontend && npm ci && npm run build && npm test)
docker compose config --quiet
docker compose up --build -d --wait
curl -f http://localhost:8080/actuator/health/readiness
curl -f http://localhost:4200/api/v1/stores
curl -f http://localhost:8080/api/v1/stores > /tmp/stores-before.json
docker compose restart postgres backend
docker compose up -d --wait
curl -f http://localhost:8080/api/v1/stores > /tmp/stores-after.json
diff /tmp/stores-before.json /tmp/stores-after.json
```

The regular Maven build runs five MVC tests: DTO contract, empty list, security denials, explicit CORS and sanitized error response. The integration profile additionally runs five Testcontainers tests against real PostgreSQL: Flyway, filtering/order through HTTP, empty results, database constraints, repeatable seed, health and local OpenAPI. Integration fails rather than silently skipping if Docker is unavailable. Disposable test data is separate from `.dockerized-postgres`.

Frontend tests exercise loading → success, empty results, and error → retry → success using the real typed service with an HTTP test backend. Production build checks Angular templates and TypeScript. jsdom verifies DOM behavior, not browser layout or live reload; manually open the application for visual checks.

For readiness failure checking in this local-only stack, stop PostgreSQL, request both probes, then start it again. Liveness should stay UP; readiness becomes DOWN (503) after the datasource timeout. Do not use this disruption check against shared environments.

Backend JAR: `backend/target/backend-0.0.1-SNAPSHOT.jar`. Angular output: `frontend/dist/commerce/browser`. Frontend live reload watches `src`; Java changes require `docker compose up -d --build backend`. See [recorded verification](verification.md).
