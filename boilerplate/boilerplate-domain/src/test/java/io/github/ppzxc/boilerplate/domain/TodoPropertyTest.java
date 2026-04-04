package io.github.ppzxc.boilerplate.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;

class TodoPropertyTest {

  /**
   * 불변식 1: 공백이 아닌 모든 문자열 title로 Todo를 생성할 수 있다.
   *
   * <p>생성된 Todo의 title은 앞뒤 공백이 제거(strip)된 값이어야 한다.
   */
  @Property
  void create_succeeds_for_any_non_blank_title(
      @ForAll @AlphaChars @StringLength(min = 1, max = 255) String title) {
    Todo todo = Todo.create(title);

    assertThat(todo.getTitle()).isEqualTo(title.strip());
    assertThat(todo.isCompleted()).isFalse();
    assertThat(todo.getId()).isNull();
    assertThat(todo.getCreatedAt()).isNotNull();
    assertThat(todo.getUpdatedAt()).isNotNull();
  }

  /**
   * 불변식 2: blank 문자열로 Todo를 생성하면 반드시 DomainException이 발생한다.
   *
   * <p>공백만 포함하는 모든 문자열에 대해 이 불변식이 성립해야 한다.
   */
  @Property
  void create_throws_for_any_blank_title(@ForAll("blankStrings") String blankTitle) {
    assertThatThrownBy(() -> Todo.create(blankTitle)).isInstanceOf(DomainException.class);
  }

  /**
   * 불변식 3: complete() 후 uncomplete()하면 항상 completed=false이다.
   *
   * <p>어떤 title로 생성하든, 상태 전환이 올바르게 되돌아와야 한다.
   */
  @Property
  void complete_then_uncomplete_always_returns_false(
      @ForAll @AlphaChars @StringLength(min = 1, max = 50) String title) {
    Todo todo = Todo.create(title);

    boolean result = todo.complete().uncomplete().isCompleted();

    assertThat(result).isFalse();
  }

  /**
   * 불변식 4: updateTitle()은 앞뒤 공백을 제거한 값을 반환한다.
   *
   * <p>패딩된 제목으로 updateTitle()을 호출하면 strip()된 값이 저장되어야 한다.
   */
  @Property
  void updateTitle_strips_leading_and_trailing_whitespace(
      @ForAll @AlphaChars @StringLength(min = 1, max = 50) String inner) {
    String paddedTitle = "  " + inner + "  ";
    Todo todo = Todo.create("initial");

    Todo updated = todo.updateTitle(paddedTitle);

    assertThat(updated.getTitle()).isEqualTo(inner);
  }

  @Provide
  Arbitrary<String> blankStrings() {
    return Arbitraries.strings().withChars(' ', '\t', '\n', '\r').ofMinLength(1).ofMaxLength(20);
  }
}
