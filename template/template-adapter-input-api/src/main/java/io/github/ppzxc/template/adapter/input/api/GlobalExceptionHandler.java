package io.github.ppzxc.template.adapter.input.api;

import io.github.ppzxc.template.domain.DomainException;
import io.github.ppzxc.template.domain.ErrorCode;
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
import org.springframework.validation.FieldError;
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
    ProblemDetail problem = ProblemDetail.forStatus(errorCode.httpStatus());
    problem.setTitle(errorCode.title());
    problem.setDetail(ex.getMessage());
    problem.setProperty("errorCode", errorCode.name());
    problem.setProperty("details", ex.details());

    LOGGER
        .atWarn()
        .addKeyValue("errorCode", errorCode.name())
        .addKeyValue("httpStatus", errorCode.httpStatus())
        .log(ex.getMessage());

    return ResponseEntity.status(errorCode.httpStatus()).body(problem);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ProblemDetail> handleUnexpectedException(Exception ex) {
    ProblemDetail problem = ProblemDetail.forStatus(ErrorCode.INTERNAL.httpStatus());
    problem.setTitle(ErrorCode.INTERNAL.title());
    problem.setDetail("An unexpected error occurred");
    problem.setProperty("errorCode", ErrorCode.INTERNAL.name());
    problem.setProperty("details", List.of());

    LOGGER.atError().setCause(ex).log("Unexpected error");

    return ResponseEntity.status(ErrorCode.INTERNAL.httpStatus()).body(problem);
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
    problem.setTitle(ErrorCode.INVALID_ARGUMENT.title());
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
        ex.getBindingResult().getAllErrors().stream()
            .filter(error -> error instanceof FieldError)
            .map(error -> (FieldError) error)
            .map(
                fieldError ->
                    new FieldViolationDto(
                        fieldError.getField(),
                        fieldError.getDefaultMessage() != null
                            ? fieldError.getDefaultMessage()
                            : "invalid value"))
            .toList();

    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle(ErrorCode.INVALID_ARGUMENT.title());
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
}
