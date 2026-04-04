package io.github.ppzxc.boilerplate.adapter.output.external;

import io.github.ppzxc.boilerplate.application.port.output.shared.ExternalDataPort;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.web.client.RestClient;

/**
 * 외부 API Outbound Adapter 스켈레톤.
 *
 * <p>CircuitBreaker + Retry + RateLimiter 패턴 예시. 실제 외부 API 연동 시 이 클래스를 참조하여 구현.
 */
@SuppressWarnings("UnusedVariable")
public class ExternalApiClient implements ExternalDataPort {

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
    return supplier.get();
  }

  private Optional<String> doFetch(String id) {
    // 스켈레톤: 실제 외부 API 호출 로직으로 교체
    // 예: return Optional.ofNullable(
    //       restClient.get().uri("/resources/{id}", id).retrieve().body(String.class));
    return Optional.empty();
  }
}
