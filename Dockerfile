# syntax=docker/dockerfile:1.7
# =============================================================================
# event_recommender — multi-stage build
# =============================================================================
# Stage 1 (build): use the official Gradle image with JDK 21 to compile and
#                  package the Spring Boot fat jar. No host-side build is needed;
#                  `docker build .` is fully self-contained.
# Stage 2 (run):   a slim JRE 21 Alpine image that runs the application as an
#                  unprivileged user with a built-in HEALTHCHECK against
#                  /actuator/health/readiness.
# =============================================================================

# ---- Stage 1: build ---------------------------------------------------------
FROM gradle:8.14.4-jdk21 AS build
WORKDIR /workspace

# Copy build descriptors first so dependency resolution is cached across
# source-only changes.
COPY settings.gradle.kts build.gradle.kts ./
COPY gradle gradle

# Prime the dependency cache (best-effort; ignored on first build when sources
# are missing).
RUN gradle --no-daemon dependencies > /dev/null 2>&1 || true

# Copy the rest of the project and build the executable jar.
COPY src src
RUN gradle --no-daemon clean bootJar

# ---- Stage 2: runtime -------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# wget is needed for the HEALTHCHECK below; it is already in alpine-base but we
# pin it explicitly for reproducibility.
RUN apk add --no-cache wget \
 && addgroup -S app \
 && adduser  -S -G app app

WORKDIR /app
COPY --from=build --chown=app:app /workspace/build/libs/event-recommender-*-SNAPSHOT.jar app.jar

USER app
EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=45s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
