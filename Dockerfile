# syntax=docker/dockerfile:1

# ---- builder ----
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace

COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
RUN ./gradlew --version

COPY build.gradle.kts settings.gradle.kts gradle.properties rewrite.yml ./
COPY config/ config/
COPY boilerplate/ boilerplate/

RUN ./gradlew :boilerplate-boot-api:bootJar -x test --no-daemon && \
    java -Djarmode=tools -jar boilerplate/boilerplate-boot-api/build/libs/boilerplate-boot-api-0.0.1.jar extract --layers --launcher --destination /workspace/extracted

# ---- runtime ----
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

COPY --from=builder /workspace/extracted/dependencies/ ./
COPY --from=builder /workspace/extracted/spring-boot-loader/ ./
COPY --from=builder /workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/extracted/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
