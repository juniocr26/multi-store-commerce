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
| `POSTGRES_DATA_SOURCE` | `./.dockerized-postgres` retains existing data; `postgres_data` selects a project-scoped named volume for fresh clones. |
| `MAVEN_CACHE_ID` | `commerce-maven`; optional BuildKit cache namespace. |
| `POSTGRES_DB` | `commerce`; initial database name. |
| `POSTGRES_USER` | `commerce`; local database owner. |
| `POSTGRES_PASSWORD` | Required; example `commerce-local-only` is public and for local development only. |
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
| Database | `POSTGRES_DATA_SOURCE` | `./.dockerized-postgres` retains existing data; `postgres_data` selects a project-scoped named volume for fresh clones. |
| `MAVEN_CACHE_ID` | `commerce-maven`; optional BuildKit cache namespace. |
| `POSTGRES_DB` |
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

## Reproducible builds and isolated verification

Only Docker with Compose v2+ and BuildKit is required for this workflow; Java,
Maven, Node and npm run inside images. On Windows use Linux containers and
`Copy-Item .env.example .env` only if `.env` does not already exist. The shell
examples below use POSIX syntax (Git Bash/WSL on Windows). Do not replace existing
credentials. The example password is a public local development value.

Maven Wrapper 3.3.4 substitutes a tar.gz download when `unzip` is missing, while
retaining the configured ZIP SHA-256. The JDK build stage installs `unzip` so
it downloads exactly the pinned ZIP. The configured SHA-256 is unchanged:
`0d7125e8c91097b36edb990ea5934e6c68b4440eef4ea96510a0f6815e7eeadb`.
It was calculated from the ZIP after matching the official Maven Central
[SHA-512 sidecar](https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.zip.sha512).
Never remove checksum verification. LF attributes and build-time normalization
protect the wrapper from Windows checkout line endings; executable permission
is set inside the image.

Maven's BuildKit cache is optional: `MAVEN_CACHE_ID` chooses its namespace.
A cache miss downloads the wrapper and dependencies again. `--no-cache` alone
does not empty cache mounts: use a new cache ID when testing cold builds.
Frontend dependencies come from `npm ci` and the committed lockfile inside
the image. There is no node_modules dependency volume to become empty or stale.
Only source is bound into the frontend; host node_modules and compiled outputs
are excluded. Rebuild frontend after editing package.json **and** package-lock.json,
Angular configuration or the proxy. A mismatched lockfile intentionally fails.
An old manually added `/app` or `/app/node_modules` mount must be removed from
local Compose overrides because it would hide image dependencies.

`POSTGRES_DATA_SOURCE` defaults to `./.dockerized-postgres`, retaining existing
local databases. For a **fresh** clone, set `POSTGRES_DATA_SOURCE=postgres_data`
in `.env` to use a Docker-managed volume named `<project>_postgres_data`, avoiding
host filesystem ownership issues. Compose creates it automatically. Switching
between bind and named storage selects a different database; it does not migrate
existing data. Keep the current source for an existing database, or back up and
restore with PostgreSQL tools before switching. Keep its original credentials.

```sh
# Normal lifecycle; database retained
docker compose up -d --build --wait --wait-timeout 180
docker compose stop
docker compose up -d --wait
docker compose up -d --build --wait
docker compose down
# HTTP checks (default host ports)
curl --fail http://localhost:8080/actuator/health/readiness
curl --fail http://localhost:8080/api/v1/stores
curl --fail http://localhost:4200/stores
curl --fail http://localhost:4200/api/v1/stores
# Frontend build and existing tests inside Docker
docker compose run --rm --no-deps frontend sh -c 'npm run build && npm test'
# Backend unit tests inside the JDK build stage
docker build --target build -t commerce-backend-check ./backend
docker run --rm commerce-backend-check ./mvnw -B -ntp verify
```

For isolated checks, copy/clone tracked sources into a temporary directory, copy
`.env.example` to a test env file there and set `POSTGRES_DATA_SOURCE=postgres_data`,
unused host ports and a unique `MAVEN_CACHE_ID`. Use the same explicit project name
and env file for **every** command:

```sh
docker compose -p commerce-check --env-file test.env config --quiet
docker compose -p commerce-check --env-file test.env build --no-cache
docker compose -p commerce-check --env-file test.env up -d --wait --wait-timeout 180
docker compose -p commerce-check --env-file test.env ps
docker compose -p commerce-check --env-file test.env restart
docker compose -p commerce-check --env-file test.env up -d --build --wait
docker compose -p commerce-check --env-file test.env down
```

**Project-scoped destructive reset:** for named storage, first inspect
`docker compose config --volumes` and `docker volume inspect <project>_postgres_data`.
`docker compose down --volumes` deletes this project's named database volume and
all its records; the next startup initializes PostgreSQL and Flyway again. For
bind storage it does not delete data: the existing `rm -rf ./.dockerized-postgres`
procedure above remains destructive. Back up first. Never run global prune.

Pinned base-image digests are multi-platform indexes containing Linux ARM64 and
AMD64; no platform is forced. Execution verification on other architectures and
Windows/Linux hosts remains separate from inspecting those manifests.
