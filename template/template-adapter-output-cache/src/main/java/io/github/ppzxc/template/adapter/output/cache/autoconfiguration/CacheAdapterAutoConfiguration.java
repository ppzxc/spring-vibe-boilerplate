package io.github.ppzxc.template.adapter.output.cache.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * template-adapter-output-cache의 CachePort 구현체를 자동 등록.
 *
 * <p>Phase 1: Caffeine (인-메모리 캐시) 기본 사용. Phase 2+: spring.profiles.active=redis 설정 시 Redis 구현체로 전환.
 */
@AutoConfiguration
@ComponentScan(basePackages = "io.github.ppzxc.template.adapter.output.cache")
public class CacheAdapterAutoConfiguration {}
