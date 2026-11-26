package use_case.semantic_search;

/** Receives the result of a semantic search. Implemented by the presenter. */
public interface SemanticSearchOutputBoundary {

  /**
   * Shows the matching stocks.
   *
   * @param outputData the matches and how they were found
   */
  void prepareSuccessView(SemanticSearchOutputData outputData);

  /**
   * Shows why the search failed.
   *
   * @param errorMessage a message that is safe to show to the user
   */
  void prepareFailView(String errorMessage);
}
