# syntax=docker/dockerfile:1
FROM eclipse-temurin:21.0.12.1_1-jdk-noble@sha256:1afee26c547e1aa89f47404854dd09203b8bd35a2be269e16bdfd69c4a2c2b50 AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -ntp dependency:go-offline
COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -ntp package -DskipTests

FROM eclipse-temurin:21.0.12.1_1-jre-noble@sha256:0c324fbe2e1455c3159184717440f3b52906ef82a39c4a6efc9edb401d31971a AS runtime
ARG APP_UID=10001
ARG APP_GID=10001
RUN groupadd --gid $APP_GID commerce && useradd --uid $APP_UID --gid $APP_GID --create-home commerce
WORKDIR /app
COPY --from=build --chown=commerce:commerce /workspace/target/backend-0.0.1-SNAPSHOT.jar app.jar
USER commerce
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=6 CMD ["/bin/bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080; printf 'GET /actuator/health/readiness HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n' >&3; head -n 1 <&3 | grep -q ' 200 '"]
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
