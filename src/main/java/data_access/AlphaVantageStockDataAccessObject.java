package data_access;

import entity.PriceHistory;
import entity.Quote;
import use_case.PriceHistoryDataAccessInterface;
import use_case.QuoteDataAccessInterface;
import use_case.StockDataUnavailableException;

/**
 * Provides stock data from Alpha Vantage to the use cases.
 *
 * <p>This is the "Data Access" box of the Clean Architecture engine: it implements the data access
 * interfaces declared in the use case layer, uses {@link AlphaVantageClient} to reach the API (the
 * "Database") and {@link AlphaVantageParser} to build entities, and turns Alpha Vantage errors into
 * {@link StockDataUnavailableException} so use cases never see an Alpha Vantage type.
 */
public class AlphaVantageStockDataAccessObject
    implements QuoteDataAccessInterface, PriceHistoryDataAccessInterface {

  private final AlphaVantageClient client;
  private final AlphaVantageParser parser;

  /**
   * Creates the data access object.
   *
   * @param client sends requests to Alpha Vantage
   * @param parser turns responses into entities
   */
  public AlphaVantageStockDataAccessObject(AlphaVantageClient client, AlphaVantageParser parser) {
    this.client = client;
    this.parser = parser;
  }

  @Override
  public Quote getQuote(String symbol) throws StockDataUnavailableException {
    try {
      return parser.parseQuote(client.fetchGlobalQuote(symbol));
    } catch (AlphaVantageException e) {
      throw new StockDataUnavailableException(
          "Could not get a quote for " + symbol + ": " + e.getMessage());
    }
  }

  @Override
  public PriceHistory getDailyHistory(String symbol) throws StockDataUnavailableException {
    try {
      return parser.parseDailyHistory(client.fetchDailyTimeSeries(symbol));
    } catch (AlphaVantageException e) {
      throw new StockDataUnavailableException(
          "Could not get price history for " + symbol + ": " + e.getMessage());
    }
  }
}
