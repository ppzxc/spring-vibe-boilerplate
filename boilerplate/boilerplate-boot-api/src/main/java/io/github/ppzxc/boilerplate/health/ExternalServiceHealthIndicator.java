package io.github.ppzxc.boilerplate.health;

import io.github.ppzxc.boilerplate.application.port.output.shared.CheckExternalServiceHealthPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

/** 외부 서비스 헬스체크. CheckExternalServiceHealthPort를 통해 헥사고날 경계를 준수한다. */
@RequiredArgsConstructor
public class ExternalServiceHealthIndicator implements HealthIndicator {

  private final CheckExternalServiceHealthPort checkExternalServiceHealthPort;

  @Override
  public Health health() {
    if (checkExternalServiceHealthPort.isHealthy()) {
      return Health.up().withDetail("externalService", "reachable").build();
    }
    return Health.down().withDetail("externalService", "unreachable").build();
  }
}
