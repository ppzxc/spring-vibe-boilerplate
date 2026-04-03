package io.github.ppzxc.template.domain;

import java.util.List;

/**
 * 도메인 레이어 기본 예외.
 *
 * <p>AIP-193 ErrorCode를 보유하며, adapter 레이어에서 ProblemDetail로 변환된다. 선택적으로 FieldViolation 상세를 포함할 수 있다.
 */
public class DomainException extends RuntimeException {

  private final ErrorCode errorCode;
  private final List<FieldViolation> details;

  public DomainException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
    this.details = List.of();
  }

  public DomainException(ErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
    this.details = List.of();
  }

  private DomainException(ErrorCode errorCode, String message, List<FieldViolation> details) {
    super(message);
    this.errorCode = errorCode;
    this.details = List.copyOf(details);
  }

  public static DomainException withDetails(
      ErrorCode errorCode, String message, List<FieldViolation> details) {
    return new DomainException(errorCode, message, details);
  }

  public ErrorCode errorCode() {
    return errorCode;
  }

  public List<FieldViolation> details() {
    return details;
  }

  /**
   * AIP-193 BadRequest.FieldViolation에 대응하는 필드 검증 상세.
   *
   * @param field 위반 필드명
   * @param description 위반 설명
   */
  public record FieldViolation(String field, String description) {}
}
