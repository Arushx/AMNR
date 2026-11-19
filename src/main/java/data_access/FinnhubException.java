package data_access;

/**
 * Thrown when Finnhub data can't be fetched or understood: no network, a bad API key, the rate
 * limit, or a response in an unexpected format.
 */
public class FinnhubException extends Exception {

  /**
   * Creates the exception with a message that is safe to show to the user.
   *
   * @param message what went wrong
   */
  public FinnhubException(String message) {
    super(message);
  }

  /**
   * Creates the exception, keeping the lower-level exception that caused it.
   *
   * @param message what went wrong
   * @param cause the original exception, e.g. an {@code IOException} from the network
   */
  public FinnhubException(String message, Throwable cause) {
    super(message, cause);
  }
}
