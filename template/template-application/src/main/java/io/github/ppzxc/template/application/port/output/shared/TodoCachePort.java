package io.github.ppzxc.template.application.port.output.shared;

import io.github.ppzxc.template.domain.Todo;
import java.util.Optional;

/** Todo 캐시 Outbound Port. 구현체는 adapter-output-cache 모듈에 위치. */
public interface TodoCachePort {

  Optional<Todo> get(long id);

  void put(Todo todo);

  void evict(long id);
}
