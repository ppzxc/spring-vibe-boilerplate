# Assembly Rules

## 모듈 자동 조립 규칙 [ADR-0014]

- 모든 모듈은 자체 AutoConfiguration + `AutoConfiguration.imports` 파일로 Bean을 등록할 것 [ADR-0014]
- Boot 모듈에서 `@ComponentScan` 커스터마이징 금지 — `AutoConfigurationExcludeFilter`를 파괴한다 [ADR-0014]
- Boot 모듈에서 `@Bean` 직접 등록 금지 [ADR-0014]
- `AutoConfiguration.imports` 미등록 상태에서 `@AutoConfiguration` 사용 금지 [ADR-0014]
- adapter 모듈에서 `@Component`/`@Service`/`@Repository` 스테레오타입 사용 금지 [ADR-0014]
- AutoConfiguration 패키지: `io.github.ppzxc.template.autoconfigure.*` 네임스페이스 사용 [ADR-0014]
