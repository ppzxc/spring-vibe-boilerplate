package io.github.ppzxc.template.autoconfigure.adapter.input.api;

import io.github.ppzxc.template.adapter.input.api.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** adapter-input-api 모듈의 Bean을 등록하는 AutoConfiguration. */
@AutoConfiguration
public class ApiAdapterAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  GlobalExceptionHandler globalExceptionHandler() {
    return new GlobalExceptionHandler();
  }
}
