package io.github.ppzxc.boilerplate.adapter.output.external;

import io.github.ppzxc.boilerplate.application.port.output.shared.CheckExternalServiceHealthPort;
import io.github.ppzxc.boilerplate.application.port.output.shared.ExternalDataPort;
import io.github.ppzxc.boilerplate.domain.DomainException;
import io.github.ppzxc.boilerplate.domain.ErrorCode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 외부 API Outbound Adapter.
 *
 * <p>CircuitBreaker + Retry + RateLimiter 패턴과 함께 기술 예외({@link RestClientException}, {@link
 * CallNotPermittedException})를 도메인 예외({@link DomainException})로 변환한다. application 레이어는 Spring Web이나
 * Resilience4j에 의존하지 않는다.
 */
@SuppressWarnings("UnusedVariable")
public class ExternalApiClient implements ExternalDataPort, CheckExternalServiceHealthPort {

  private static final String COMPONENT_NAME = "externalApi";

  private final RestClient restClient;
  private final CircuitBreaker circuitBreaker;
  private final Retry retry;
  private final RateLimiter rateLimiter;

  public ExternalApiClient(
      RestClient restClient,
      CircuitBreakerRegistry circuitBreakerRegistry,
      RetryRegistry retryRegistry,
      RateLimiterRegistry rateLimiterRegistry) {
    this.restClient = restClient;
    this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(COMPONENT_NAME);
    this.retry = retryRegistry.retry(COMPONENT_NAME);
    this.rateLimiter = rateLimiterRegistry.rateLimiter(COMPONENT_NAME);
  }

  @Override
  public Optional<String> fetchById(String id) {
    Supplier<Optional<String>> supplier =
        CircuitBreaker.decorateSupplier(circuitBreaker, () -> doFetch(id));
    supplier = Retry.decorateSupplier(retry, supplier);
    supplier = RateLimiter.decorateSupplier(rateLimiter, supplier);
    try {
      return supplier.get();
    } catch (CallNotPermittedException e) {
      // CircuitBreaker가 OPEN 상태 — 외부 서비스 복구 대기 중
      throw new DomainException(ErrorCode.UNAVAILABLE, "External service circuit open", e);
    }
  }

  private Optional<String> doFetch(String id) {
    try {
      // 실제 외부 API 호출 — RestClientException을 도메인 예외로 변환
      return Optional.ofNullable(
          restClient.get().uri("/resources/{id}", id).retrieve().body(String.class));
    } catch (RestClientException e) {
      throw new DomainException(ErrorCode.UNAVAILABLE, "External service unavailable", e);
    }
  }

  @Override
  public boolean isHealthy() {
    // CircuitBreaker 상태가 OPEN이면 외부 서비스 장애로 판단
    return circuitBreaker.getState() != CircuitBreaker.State.OPEN;
  }
}
