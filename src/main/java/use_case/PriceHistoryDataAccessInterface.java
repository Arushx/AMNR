package use_case;

import entity.PriceHistory;

/** Gives a use case the daily price history of a stock, without saying where it comes from. */
public interface PriceHistoryDataAccessInterface {

  /**
   * Returns the recent daily prices of a stock, oldest day first.
   *
   * @param symbol the ticker symbol, e.g. "IBM"
   * @return the price history
   * @throws StockDataUnavailableException if the history can't be provided
   */
  PriceHistory getDailyHistory(String symbol) throws StockDataUnavailableException;
}
