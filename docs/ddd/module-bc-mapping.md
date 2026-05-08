# Module ↔ BC Mapping

> Gradle 모듈, BC, 계층의 매핑 매트릭스.
> 신규 BC 추가 시 `.claude/rules/scaffold.md` §신규 BC 모듈 초기화 체크리스트 Step 7과 함께 갱신한다.

---

| Gradle 모듈 | BC | 계층 | 역할 |
|------------|-----|------|------|
| `boilerplate-identity-domain` | Identity | Domain | Aggregate, VO, Event, Exception — 순수 Java |
| `boilerplate-identity-application` | Identity | Application | Port, UseCase, Command/Query/Result |
| `boilerplate-identity-adapter-input-api` | Identity | Adapter-In | REST Controller, Request/Response DTO |
| `boilerplate-identity-adapter-output-persist` | Identity | Adapter-Out | jOOQ PersistenceAdapter, QueryAdapter, Mapper |
| `boilerplate-identity-configuration` | Identity | Configuration | Bean 등록, TX 프록시, EventTranslator |
| `boilerplate-shared-event` | Cross-BC | Published Language | BC 간 Integration Event 계약 — 순수 Java record |
| `boilerplate-boot-api` | All | Boot | Spring Boot 진입점, 전체 조립 |
