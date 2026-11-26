package use_case.semantic_search;

/** Starts a semantic search. Implemented by {@link SemanticSearchInteractor}. */
public interface SemanticSearchInputBoundary {

  /**
   * Searches for stocks matching a sentence.
   *
   * @param inputData what the user typed
   */
  void execute(SemanticSearchInputData inputData);
}
