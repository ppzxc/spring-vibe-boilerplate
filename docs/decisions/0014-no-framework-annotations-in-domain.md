# ADR-0014: Domain에 프레임워크 어노테이션 금지

## Status

Accepted

## Context

ADR-0001의 세부 규칙. Domain 클래스에 Spring `@Component`, `@Service`, `@Entity`, `@Table`, JPA 어노테이션, Jackson `@JsonProperty` 등 프레임워크/기술 어노테이션을 붙이는 관행을 명시적으로 금지한다.

## Decision

Domain 모듈 클래스에 프레임워크 어노테이션을 사용하지 않는다 (D-2).

허용: 순수 Java 어노테이션 (`@Override`, `@FunctionalInterface`, `@SuppressWarnings` 등)
금지: `org.springframework.*`, `jakarta.*`, `com.fasterxml.*`, `org.jooq.*`, `org.jspecify.*`

## Consequences

Domain 클래스가 프레임워크 교체에 영향받지 않음. Null-safety는 `Objects.requireNonNull`로 처리. Bean 등록은 Configuration 모듈이 담당.
