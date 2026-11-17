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
   * @param ticker the text the user typed in the search box
   */
  public void execute(String ticker) {
    interactor.execute(new SearchStockInputData(ticker));
  }
}
