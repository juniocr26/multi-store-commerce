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

## Docker repair verification — 2026-10-02

This record supplements the historical checks above. Executed on macOS ARM64,
Docker Desktop Linux/aarch64, Engine 29.5.3 and Compose 5.1.4. All application
build/test tools ran inside Docker. Sources came from `git archive HEAD` into
`/tmp/commerce-docker-validation`, with the Docker fixes copied over. The isolated
project was `commerce-validation-20261002`; ports were 18089 (backend), 14209
(frontend) and 15439 (database). No personal `.env` or existing database was used.

The original failure was reproduced in the unchanged Temurin JDK image with
`MAVEN_USER_HOME=/tmp/empty-maven`: `unzip` was absent, the wrapper downloaded
tar.gz and failed the ZIP SHA-256 comparison. The official ZIP downloaded from
Maven Central matched its official SHA-512 sidecar; its computed SHA-256 exactly
matched the existing property. No checksum or wrapper script was changed.

| Commands/checks | Result |
| --- | --- |
| `docker compose --env-file .env.example config --quiet`, `git diff --check` | PASS |
| `docker compose -p commerce-validation-20261002 --env-file test.env build --no-cache` with new `MAVEN_CACHE_ID=commerce-validation-20261002-empty` | PASS: fresh tracked sources, no host dependencies, no reusable application build steps, empty Maven cache namespace |
| `docker compose -p commerce-validation-20261002 --env-file test.env up -d --wait --wait-timeout 180` | PASS: new project networks and postgres_data volume, all three services healthy |
| SQL `SELECT version, success FROM flyway_schema_history` | PASS: V1 applied; empty stores response `[]` with seed disabled |
| HTTP readiness, API, frontend `/stores`, `/main.js`, `/styles.css`, proxied `/api/v1/stores` | PASS: HTTP 200; proxy matches backend |
| `docker run --rm commerce-validation-20261002-frontend sh -c 'npm run build && npm test'` | PASS: production bundle and 3 frontend tests |
| `docker build --target build --build-arg MAVEN_CACHE_ID=commerce-validation-20261002-empty -t commerce-validation-20261002-tests ./backend` followed by command below | PASS: 5 API tests + 5 PostgreSQL integration tests, no skips |
| Fake host node_modules, dist, backend target and .m2 poison files followed by rebuild/image checks | PASS: excluded from images |
| Added `is-number@7.0.0` in temporary frontend manifest/lock with containerized npm, rebuilt and required it inside image | PASS: `npm ci` reran and installed changed dependencies; original manifests restored afterward |
| Source edit while Angular ran | PASS: updated marker appeared in served JavaScript without rebuild; source restored. This was HTTP verification, not a browser rendering test. |
| `restart`, `down --rmi local`, `up -d --build --wait` | PASS: stored UUID/content retained after restart and removal/rebuild of isolated application images, containers and networks |
| Inspected named volume labels, `down --volumes`, rebuilt/start with original manifests and CRLF/non-executable host mvnw | PASS: empty volume recreated and Flyway V1 applied; build normalized wrapper; repeated stop/up passed |
| Default `./.dockerized-postgres` bind in temporary checkout with seed enabled | PASS: fresh initialization, 3 total stores / 2 active; all services healthy |
| Runtime `id` | PASS: backend 10001; frontend 1000 |
| `docker buildx imagetools inspect --raw` for all four pinned images | PASS: each index contains Linux AMD64 and ARM64 |

Backend integration command (Docker Desktop, isolated test image):

```sh
docker run --rm \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v commerce-validation-20261002-test-maven:/root/.m2 \
  -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal \
  commerce-validation-20261002-tests ./mvnw -B -ntp verify -Pintegration
```

No dependency volumes exist in the supported frontend configuration: absent or
stale node_modules volumes cannot affect it. BuildKit caches are optional and
cold build behavior was verified using a fresh namespace, without global prune.
Validation resources were removed using the explicit isolated project name;
existing workspace database files, containers and images were not changed.
BuildKit validation cache entries may remain as optional reusable cache.

Remaining limits: Windows/Linux hosts and AMD64 execution were not tested;
manifest inspection alone does not verify them. No browser was used during this
repair; frontend assets, proxy, DOM unit tests and source polling were checked.
Repeat the documented isolated commands on those hosts/architectures. The npm
install/audit now reports two critical entries (`piscina` and its dependent
`@angular/build`, advisory GHSA-67c8-pqhq-4rmx); this supersedes the historical
zero-audit result above. Dependency security upgrades were left outside this
Docker repair. Stripe and application code were untouched.
