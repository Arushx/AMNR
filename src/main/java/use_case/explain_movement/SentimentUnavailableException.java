package use_case.explain_movement;

/**
 * Thrown by a {@link SentimentClassifier} that can't label articles, e.g. one that runs a model
 * which failed to load or crashed.
 */
public class SentimentUnavailableException extends Exception {

  /**
   * Creates the exception.
   *
   * @param message what went wrong
   */
  public SentimentUnavailableException(String message) {
    super(message);
  }

  /**
   * Creates the exception, keeping the lower-level exception that caused it.
   *
   * @param message what went wrong
   * @param cause the original exception
   */
  public SentimentUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
