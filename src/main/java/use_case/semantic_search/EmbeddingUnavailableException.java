package use_case.semantic_search;

/**
 * Thrown when text can't be turned into an embedding (model file missing, model failed to run).
 *
 * <p>It lives in the use case layer so the interactor can fall back to keyword search without
 * knowing how embeddings are made.
 */
public class EmbeddingUnavailableException extends Exception {

  /**
   * Creates the exception with a message that is safe to show to the user.
   *
   * @param message what went wrong
   */
  public EmbeddingUnavailableException(String message) {
    super(message);
  }

  /**
   * Creates the exception, keeping the underlying cause for debugging.
   *
   * @param message what went wrong
   * @param cause the original error
   */
  public EmbeddingUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
