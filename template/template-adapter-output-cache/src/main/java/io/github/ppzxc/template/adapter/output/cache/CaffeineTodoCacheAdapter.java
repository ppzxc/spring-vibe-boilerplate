package io.github.ppzxc.template.adapter.output.cache;

import io.github.ppzxc.template.application.port.output.shared.TodoCachePort;
import io.github.ppzxc.template.domain.Todo;
import java.util.Optional;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/** Caffeine 기반 Todo 캐시 어댑터. */
public class CaffeineTodoCacheAdapter implements TodoCachePort {

  private static final String CACHE_NAME = "todos";

  private final CacheManager cacheManager;

  public CaffeineTodoCacheAdapter(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  @Override
  public Optional<Todo> get(long id) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache == null) {
      return Optional.empty();
    }
    Cache.ValueWrapper wrapper = cache.get(id);
    if (wrapper == null) {
      return Optional.empty();
    }
    return Optional.ofNullable((Todo) wrapper.get());
  }

  @Override
  public void put(Todo todo) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache != null && todo.getId() != null) {
      cache.put(todo.getId(), todo);
    }
  }

  @Override
  public void evict(long id) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache != null) {
      cache.evict(id);
    }
  }
}
