# ADR-0008: Application Service에 @Transactional 금지

## Status

Accepted

## Context

Application Service에 `@Transactional`을 붙이는 것이 Spring 일반 관행이다. 그러나 `@Transactional`은 Spring 의존성이고, Application 모듈은 Spring에 의존하지 않아야 한다 (ADR-0001, ADR-0002).

## Decision

Application Service 클래스/메서드에 `@Transactional`을 붙이지 않는다. Application 모듈의 `build.gradle.kts`에 `spring-tx` 의존성을 포함하지 않는다.

트랜잭션 경계는 **Configuration 모듈**에서 프록시로 외부에서 적용한다.

**TX 프록시 패턴** (Configuration 모듈):
```java
private <T> T createTxProxy(Object target, Class<T> iface, PlatformTransactionManager tm) {
    var source = new MatchAlwaysTransactionAttributeSource();
    var interceptor = new TransactionInterceptor(tm, source);
    var factory = new ProxyFactory(target);
    factory.addInterface(iface);
    factory.addAdvice(interceptor);
    return iface.cast(factory.getProxy());
}
```

**T-1 UseCase = TX 경계**: UseCase(Input Port 구현체)가 트랜잭션 경계. Command UseCase: R/W TX, Query UseCase: R/O TX.

## Consequences

### Positive
- Application Service를 `new`로 생성 후 Spring 없이 단위 테스트 가능
- Application 모듈에 Spring 의존성 없음 → ADR-0001/0002 완전 준수
- TX 설정이 Configuration 한 곳에 집중 → 명시적

### Negative
- Bean 등록 시 TX 프록시 추가 코드 필요 → Configuration이 길어짐
- `@Transactional` 익숙한 개발자에게 낯선 패턴
- `@TransactionalEventListener` 등 Spring TX 어노테이션은 Configuration 모듈에서만 사용 가능
