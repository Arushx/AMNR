package data_access;

/**
 * Thrown when Alpha Vantage data can't be fetched or understood: no network, a bad API key, the
 * daily request limit, an unknown symbol, or a response in an unexpected format.
 */
public class AlphaVantageException extends Exception {

  /**
   * Creates the exception with a message that is safe to show to the user.
   *
   * @param message what went wrong
   */
  public AlphaVantageException(String message) {
    super(message);
  }

  /**
   * Creates the exception, keeping the lower-level exception that caused it.
   *
   * @param message what went wrong
   * @param cause the original exception, e.g. an {@code IOException} from the network
   */
  public AlphaVantageException(String message, Throwable cause) {
    super(message, cause);
  }
}
