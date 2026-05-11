package io.github.ppzxc.boilerplate.identity.domain.exception;

/** User Aggregate Domain Exception 그룹 (D-13). */
public abstract sealed class UserException extends RuntimeException
    permits UserException.AlreadySuspendedException,
        UserException.AlreadyDeactivatedException,
        UserException.AlreadyExistsException,
        UserException.NotFoundException,
        UserException.InvalidCredentialException,
        UserException.IneligibleStatusException {

  protected UserException(String message) {
    super(message);
  }

  public static final class AlreadySuspendedException extends UserException {
    public AlreadySuspendedException(String userId) {
      super("User already suspended: " + userId);
    }
  }

  public static final class AlreadyDeactivatedException extends UserException {
    public AlreadyDeactivatedException(String userId) {
      super("User already deactivated: " + userId);
    }
  }

  public static final class AlreadyExistsException extends UserException {
    public AlreadyExistsException(String email) {
      super("User already exists with email: " + email);
    }
  }

  public static final class NotFoundException extends UserException {
    public NotFoundException(String userId) {
      super("User not found: " + userId);
    }
  }

  public static final class InvalidCredentialException extends UserException {
    public InvalidCredentialException() {
      super("Invalid email or password");
    }
  }

  public static final class IneligibleStatusException extends UserException {
    public IneligibleStatusException(String status) {
      super("User status is not eligible for login: " + status);
    }
  }
}
