package io.github.ppzxc.boilerplate.autoconfigure.adapter.output.external;

import io.github.ppzxc.boilerplate.adapter.output.external.ExternalApiClient;
import io.github.ppzxc.boilerplate.application.port.output.shared.CheckExternalServiceHealthPort;
import io.github.ppzxc.boilerplate.application.port.output.shared.ExternalDataPort;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/** External API Adapter AutoConfiguration — Resilience4j 레지스트리 + Adapter Bean 등록. */
@AutoConfiguration
public class ExternalAdapterAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  CircuitBreakerRegistry circuitBreakerRegistry() {
    return CircuitBreakerRegistry.ofDefaults();
  }

  @Bean
  @ConditionalOnMissingBean
  RetryRegistry retryRegistry() {
    return RetryRegistry.ofDefaults();
  }

  @Bean
  @ConditionalOnMissingBean
  RateLimiterRegistry rateLimiterRegistry() {
    return RateLimiterRegistry.ofDefaults();
  }

  @Bean(name = "externalRestClient")
  @ConditionalOnMissingBean(name = "externalRestClient")
  RestClient externalRestClient() {
    return RestClient.create();
  }

  @Bean
  @ConditionalOnMissingBean
  ExternalApiClient externalApiClient(
      RestClient externalRestClient,
      CircuitBreakerRegistry circuitBreakerRegistry,
      RetryRegistry retryRegistry,
      RateLimiterRegistry rateLimiterRegistry) {
    return new ExternalApiClient(
        externalRestClient, circuitBreakerRegistry, retryRegistry, rateLimiterRegistry);
  }

  @Bean
  @ConditionalOnMissingBean
  ExternalDataPort externalDataPort(ExternalApiClient externalApiClient) {
    return externalApiClient;
  }

  @Bean
  @ConditionalOnMissingBean
  CheckExternalServiceHealthPort checkExternalServiceHealthPort(
      ExternalApiClient externalApiClient) {
    return externalApiClient;
  }
}
