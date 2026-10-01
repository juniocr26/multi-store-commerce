# Version selection

[Português](../pt-BR/versions.md)

Verified against official documentation and registry metadata on 2026-10-01. These are explicit compatible selections, not a claim that every dependency is the newest available.

Java 21 is an LTS baseline compatible with Boot 3.5.16 (Java 17–25); Temurin 21.0.12.1+1 supplies the JDK build and JRE runtime. Maven 3.9.11 is compatible and pinned by Wrapper 3.3.4. Boot 3.5 avoids introducing a major framework migration into this small foundation; springdoc 2.8.17 follows its Boot 3.5 compatibility family.

Angular 21.2.25 uses CLI/build 21.2.24 (independently published package versions). Angular 21 supports Node 22.12+ and TypeScript 5.9.x; selected Node 22.23.3 LTS, TypeScript 5.9.3 and RxJS 7.8.2 satisfy those ranges. Vitest 4.1.11 and jsdom 27.4.0 support DOM tests without a browser download. npm 10.9.9 bundled with Node runs `npm ci`; the lock was resolved with npm 11.11.1 to avoid an npm 10 peer-resolution crash. The lockfile, rather than a global npm update, controls reproducible installs.

PostgreSQL 17.11 uses the documented PostgreSQL ≤17 data directory `/var/lib/postgresql/data`. PostgreSQL 18 changed the layout; do not change the major image tag without a data migration plan. All three runtime/base image families have exact tags plus immutable multi-platform digests. BuildKit caches Maven downloads. Package resolution is locked, but this does not claim bit-for-bit reproducible output across machines.

Boot's BOM manages Flyway 11.7.2, Hibernate 6.6.53.Final, PostgreSQL JDBC 42.7.11, JUnit 5.12.2, Mockito 5.17.0 and Testcontainers 1.21.4. Both `flyway-core` and `flyway-database-postgresql` are included. No H2 fallback is used.

## Sources

- [Spring Boot 3.5 requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [Spring Boot package metadata](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/maven-metadata.xml)
- [Angular compatibility matrix](https://angular.dev/reference/versions)
- [Angular core metadata](https://registry.npmjs.org/@angular/core/21.2.25)
- [Angular build metadata](https://registry.npmjs.org/@angular/build/21.2.24)
- [Node release lifecycle](https://nodejs.org/en/about/previous-releases)
- [Node 22.23.3 checksums](https://nodejs.org/dist/v22.23.3/SHASUMS256.txt)
- [springdoc compatibility](https://springdoc.org/#what-is-the-compatibility-matrix-of-springdoc-openapi-with-spring-boot)
- [PostgreSQL image storage layout](https://hub.docker.com/_/postgres)
- [Temurin image metadata](https://hub.docker.com/v2/repositories/library/eclipse-temurin/tags/21.0.12.1_1-jre-noble)
- [PostgreSQL image metadata](https://hub.docker.com/v2/repositories/library/postgres/tags/17.11-bookworm)
- [Node image metadata](https://hub.docker.com/v2/repositories/library/node/tags/22.23.3-bookworm-slim)
