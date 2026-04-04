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
  INVALID_ARGUMENT(400, "Invalid Argument"),
  FAILED_PRECONDITION(400, "Failed Precondition"),
  OUT_OF_RANGE(400, "Out of Range"),
  UNAUTHENTICATED(401, "Unauthenticated"),
  PERMISSION_DENIED(403, "Permission Denied"),
  NOT_FOUND(404, "Not Found"),
  ALREADY_EXISTS(409, "Already Exists"),
  ABORTED(409, "Aborted"),
  RESOURCE_EXHAUSTED(429, "Resource Exhausted"),
  CANCELLED(499, "Cancelled"),
  INTERNAL(500, "Internal"),
  DATA_LOSS(500, "Data Loss"),
  UNAVAILABLE(503, "Unavailable"),
  DEADLINE_EXCEEDED(504, "Deadline Exceeded");

  private final int httpStatus;
  private final String title;

  ErrorCode(int httpStatus, String title) {
    this.httpStatus = httpStatus;
    this.title = title;
  }

  public int httpStatus() {
    return httpStatus;
  }

  public String title() {
    return title;
  }
}
