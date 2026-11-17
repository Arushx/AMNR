package use_case;

import entity.Quote;

/** Gives a use case the latest quote for a stock, without saying where it comes from. */
public interface QuoteDataAccessInterface {

  /**
   * Returns the latest quote for a stock.
   *
   * @param symbol the ticker symbol, e.g. "IBM"
   * @return the quote
   * @throws StockDataUnavailableException if the quote can't be provided
   */
  Quote getQuote(String symbol) throws StockDataUnavailableException;
}
