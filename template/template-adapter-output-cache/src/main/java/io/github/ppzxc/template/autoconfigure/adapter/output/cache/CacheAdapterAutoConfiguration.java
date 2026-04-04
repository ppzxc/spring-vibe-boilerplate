package io.github.ppzxc.template.autoconfigure.adapter.output.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.ppzxc.template.adapter.output.cache.CaffeineTodoCacheAdapter;
import io.github.ppzxc.template.application.port.output.shared.TodoCachePort;
import java.time.Duration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;

/** Cache Adapter AutoConfiguration — Caffeine L1 캐시 Bean 등록. */
@AutoConfiguration
public class CacheAdapterAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  CacheManager cacheManager() {
    CaffeineCacheManager manager = new CaffeineCacheManager("todos");
    manager.setCaffeine(
        Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .recordStats());
    return manager;
  }

  @Bean
  @ConditionalOnMissingBean
  CaffeineTodoCacheAdapter caffeineTodoCacheAdapter(CacheManager cacheManager) {
    return new CaffeineTodoCacheAdapter(cacheManager);
  }

  @Bean
  @ConditionalOnMissingBean
  TodoCachePort todoCachePort(CaffeineTodoCacheAdapter caffeineTodoCacheAdapter) {
    return caffeineTodoCacheAdapter;
  }
}
