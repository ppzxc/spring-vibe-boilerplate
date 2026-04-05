package io.github.ppzxc.boilerplate.adapter.output.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class ExternalApiClientTest {

  private static final String COMPONENT = "externalApi";

  @Test
  void fetchById_returns_empty_on_normal_call() {
    ExternalApiClient client = clientWithDefaults();

    Optional<String> result = client.fetchById("any-id");

    assertThat(result).isEmpty();
  }

  @Test
  void fetchById_throws_when_circuit_breaker_is_open() {
    CircuitBreakerRegistry cbRegistry = CircuitBreakerRegistry.ofDefaults();
    cbRegistry.circuitBreaker(COMPONENT).transitionToOpenState();
    ExternalApiClient client =
        new ExternalApiClient(
            mock(RestClient.class),
            cbRegistry,
            RetryRegistry.ofDefaults(),
            RateLimiterRegistry.ofDefaults());

    assertThatThrownBy(() -> client.fetchById("id")).isInstanceOf(CallNotPermittedException.class);
  }

  @Test
  void fetchById_retries_on_failure_then_propagates() {
    CircuitBreakerConfig cbConfig =
        CircuitBreakerConfig.custom()
            .slidingWindowSize(2)
            .minimumNumberOfCalls(2)
            .failureRateThreshold(100)
            .build();
    CircuitBreakerRegistry cbRegistry = CircuitBreakerRegistry.of(cbConfig);
    cbRegistry.circuitBreaker(COMPONENT).transitionToOpenState();

    RetryConfig retryConfig = RetryConfig.custom().maxAttempts(2).build();
    RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);

    ExternalApiClient client =
        new ExternalApiClient(
            mock(RestClient.class), cbRegistry, retryRegistry, RateLimiterRegistry.ofDefaults());

    assertThatThrownBy(() -> client.fetchById("id")).isInstanceOf(CallNotPermittedException.class);

    assertThat(retryRegistry.retry(COMPONENT).getMetrics().getNumberOfFailedCallsWithRetryAttempt())
        .isGreaterThanOrEqualTo(1);
  }

  @Test
  void fetchById_throws_when_rate_limit_exhausted() {
    RateLimiterConfig rlConfig =
        RateLimiterConfig.custom()
            .limitForPeriod(1)
            .limitRefreshPeriod(Duration.ofDays(1))
            .timeoutDuration(Duration.ZERO)
            .build();
    RateLimiterRegistry rlRegistry = RateLimiterRegistry.of(rlConfig);

    ExternalApiClient client =
        new ExternalApiClient(
            mock(RestClient.class),
            CircuitBreakerRegistry.ofDefaults(),
            RetryRegistry.ofDefaults(),
            rlRegistry);

    client.fetchById("first-call-consumes-permit");

    assertThatThrownBy(() -> client.fetchById("second-call-exhausted"))
        .isInstanceOf(RequestNotPermitted.class);
  }

  @Test
  void fetchById_circuit_breaker_records_success_when_call_succeeds() {
    CircuitBreakerRegistry cbRegistry = CircuitBreakerRegistry.ofDefaults();
    CircuitBreaker cb = cbRegistry.circuitBreaker(COMPONENT);

    ExternalApiClient client =
        new ExternalApiClient(
            mock(RestClient.class),
            cbRegistry,
            RetryRegistry.ofDefaults(),
            RateLimiterRegistry.ofDefaults());

    client.fetchById("id");

    assertThat(cb.getMetrics().getNumberOfSuccessfulCalls()).isEqualTo(1);
  }

  private ExternalApiClient clientWithDefaults() {
    return new ExternalApiClient(
        mock(RestClient.class),
        CircuitBreakerRegistry.ofDefaults(),
        RetryRegistry.ofDefaults(),
        RateLimiterRegistry.ofDefaults());
  }
}
