# syntax=docker/dockerfile:1
FROM eclipse-temurin:25.0.4.1_1-jdk-noble@sha256:0d623ea18d7b0fe1e12a2c0a920f7e950cad433387e5a49d3611e1507fa07602 AS build
# Wrapper 3.3.4 falls back to tar.gz without unzip, but the pinned checksum is
# for the ZIP. Keep the downloaded archive and integrity check consistent.
RUN apt-get update && apt-get install -y --no-install-recommends unzip \
    && rm -rf /var/lib/apt/lists/*
ARG MAVEN_CACHE_ID=commerce-maven
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN --mount=type=cache,id=${MAVEN_CACHE_ID},target=/root/.m2 ./mvnw -B -ntp dependency:go-offline
COPY src src
RUN --mount=type=cache,id=${MAVEN_CACHE_ID},target=/root/.m2 ./mvnw -B -ntp package -DskipTests

FROM eclipse-temurin:25.0.4.1_1-jre-noble@sha256:398f810215757dc1926390014272579fb0e57c41ef1c8aa4f64ae761613a168b AS runtime
ARG APP_UID=10001
ARG APP_GID=10001
RUN groupadd --gid $APP_GID commerce && useradd --uid $APP_UID --gid $APP_GID --create-home commerce
WORKDIR /app
COPY --from=build --chown=commerce:commerce /workspace/target/backend-0.0.1-SNAPSHOT.jar app.jar
USER commerce
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=6 CMD ["/bin/bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080; printf 'GET /actuator/health/readiness HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n' >&3; head -n 1 <&3 | grep -q ' 200 '"]
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
