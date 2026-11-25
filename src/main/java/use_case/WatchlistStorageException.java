package use_case;

/**
 * Thrown by a data access object when a watchlist can't be read or saved (database missing, symbol
 * already saved, unknown user, ...).
 *
 * <p>It lives in the use case layer so interactors can catch it without knowing how watchlists are
 * stored.
 */
public class WatchlistStorageException extends Exception {

  /**
   * Creates the exception with a message that is safe to show to the user.
   *
   * @param message what went wrong
   */
  public WatchlistStorageException(String message) {
    super(message);
  }

  /**
   * Creates the exception, keeping the underlying error for debugging.
   *
   * @param message what went wrong
   * @param cause the error that caused it
   */
  public WatchlistStorageException(String message, Throwable cause) {
    super(message, cause);
  }
}
