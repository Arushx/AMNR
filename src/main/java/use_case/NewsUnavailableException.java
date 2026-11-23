package use_case;

/**
 * Thrown by a data access object when news can't be provided, for any reason (no network, bad API
 * key, request limit reached, ...).
 *
 * <p>It lives in the use case layer so interactors can catch it without knowing where the news
 * comes from.
 */
public class NewsUnavailableException extends Exception {

  /**
   * Creates the exception with a message that is safe to show to the user.
   *
   * @param message what went wrong
   */
  public NewsUnavailableException(String message) {
    super(message);
  }
}
