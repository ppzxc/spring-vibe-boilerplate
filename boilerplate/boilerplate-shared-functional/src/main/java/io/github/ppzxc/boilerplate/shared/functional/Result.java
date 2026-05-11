package io.github.ppzxc.boilerplate.shared.functional;

import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface Result<T, E> {
  /** 성공 케이스. */
  record Success<T, E>(T value) implements Result<T, E> {}

  /** 실패 케이스. */
  record Failure<T, E>(E error) implements Result<T, E> {}

  static <T, E> Result<T, E> success(T value) {
    return new Success<>(value);
  }

  static <T, E> Result<T, E> failure(E error) {
    return new Failure<>(error);
  }

  default boolean isSuccess() {
    return this instanceof Success<T, E>;
  }

  default boolean isFailure() {
    return this instanceof Failure<T, E>;
  }

  default <U> Result<U, E> map(Function<? super T, ? extends U> fn) {
    return switch (this) {
      case Success<T, E>(var v) -> success(fn.apply(v));
      case Failure<T, E>(var e) -> failure(e);
    };
  }

  default <U> Result<U, E> flatMap(Function<? super T, Result<U, E>> fn) {
    return switch (this) {
      case Success<T, E>(var v) -> fn.apply(v);
      case Failure<T, E>(var e) -> failure(e);
    };
  }

  default <F> Result<T, F> mapError(Function<? super E, ? extends F> fn) {
    return switch (this) {
      case Success<T, E>(var v) -> success(v);
      case Failure<T, E>(var e) -> failure(fn.apply(e));
    };
  }

  default <R> R fold(
      Function<? super T, ? extends R> onSuccess, Function<? super E, ? extends R> onFailure) {
    return switch (this) {
      case Success<T, E>(var v) -> onSuccess.apply(v);
      case Failure<T, E>(var e) -> onFailure.apply(e);
    };
  }

  default Result<T, E> onSuccess(Consumer<? super T> action) {
    if (this instanceof Success<T, E>(var v)) {
      action.accept(v);
    }
    return this;
  }

  default Result<T, E> onFailure(Consumer<? super E> action) {
    if (this instanceof Failure<T, E>(var e)) {
      action.accept(e);
    }
    return this;
  }
}
