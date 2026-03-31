package io.github.ppzxc.template.adapter.output.persist.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * template-adapter-output-persist의 JPA Repository 구현체를 자동 등록.
 *
 * <p>Flyway 마이그레이션은 Spring Boot 자동 구성으로 처리.
 */
@AutoConfiguration
@ComponentScan(basePackages = "io.github.ppzxc.template.adapter.output.persist")
public class PersistAdapterAutoConfiguration {}
