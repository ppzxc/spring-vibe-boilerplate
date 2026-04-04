package io.github.ppzxc.boilerplate.adapter.input.api;

import io.github.ppzxc.boilerplate.domain.DomainException;
import io.github.ppzxc.boilerplate.domain.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** 전역 예외 처리기. 도메인 예외를 RFC 9457 ProblemDetail로 변환한다. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(DomainException.class)
  ResponseEntity<ProblemDetail> handleDomainException(DomainException ex) {
    ErrorCode errorCode = ex.errorCode();
    HttpStatus status = errorCodeToHttpStatus(errorCode);
    ProblemDetail problem = ProblemDetail.forStatus(status);
    problem.setTitle(errorCodeToTitle(errorCode));
    problem.setDetail(ex.getMessage());
    problem.setProperty("errorCode", errorCode.name());
    problem.setProperty("details", ex.details());

    LOGGER
        .atWarn()
        .addKeyValue("errorCode", errorCode.name())
        .addKeyValue("httpStatus", status.value())
        .log(ex.getMessage());

    return ResponseEntity.status(status).body(problem);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ProblemDetail> handleUnexpectedException(Exception ex) {
    HttpStatus status = errorCodeToHttpStatus(ErrorCode.INTERNAL);
    ProblemDetail problem = ProblemDetail.forStatus(status);
    problem.setTitle(errorCodeToTitle(ErrorCode.INTERNAL));
    problem.setDetail("An unexpected error occurred");
    problem.setProperty("errorCode", ErrorCode.INTERNAL.name());
    problem.setProperty("details", List.of());

    LOGGER.atError().setCause(ex).log("Unexpected error");

    return ResponseEntity.status(status).body(problem);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ProblemDetail> handleConstraintViolationException(
      ConstraintViolationException ex) {
    List<FieldViolationDto> violations =
        ex.getConstraintViolations().stream()
            .map(
                v -> {
                  String field = v.getPropertyPath().toString();
                  int dotIdx = field.lastIndexOf('.');
                  String shortField = dotIdx >= 0 ? field.substring(dotIdx + 1) : field;
                  return new FieldViolationDto(shortField, v.getMessage());
                })
            .toList();

    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle(errorCodeToTitle(ErrorCode.INVALID_ARGUMENT));
    problem.setProperty("errorCode", ErrorCode.INVALID_ARGUMENT.name());
    problem.setProperty("details", violations);

    LOGGER
        .atWarn()
        .addKeyValue("errorCode", ErrorCode.INVALID_ARGUMENT.name())
        .addKeyValue("violationCount", violations.size())
        .log("Constraint violation");

    return ResponseEntity.badRequest().body(problem);
  }

  record FieldViolationDto(String field, String description) {}

  @Override
  protected @org.jspecify.annotations.Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
      @NonNull MethodArgumentNotValidException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      @NonNull WebRequest request) {
    List<FieldViolationDto> violations =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError ->
                    new FieldViolationDto(
                        fieldError.getField(),
                        fieldError.getDefaultMessage() != null
                            ? fieldError.getDefaultMessage()
                            : "invalid value"))
            .toList();

    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle(errorCodeToTitle(ErrorCode.INVALID_ARGUMENT));
    problem.setProperty("errorCode", ErrorCode.INVALID_ARGUMENT.name());
    problem.setProperty("details", violations);

    LOGGER
        .atWarn()
        .addKeyValue("errorCode", ErrorCode.INVALID_ARGUMENT.name())
        .addKeyValue("violationCount", violations.size())
        .log("Validation failed");

    return ResponseEntity.badRequest().body(problem);
  }

  @Override
  protected @org.jspecify.annotations.Nullable ResponseEntity<Object> handleExceptionInternal(
      @NonNull Exception ex,
      @org.jspecify.annotations.Nullable Object body,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode statusCode,
      @NonNull WebRequest request) {
    ResponseEntity<Object> response =
        super.handleExceptionInternal(ex, body, headers, statusCode, request);
    if (response != null && response.getBody() instanceof ProblemDetail problemDetail) {
      ErrorCode errorCode = httpStatusToErrorCode(statusCode);
      problemDetail.setProperty("errorCode", errorCode.name());
      problemDetail.setProperty("details", List.of());
    }
    return response;
  }

  private ErrorCode httpStatusToErrorCode(HttpStatusCode statusCode) {
    int status = statusCode.value();
    if (status == 404) {
      return ErrorCode.NOT_FOUND;
    }
    if (status >= 400 && status < 500) {
      return ErrorCode.INVALID_ARGUMENT;
    }
    return ErrorCode.INTERNAL;
  }

  HttpStatus errorCodeToHttpStatus(ErrorCode errorCode) {
    return switch (errorCode) {
      case INVALID_ARGUMENT, FAILED_PRECONDITION, OUT_OF_RANGE -> HttpStatus.BAD_REQUEST;
      case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
      case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
      case NOT_FOUND -> HttpStatus.NOT_FOUND;
      case ALREADY_EXISTS, ABORTED -> HttpStatus.CONFLICT;
      case RESOURCE_EXHAUSTED -> HttpStatus.TOO_MANY_REQUESTS;
      case CANCELLED -> HttpStatus.valueOf(499);
      case INTERNAL, DATA_LOSS -> HttpStatus.INTERNAL_SERVER_ERROR;
      case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
      case DEADLINE_EXCEEDED -> HttpStatus.GATEWAY_TIMEOUT;
    };
  }

  String errorCodeToTitle(ErrorCode errorCode) {
    return switch (errorCode) {
      case INVALID_ARGUMENT -> "Invalid Argument";
      case FAILED_PRECONDITION -> "Failed Precondition";
      case OUT_OF_RANGE -> "Out of Range";
      case UNAUTHENTICATED -> "Unauthenticated";
      case PERMISSION_DENIED -> "Permission Denied";
      case NOT_FOUND -> "Not Found";
      case ALREADY_EXISTS -> "Already Exists";
      case ABORTED -> "Aborted";
      case RESOURCE_EXHAUSTED -> "Resource Exhausted";
      case CANCELLED -> "Cancelled";
      case INTERNAL -> "Internal";
      case DATA_LOSS -> "Data Loss";
      case UNAVAILABLE -> "Unavailable";
      case DEADLINE_EXCEEDED -> "Deadline Exceeded";
    };
  }
}
