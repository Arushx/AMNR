package interface_adapter.semantic_search;

import use_case.semantic_search.SemanticSearchInputBoundary;
import use_case.semantic_search.SemanticSearchInputData;

/** Turns the user's search sentence into input data and starts the Semantic Search use case. */
public class SemanticSearchController {

  private final SemanticSearchInputBoundary interactor;

  /**
   * Creates the controller.
   *
   * @param interactor the use case to start
   */
  public SemanticSearchController(SemanticSearchInputBoundary interactor) {
    this.interactor = interactor;
  }

  /**
   * Searches for stocks matching a sentence.
   *
   * <p>The first search loads the embedding model and may take several seconds, so the view must
   * call this off the Swing Event Dispatch Thread (for example from a {@code SwingWorker}).
   *
   * @param query the sentence, e.g. "best selling oil stocks"
   */
  public void execute(String query) {
    interactor.execute(new SemanticSearchInputData(query));
  }
}
