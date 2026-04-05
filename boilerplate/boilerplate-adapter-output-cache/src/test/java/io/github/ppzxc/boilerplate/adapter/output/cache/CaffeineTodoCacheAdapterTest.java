package io.github.ppzxc.boilerplate.adapter.output.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.ppzxc.boilerplate.domain.Todo;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCacheManager;

class CaffeineTodoCacheAdapterTest {

  private CaffeineTodoCacheAdapter adapter;

  @BeforeEach
  void setUp() {
    CaffeineCacheManager manager = new CaffeineCacheManager("todos");
    manager.setCaffeine(Caffeine.newBuilder().maximumSize(100));
    manager.setAllowNullValues(false);
    adapter = new CaffeineTodoCacheAdapter(manager);
  }

  @Test
  void get_returnEmpty_whenCacheMiss() {
    Optional<Todo> result = adapter.get(999L);

    assertThat(result).isEmpty();
  }

  @Test
  void put_andGet_returnsCachedTodo() {
    Todo todo = Todo.reconstitute(1L, "cached todo", false, now(), now());

    adapter.put(todo);
    Optional<Todo> result = adapter.get(1L);

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(1L);
    assertThat(result.get().getTitle()).isEqualTo("cached todo");
  }

  @Test
  void evict_removesEntryFromCache() {
    Todo todo = Todo.reconstitute(2L, "to be evicted", false, now(), now());
    adapter.put(todo);
    assertThat(adapter.get(2L)).isPresent();

    adapter.evict(2L);

    assertThat(adapter.get(2L)).isEmpty();
  }

  @Test
  void put_doesNothing_whenTodoIdIsNull() {
    Todo todoWithoutId = Todo.create("no id yet");

    adapter.put(todoWithoutId);

    // id가 null이면 캐시에 저장하지 않으므로, 어떤 키로도 조회 불가
    assertThat(adapter.get(0L)).isEmpty();
  }

  @Test
  void get_returnEmpty_whenCacheNameNotFound() {
    CaffeineCacheManager emptyManager = new CaffeineCacheManager();
    emptyManager.setCaffeine(Caffeine.newBuilder().maximumSize(100));
    CaffeineTodoCacheAdapter adapterWithoutCache = new CaffeineTodoCacheAdapter(emptyManager);

    Optional<Todo> result = adapterWithoutCache.get(1L);

    assertThat(result).isEmpty();
  }

  @Test
  void put_doesNothing_whenCacheNameNotFound() {
    CaffeineCacheManager emptyManager = new CaffeineCacheManager();
    emptyManager.setCaffeine(Caffeine.newBuilder().maximumSize(100));
    CaffeineTodoCacheAdapter adapterWithoutCache = new CaffeineTodoCacheAdapter(emptyManager);
    Todo todo = Todo.reconstitute(1L, "title", false, now(), now());

    adapterWithoutCache.put(todo);
    // should not throw
  }

  @Test
  void evict_doesNothing_whenCacheNameNotFound() {
    CaffeineCacheManager emptyManager = new CaffeineCacheManager();
    emptyManager.setCaffeine(Caffeine.newBuilder().maximumSize(100));
    CaffeineTodoCacheAdapter adapterWithoutCache = new CaffeineTodoCacheAdapter(emptyManager);

    adapterWithoutCache.evict(1L);
    // should not throw
  }

  private static java.time.LocalDateTime now() {
    return java.time.LocalDateTime.now(java.time.ZoneId.systemDefault());
  }
}
