package use_case.explain_movement;

/**
 * Thrown by a {@link MoveSummarizer} that can't produce a summary, e.g. a summarizer that calls a
 * language model that is down or replies with something unusable.
 */
public class SummaryUnavailableException extends Exception {

  /**
   * Creates the exception.
   *
   * @param message what went wrong
   */
  public SummaryUnavailableException(String message) {
    super(message);
  }
}
