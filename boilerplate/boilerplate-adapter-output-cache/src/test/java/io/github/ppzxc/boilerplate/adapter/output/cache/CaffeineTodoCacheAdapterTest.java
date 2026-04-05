package io.github.ppzxc.boilerplate.adapter.output.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.ppzxc.boilerplate.domain.Todo;
import io.github.ppzxc.boilerplate.domain.TodoFixtures;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

class CaffeineTodoCacheAdapterTest {

  private CacheManager cacheManager;
  private Cache cache;
  private CaffeineTodoCacheAdapter adapter;

  @BeforeEach
  void setUp() {
    cacheManager = mock(CacheManager.class);
    cache = mock(Cache.class);
    adapter = new CaffeineTodoCacheAdapter(cacheManager);
  }

  @Test
  void get_returns_todo_when_cache_hit() {
    Todo todo = TodoFixtures.savedTodo(1L, "title", false);
    Cache.ValueWrapper wrapper = mock(Cache.ValueWrapper.class);
    when(cacheManager.getCache("todos")).thenReturn(cache);
    when(cache.get(todo.getId())).thenReturn(wrapper);
    when(wrapper.get()).thenReturn(todo);

    Optional<Todo> result = adapter.get(todo.getId());

    assertThat(result).contains(todo);
  }

  @Test
  void get_returns_empty_when_wrapper_is_null() {
    when(cacheManager.getCache("todos")).thenReturn(cache);
    when(cache.get(1L)).thenReturn(null);

    Optional<Todo> result = adapter.get(1L);

    assertThat(result).isEmpty();
  }

  @Test
  void get_returns_empty_when_cache_is_null() {
    when(cacheManager.getCache("todos")).thenReturn(null);

    Optional<Todo> result = adapter.get(1L);

    assertThat(result).isEmpty();
  }

  @Test
  void put_stores_todo_in_cache() {
    Todo todo = TodoFixtures.savedTodo(1L, "title", false);
    when(cacheManager.getCache("todos")).thenReturn(cache);

    adapter.put(todo);

    verify(cache).put(todo.getId(), todo);
  }

  @Test
  void put_does_nothing_when_cache_is_null() {
    Todo todo = TodoFixtures.savedTodo(1L, "title", false);
    when(cacheManager.getCache("todos")).thenReturn(null);

    adapter.put(todo);
    // 예외 없이 종료
  }

  @Test
  void evict_removes_entry_from_cache() {
    when(cacheManager.getCache("todos")).thenReturn(cache);

    adapter.evict(1L);

    verify(cache).evict(1L);
  }

  @Test
  void evict_does_nothing_when_cache_is_null() {
    when(cacheManager.getCache("todos")).thenReturn(null);

    adapter.evict(1L);
    // 예외 없이 종료
  }
}
