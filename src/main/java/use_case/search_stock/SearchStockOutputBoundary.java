package use_case.search_stock;

/** How the interactor reports the result. Implemented by the presenter. */
public interface SearchStockOutputBoundary {

  /**
   * Shows a successful search.
   *
   * @param outputData the quote and price history found
   */
  void prepareSuccessView(SearchStockOutputData outputData);

  /**
   * Shows the company-name matches so the user can pick one.
   *
   * @param outputData the best matches, best first
   */
  void prepareMatchesView(SearchStockMatchesOutputData outputData);

  /**
   * Shows why the search failed.
   *
   * @param errorMessage a message that can be shown to the user
   */
  void prepareFailView(String errorMessage);
}
