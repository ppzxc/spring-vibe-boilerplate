package io.github.ppzxc.boilerplate.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ErrorCodeTest {

  @ParameterizedTest
  @EnumSource(ErrorCode.class)
  void every_error_code_has_http_status(ErrorCode errorCode) {
    assertThat(errorCode.httpStatus()).isBetween(400, 599);
  }

  @ParameterizedTest
  @EnumSource(ErrorCode.class)
  void every_error_code_has_non_blank_title(ErrorCode errorCode) {
    assertThat(errorCode.title()).isNotBlank();
  }

  @Test
  void invalid_argument_maps_to_400() {
    assertThat(ErrorCode.INVALID_ARGUMENT.httpStatus()).isEqualTo(400);
  }

  @Test
  void not_found_maps_to_404() {
    assertThat(ErrorCode.NOT_FOUND.httpStatus()).isEqualTo(404);
  }

  @Test
  void unauthenticated_maps_to_401() {
    assertThat(ErrorCode.UNAUTHENTICATED.httpStatus()).isEqualTo(401);
  }

  @Test
  void permission_denied_maps_to_403() {
    assertThat(ErrorCode.PERMISSION_DENIED.httpStatus()).isEqualTo(403);
  }

  @Test
  void internal_maps_to_500() {
    assertThat(ErrorCode.INTERNAL.httpStatus()).isEqualTo(500);
  }
}
