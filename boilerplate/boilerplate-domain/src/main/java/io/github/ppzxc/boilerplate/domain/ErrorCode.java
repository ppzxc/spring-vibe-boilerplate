package io.github.ppzxc.boilerplate.domain;

/**
 * AIP-193 기반 표준 에러 코드.
 *
 * <p>Google API Improvement Proposals (AIP-193)의 canonical error codes를 따르며, gRPC 상태 코드와 1:1 매핑된다.
 * 각 코드는 대응하는 HTTP 상태 코드를 가진다.
 *
 * @see <a href="https://google.aip.dev/193">AIP-193: Errors</a>
 */
public enum ErrorCode {
  INVALID_ARGUMENT,
  FAILED_PRECONDITION,
  OUT_OF_RANGE,
  UNAUTHENTICATED,
  PERMISSION_DENIED,
  NOT_FOUND,
  ALREADY_EXISTS,
  ABORTED,
  RESOURCE_EXHAUSTED,
  CANCELLED,
  INTERNAL,
  DATA_LOSS,
  UNAVAILABLE,
  DEADLINE_EXCEEDED
}
