package use_case.search_stock;

/** Starts the Search Stock use case. Implemented by the interactor, called by the controller. */
public interface SearchStockInputBoundary {

  /**
   * Searches for a stock and reports the result through the output boundary.
   *
   * @param inputData what the user searched for
   */
  void execute(SearchStockInputData inputData);
}
