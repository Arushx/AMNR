package use_case;

/**
 * Thrown by a data access object when stock data can't be provided, for any reason (no network,
 * unknown symbol, request limit reached, ...).
 *
 * <p>It lives in the use case layer so interactors can catch it without knowing where the data
 * comes from.
 */
public class StockDataUnavailableException extends Exception {

  /**
   * Creates the exception with a message that is safe to show to the user.
   *
   * @param message what went wrong
   */
  public StockDataUnavailableException(String message) {
    super(message);
  }
}
