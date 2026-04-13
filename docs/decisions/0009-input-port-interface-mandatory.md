# ADR-0009: Input Port 인터페이스 필수

## Status

Accepted

## Context

단순 CRUD UseCase에서 Input Port 인터페이스 없이 Service를 직접 주입하면 코드가 줄어든다. 그러나 이는 Inbound Adapter가 Application 구현체에 직접 의존하게 만든다.

## Decision

모든 UseCase는 Input Port 인터페이스로 정의한다. "단순 CRUD라서 생략"은 허용하지 않는다.

Interface: `{Verb}{Subject}UseCase` → `application/port/in/`
Implementation: `{Verb}{Subject}Service` → `application/service/`

## Consequences

단순 CRUD도 인터페이스 파일이 추가되나, Inbound Adapter와 Application Service 간 결합도가 제거된다. TX 프록시도 인터페이스를 기반으로 생성된다.
