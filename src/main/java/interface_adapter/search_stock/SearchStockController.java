package interface_adapter.search_stock;

import use_case.search_stock.SearchStockInputBoundary;
import use_case.search_stock.SearchStockInputData;

/** Turns the user's search into input data and starts the Search Stock use case. */
public class SearchStockController {

  private final SearchStockInputBoundary interactor;

  /**
   * Creates the controller.
   *
   * @param interactor the use case to start
   */
  public SearchStockController(SearchStockInputBoundary interactor) {
    this.interactor = interactor;
  }

  /**
   * Searches for a stock.
   *
   * <p>This contacts the stock data API, so the view must call it off the Swing Event Dispatch
   * Thread (for example from a {@code SwingWorker}).
   *
   * <p>Also used when the user picks one of the name matches: the view passes that match's ticker.
   *
   * @param query the text the user typed (a ticker or company name), or a chosen match's ticker
   */
  public void execute(String query) {
    interactor.execute(new SearchStockInputData(query));
  }
}
