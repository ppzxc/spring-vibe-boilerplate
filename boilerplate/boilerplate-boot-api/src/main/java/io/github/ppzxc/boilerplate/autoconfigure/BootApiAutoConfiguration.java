package io.github.ppzxc.boilerplate.autoconfigure;

import io.github.ppzxc.boilerplate.health.ExternalServiceHealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Boot API 자동 구성 — HealthIndicator 등 인프라 Bean 등록. */
@AutoConfiguration
@EnableConfigurationProperties( CorsProperties.class)
public class BootApiAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  ExternalServiceHealthIndicator externalServiceHealthIndicator() {
    return new ExternalServiceHealthIndicator();
  }
}
