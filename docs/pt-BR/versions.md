# Seleção de versões

[English](../en/versions.md)

Verificado na documentação oficial e nos registros de pacotes em 2026-10-02. São escolhas explícitas e compatíveis, sem afirmar que todas as dependências são as mais recentes.

Java 25 é a versão LTS mais recente em 2026-10-02 e é compatível com Boot 3.5.16 (Java 17–25); Temurin 25.0.4.1+1 fornece JDK de build e JRE de execução. Maven 3.9.11 é compatível e fixado pelo Wrapper 3.3.4. Boot 3.5 evita introduzir uma migração principal do framework nesta fundação; springdoc 2.8.17 segue a família compatível com Boot 3.5.

Angular 21.2.25 utiliza CLI/build 21.2.24 (pacotes publicados independentemente). Angular 21 aceita Node 22.12+ e TypeScript 5.9.x; Node 22.23.3 LTS, TypeScript 5.9.3 e RxJS 7.8.2 atendem aos intervalos. Vitest 4.1.11 e jsdom 27.4.0 permitem testes DOM sem baixar navegador. npm 10.9.9 incluído no Node executa `npm ci`; o lock foi resolvido com npm 11.11.1 para contornar uma falha do npm 10 na resolução de peers. O lockfile controla as instalações reproduzíveis, sem exigir atualização global do npm.

PostgreSQL 17.11 utiliza `/var/lib/postgresql/data`, documentado para versões ≤17. PostgreSQL 18 alterou o layout; não mude a versão principal sem planejar a migração dos dados. As imagens têm tags exatas e digests imutáveis multi-plataforma. BuildKit reutiliza downloads Maven. A resolução de pacotes é fixada, sem alegar builds idênticos byte a byte entre máquinas.

O BOM do Boot gerencia Flyway 11.7.2, Hibernate 6.6.53.Final, JDBC PostgreSQL 42.7.11, JUnit 5.12.2, Mockito 5.17.0 e Testcontainers 1.21.4. Inclui `flyway-core` e `flyway-database-postgresql`. Não há fallback H2.

## Fontes

- [Spring Boot 3.5 requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [Spring Boot package metadata](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/maven-metadata.xml)
- [Angular compatibility matrix](https://angular.dev/reference/versions)
- [Angular core metadata](https://registry.npmjs.org/@angular/core/21.2.25)
- [Angular build metadata](https://registry.npmjs.org/@angular/build/21.2.24)
- [Node release lifecycle](https://nodejs.org/en/about/previous-releases)
- [Node 22.23.3 checksums](https://nodejs.org/dist/v22.23.3/SHASUMS256.txt)
- [springdoc compatibility](https://springdoc.org/#what-is-the-compatibility-matrix-of-springdoc-openapi-with-spring-boot)
- [PostgreSQL image storage layout](https://hub.docker.com/_/postgres)
- [Temurin 25.0.4.1+1 image metadata](https://hub.docker.com/v2/repositories/library/eclipse-temurin/tags/25.0.4.1_1-jre-noble)
- [PostgreSQL image metadata](https://hub.docker.com/v2/repositories/library/postgres/tags/17.11-bookworm)
- [Node image metadata](https://hub.docker.com/v2/repositories/library/node/tags/22.23.3-bookworm-slim)
