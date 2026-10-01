# Docker and configuration

[Português](../pt-BR/docker.md) · [README](../../README.md)

Use Docker Compose v2 with BuildKit. Copy `.env.example` without replacing local configuration, edit it, then run:

```sh
cp -n .env.example .env
docker compose config --quiet
docker compose up --build
# Or detached, waiting for health:
docker compose up --build -d --wait
docker compose ps
docker compose logs -f backend
```

Exactly three services share an internal bridge: frontend, backend and postgres. Frontend, backend and PostgreSQL ports are configured for publication on loopback. A second bridge, `web`, connects only the applications and enables host port publishing: During validation with Docker Engine 29.5.3, ports were not published on containers attached exclusively to an internal network. PostgreSQL also joins a dedicated non-internal bridge, `db_access`, which enables its loopback host binding on Docker Engine 29.5.3. Only PostgreSQL joins `db_access`; `commerce` remains internal and backend connections still use `postgres:5432`. The database health check uses container-expanded `$$POSTGRES_USER` and `$$POSTGRES_DB`. Backend waits for the database; frontend waits for backend readiness. Restart policy is `unless-stopped`. No external runtime services are needed.

Backend is a multi-stage JDK/Maven Wrapper build and a non-root JRE runtime. Its health check uses Bash `/dev/tcp`, `head` and `grep`, present in the pinned Ubuntu image. There is no source bind over the packaged JAR. Frontend uses non-root Node and `npm ci`. Only `frontend/src` is mounted read-only, keeping Linux dependencies inside the image; Angular polls for live reload. Changes to dependencies, proxy or Angular configuration require a rebuild.

```sh
docker compose up -d --build backend # Java changes; no backend hot reload
docker compose up -d --build frontend # Dependency/config changes
docker compose restart postgres backend # Non-destructive restart
docker compose down # Stop/remove containers and network; keep database files
```

## Environment contract

| Variable | Default / role |
| --- | --- |
| `APP_ENV` | Compose `dev`; direct launch `default`. Only `dev` enables local OpenAPI and permits seed. |
| `FRONTEND_HOST_PORT` | `4200`; host only. |
| `BACKEND_HOST_PORT` | `8080`; host only. |
| `POSTGRES_HOST_PORT` | `5432`; PostgreSQL host port, bound to `127.0.0.1`. |
| `POSTGRES_DB` | `commerce`; initial database name. |
| `POSTGRES_USER` | `commerce`; local database owner. |
| `POSTGRES_PASSWORD` | Required; example is development-only. |
| `ALLOWED_ORIGINS` | Explicit comma-separated origins; example includes localhost and 127.0.0.1:4200. |
| `DEV_SEED_ENABLED` | Default false; example true; also requires `dev`. |
| `APP_UID` / `APP_GID` | Backend image build arguments, default `10001`. |
| `DB_URL` | Direct backend launch: `jdbc:postgresql://localhost:5432/commerce`; Compose constructs it using `postgres:5432` and `POSTGRES_DB`. |
| `API_PROXY_TARGET` | Direct Angular server: `http://localhost:8080`; Compose sets `http://backend:8080`. Never bundled into browser code. |

Changing host ports never changes internal Docker addresses. Update `ALLOWED_ORIGINS` when changing browser origin. `.env` is Compose interpolation, not an automatic environment loader for Maven or npm; see [direct launch](setup.md).

## DBeaver connection

Create a PostgreSQL connection in DBeaver using the values from your existing `.env`:

| Setting | Value |
| --- | --- |
| Host | `127.0.0.1` |
| Port | `POSTGRES_HOST_PORT` (default `5432`) |
| Database | `POSTGRES_DB` |
| Username | `POSTGRES_USER` |
| Password | `POSTGRES_PASSWORD` |

If host port `5432` is already in use, set `POSTGRES_HOST_PORT=5433` in `.env` and use port `5433` in DBeaver. Apply the port mapping with `docker compose up -d --force-recreate postgres`; this retains the existing database bind mount and data. The backend continues connecting to `postgres:5432` within Docker regardless of the host port. Keep the credentials of the already initialized database; no data reset is needed.

## Schema and development data

Flyway runs schema migrations on backend startup and Hibernate validates mappings. Seed requires both `APP_ENV=dev` and `DEV_SEED_ENABLED=true`; it inserts two active fictional stores and one inactive store idempotently. Set false and recreate backend to stop future seed execution. Existing rows remain. To observe an empty database without deleting anything, use a fresh database with seed disabled (or the automated isolated empty-database test).

PostgreSQL 17 data is bind-mounted at `./.dockerized-postgres:/var/lib/postgresql/data`, excluded from Git. `docker compose down`, including `down -v`, does **not** delete bind-mounted data. Changing initial database/user/password variables does not change an already initialized database. Keep existing credentials or alter them deliberately using PostgreSQL administration.

**Permanent deletion — optional local reset only. The next command destroys every local database record. Back up anything needed first. It is not part of verification.**

```sh
docker compose down
rm -rf -- ./.dockerized-postgres
```

No Stripe/JWT secrets are needed. Do not treat the development server, local credentials or database-owner account as a production deployment configuration.

[Docker network reference](https://docs.docker.com/engine/network/) explains connecting applications to both internal and external bridges.
