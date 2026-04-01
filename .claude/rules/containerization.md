# Containerization Rules

## 이미지 빌드 방법 [ADR-0010]

```bash
# 권장: bootBuildImage (Buildpacks, Dockerfile 불필요)
./gradlew bootBuildImage

# 대안: Dockerfile (직접 제어 필요 시)
docker build -t template-api .
```

## docker-compose 구성 규칙

- 파일 위치: 프로젝트 루트 `docker-compose.yml`

```yaml
services:
  app:
    image: template-api
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: local
      DB_URL: jdbc:postgresql://postgres:5432/template
      DB_USERNAME: template
      DB_PASSWORD: template
    depends_on:
      - postgres

  postgres:
    image: postgres:18
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: template
      POSTGRES_USER: template
      POSTGRES_PASSWORD: template
```

## Dockerfile 작성 규칙 (대안 사용 시)

- 멀티스테이지 빌드 필수 (builder + runtime 분리)
- runtime 베이스 이미지: `eclipse-temurin:25-jre`
- Virtual Threads를 위한 JVM 옵션 고려:

```dockerfile
FROM eclipse-temurin:25-jre AS runtime
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

## 미채택 방식

| 방식 | 미채택 이유 |
|------|-----------|
| GraalVM Native Image | Virtual Threads와 목적 중복, 빌드 시간 과다 |
| Jib | 유지보수 빈도 감소 우려 |
