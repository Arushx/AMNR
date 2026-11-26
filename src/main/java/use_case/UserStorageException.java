package use_case;

/**
 * Thrown by a data access object when user accounts can't be read or saved (database missing,
 * username already taken, ...).
 *
 * <p>It lives in the use case layer so interactors can catch it without knowing how users are
 * stored.
 */
public class UserStorageException extends Exception {

  /**
   * Creates the exception with a message that is safe to show to the user.
   *
   * @param message what went wrong
   */
  public UserStorageException(String message) {
    super(message);
  }

  /**
   * Creates the exception, keeping the underlying error for debugging.
   *
   * @param message what went wrong
   * @param cause the error that caused it
   */
  public UserStorageException(String message, Throwable cause) {
    super(message, cause);
  }
}
