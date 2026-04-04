# Containerization Rules

## 이미지 빌드 방법 [ADR-0010]

```bash
# 권장: bootBuildImage (Buildpacks, Dockerfile 불필요)
./gradlew bootBuildImage

# 대안: Dockerfile (직접 제어 필요 시)
docker build -t boilerplate-api .
```

## docker-compose 구성 규칙

- 파일 위치: 프로젝트 루트 `docker-compose.yml`
- 비밀값은 `.env` 파일로 분리하여 주입 (`configuration.md` 비밀 관리 규칙 준수)

```yaml
services:
  app:
    image: boilerplate-api
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: local
      # 영속화 기술 선택 후 필요한 환경변수 추가
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
