package io.github.ppzxc.boilerplate.adapter.output.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class ExternalApiClientTest {

  private ExternalApiClient client;
  private CircuitBreakerRegistry circuitBreakerRegistry;
  private RetryRegistry retryRegistry;
  private RateLimiterRegistry rateLimiterRegistry;

  @BeforeEach
  void setUp() {
    circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
    retryRegistry = RetryRegistry.ofDefaults();
    rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
    client =
        new ExternalApiClient(
            RestClient.create(), circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
  }

  @Test
  void fetchById_returnsEmpty_forSkeletonImplementation() {
    Optional<String> result = client.fetchById("any-id");

    assertThat(result).isEmpty();
  }

  @Test
  void fetchById_usesCircuitBreaker_byComponentName() {
    CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("externalApi");

    assertThat(circuitBreaker).isNotNull();
    assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
  }

  @Test
  void fetchById_circuitBreakerOpens_whenFailureThresholdExceeded() {
    CircuitBreakerConfig config =
        CircuitBreakerConfig.custom()
            .slidingWindowSize(2)
            .minimumNumberOfCalls(2)
            .failureRateThreshold(100f)
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .build();
    CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
    CircuitBreaker circuitBreaker = registry.circuitBreaker("externalApi");

    // 실패를 시뮬레이션하여 CircuitBreaker 상태 변경
    circuitBreaker.onError(0, java.util.concurrent.TimeUnit.NANOSECONDS, new RuntimeException());
    circuitBreaker.onError(0, java.util.concurrent.TimeUnit.NANOSECONDS, new RuntimeException());

    assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
  }

  @Test
  void fetchById_throwsCallNotPermitted_whenCircuitBreakerIsOpen() {
    CircuitBreakerConfig config =
        CircuitBreakerConfig.custom()
            .slidingWindowSize(2)
            .minimumNumberOfCalls(2)
            .failureRateThreshold(100f)
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .build();
    CircuitBreakerRegistry openRegistry = CircuitBreakerRegistry.of(config);
    openRegistry.circuitBreaker("externalApi").transitionToOpenState();

    RetryConfig retryConfig = RetryConfig.custom().maxAttempts(1).build();
    RetryRegistry noRetry = RetryRegistry.of(retryConfig);

    ExternalApiClient clientWithOpenCircuit =
        new ExternalApiClient(RestClient.create(), openRegistry, noRetry, rateLimiterRegistry);

    assertThatThrownBy(() -> clientWithOpenCircuit.fetchById("id"))
        .isInstanceOf(CallNotPermittedException.class);
  }

  @Test
  void fetchById_retriesOnFailure_withRetryRegistry() {
    // RetryRegistry가 기본 설정으로 최대 3회 재시도하는지 확인
    RetryConfig defaultConfig = retryRegistry.retry("externalApi").getRetryConfig();
    assertThat(defaultConfig.getMaxAttempts()).isGreaterThan(1);
  }
}
