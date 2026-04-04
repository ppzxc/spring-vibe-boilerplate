package io.github.ppzxc.boilerplate.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

/** 외부 서비스 헬스체크 스켈레톤. 실제 외부 서비스 연결 확인 로직으로 교체하여 사용. */
public class ExternalServiceHealthIndicator implements HealthIndicator {

  @Override
  public Health health() {
    // 스켈레톤: 실제 외부 서비스 health check 로직으로 교체
    // 예: HTTP ping, gRPC health check 등
    boolean externalServiceUp = checkExternalService();
    if (externalServiceUp) {
      return Health.up().withDetail("externalService", "reachable").build();
    }
    return Health.down().withDetail("externalService", "unreachable").build();
  }

  private boolean checkExternalService() {
    // TODO: 실제 외부 서비스 연결 확인 로직 구현
    return true;
  }
}
