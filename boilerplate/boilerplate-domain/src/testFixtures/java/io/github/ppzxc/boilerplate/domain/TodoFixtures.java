package io.github.ppzxc.boilerplate.domain;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Todo 도메인 테스트 픽스처 팩토리.
 *
 * <p>FixtureMonkey를 사용하여 테스트용 Todo 인스턴스를 생성한다.
 */
public class TodoFixtures {

  private static final FixtureMonkey MONKEY =
      FixtureMonkey.builder()
          .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
          .build();

  /** FixtureMonkey가 임의 값으로 생성한 저장된 Todo. id, title, completed, createdAt, updatedAt 모두 임의 값. */
  public static Todo randomSavedTodo() {
    return MONKEY
        .giveMeBuilder(Todo.class)
        .setNotNull("id")
        .setNotNull("title")
        .setNotNull("createdAt")
        .setNotNull("updatedAt")
        .sample();
  }

  /** 도메인 팩토리 메서드를 사용한 미저장 Todo. 도메인 불변식(title not blank)이 보장됨. */
  public static Todo unsavedTodo(String title) {
    return Todo.create(title);
  }

  /** 특정 값으로 구성된 저장된 Todo. reconstitute()를 직접 사용하므로 도메인 검증 없이 원하는 상태를 만들 수 있음. */
  public static Todo savedTodo(long id, String title, boolean completed) {
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    return Todo.reconstitute(id, title, completed, now, now);
  }
}
