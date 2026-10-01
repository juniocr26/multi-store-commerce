# Verification record

[Português](../pt-BR/verification.md)

Executed on 2026-10-01 on macOS arm64 with Docker Desktop / Engine 29.5.3, temporary Temurin 21.0.12.1+1 and Node 22.23.3 toolchains. The host's preinstalled Java 17 was not used to compile this Java 21 project. Docker Desktop was initially stopped and was started for validation.

| Check | Result |
| --- | --- |
| `./mvnw verify -Pintegration` | PASS: backend compilation/package, 5 MVC tests and 5 PostgreSQL integration tests; no skips |
| `npm ci` | PASS with bundled npm 10.9.9 and committed lockfile |
| `npm run build` | PASS: production Angular bundle, about 231 kB raw initial output |
| `npm test` | PASS: 3 DOM tests covering loading, empty, success, error and retry |
| npm dependency audit at install | 0 reported vulnerabilities at verification time |
| `docker compose --env-file .env.example config --quiet` | PASS; exactly frontend/backend/postgres, internal database bridge and loopback application ports |
| `docker compose --env-file .env.example up --build -d --wait` | PASS: both application images built, all 3 services healthy |
| Host API and frontend `/api/v1/stores` proxy | PASS: same 2 active fictional stores in slug order |
| Chromium desktop (1280×800) and mobile (390×844) | PASS: rendered stores, no horizontal overflow, no uncaught application errors; screenshots visually inspected |
| Browser state checks | PASS: delayed request shows loading; mocked empty/error responses render correctly; retry returns to real API success |
| Angular source live reload | PASS: temporary heading edit appeared automatically; original source restored and observed |
| Database outage probes | PASS: stopping postgres leaves liveness 200/UP and makes readiness 503/DOWN; database then restored |
| Non-destructive postgres/backend restart | PASS: readiness recovers and store IDs/content remain identical |
| Runtime users | PASS: backend UID 10001; frontend UID 1000 |

The initial test runs found and resolved Testcontainers tag-plus-digest parsing, an npm 10 dependency-resolution crash, and Docker Engine host publishing for internal-only networks. Tests were rerun after corrections. The Maven distribution has a SHA-256 check; application base images and PostgreSQL have immutable digests.

The Compose run used `--env-file .env.example` to avoid creating or overwriting a personal `.env`. It created only this project's development containers and `.dockerized-postgres` data. No destructive database reset, commit, push or deployment was performed. Services were left running for inspection.

Browser and outage/restart checks were ad hoc validation using isolated tools in temporary storage; the committed regression suites are Maven/JUnit and Angular/Vitest. These temporary tools are not application dependencies. Linux/Windows host setup, alternate CPU architectures, production deployment, load/availability and a formal accessibility audit were not tested. There are no remaining blocked checks for the requested local skeleton.
