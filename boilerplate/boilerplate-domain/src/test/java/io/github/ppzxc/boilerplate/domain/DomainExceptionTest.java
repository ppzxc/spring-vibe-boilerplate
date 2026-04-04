package io.github.ppzxc.boilerplate.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class DomainExceptionTest {

  @Test
  void create_with_error_code_and_message() {
    var exception = new DomainException(ErrorCode.NOT_FOUND, "Order not found");

    assertThat(exception.errorCode()).isEqualTo(ErrorCode.NOT_FOUND);
    assertThat(exception.getMessage()).isEqualTo("Order not found");
    assertThat(exception.details()).isEmpty();
  }

  @Test
  void create_with_details() {
    var details = List.of(new DomainException.FieldViolation("email", "must be a valid email"));
    var exception =
        DomainException.withDetails(ErrorCode.INVALID_ARGUMENT, "Validation failed", details);

    assertThat(exception.errorCode()).isEqualTo(ErrorCode.INVALID_ARGUMENT);
    assertThat(exception.details()).hasSize(1);
    assertThat(exception.details().getFirst().field()).isEqualTo("email");
    assertThat(exception.details().getFirst().description()).isEqualTo("must be a valid email");
  }

  @Test
  void create_with_cause() {
    var cause = new RuntimeException("DB connection failed");
    var exception = new DomainException(ErrorCode.INTERNAL, "Internal error", cause);

    assertThat(exception.getCause()).isEqualTo(cause);
  }
}
