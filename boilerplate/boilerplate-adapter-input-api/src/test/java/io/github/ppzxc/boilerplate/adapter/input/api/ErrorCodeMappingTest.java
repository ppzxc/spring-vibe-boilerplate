package io.github.ppzxc.boilerplate.adapter.input.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.ppzxc.boilerplate.domain.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ErrorCodeMappingTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void invalid_argument_maps_to_400() {
    assertThat(handler.errorCodeToHttpStatus(ErrorCode.INVALID_ARGUMENT))
        .isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(handler.errorCodeToTitle(ErrorCode.INVALID_ARGUMENT)).isEqualTo("Invalid Argument");
  }

  @Test
  void not_found_maps_to_404() {
    assertThat(handler.errorCodeToHttpStatus(ErrorCode.NOT_FOUND)).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(handler.errorCodeToTitle(ErrorCode.NOT_FOUND)).isEqualTo("Not Found");
  }

  @Test
  void internal_maps_to_500() {
    assertThat(handler.errorCodeToHttpStatus(ErrorCode.INTERNAL))
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(handler.errorCodeToTitle(ErrorCode.INTERNAL)).isEqualTo("Internal");
  }
}
