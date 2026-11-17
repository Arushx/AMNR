package use_case.search_stock;

import entity.PriceHistory;
import entity.PricePoint;
import entity.Quote;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import use_case.PriceHistoryDataAccessInterface;
import use_case.QuoteDataAccessInterface;
import use_case.StockDataUnavailableException;

/**
 * The Search Stock use case: find a stock by ticker and get its latest quote and recent history.
 *
 * <p>It only knows the data access interfaces, never where the data comes from.
 */
public class SearchStockInteractor implements SearchStockInputBoundary {

  /** Letters, digits, '.' or '-', 1 to 10 characters (e.g. "AAPL", "BRK.B", "RDS-A"). */
  private static final String TICKER_PATTERN = "[A-Z0-9.\\-]{1,10}";

  private final QuoteDataAccessInterface quoteDataAccess;
  private final PriceHistoryDataAccessInterface historyDataAccess;
  private final SearchStockOutputBoundary presenter;

  /**
   * Creates the interactor.
   *
   * @param quoteDataAccess provides the latest quote
   * @param historyDataAccess provides the daily price history
   * @param presenter receives the result
   */
  public SearchStockInteractor(
      QuoteDataAccessInterface quoteDataAccess,
      PriceHistoryDataAccessInterface historyDataAccess,
      SearchStockOutputBoundary presenter) {
    this.quoteDataAccess = quoteDataAccess;
    this.historyDataAccess = historyDataAccess;
    this.presenter = presenter;
  }

  @Override
  public void execute(SearchStockInputData inputData) {
    final String raw = inputData.getTicker();
    final String ticker = raw == null ? "" : raw.trim().toUpperCase();

    if (ticker.isEmpty()) {
      presenter.prepareFailView("Please enter a ticker symbol.");
      return;
    }
    if (!ticker.matches(TICKER_PATTERN)) {
      presenter.prepareFailView(
          "\"" + raw.trim() + "\" isn't a valid ticker. Use letters, numbers, '.' or '-'.");
      return;
    }

    final Quote quote;
    final PriceHistory history;
    try {
      quote = quoteDataAccess.getQuote(ticker);
      history = historyDataAccess.getDailyHistory(ticker);
    } catch (StockDataUnavailableException e) {
      presenter.prepareFailView(e.getMessage());
      return;
    }

    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> closes = new ArrayList<>();
    for (PricePoint point : history.getPoints()) {
      dates.add(point.getDate());
      closes.add(point.getClose());
    }

    presenter.prepareSuccessView(
        new SearchStockOutputData(
            quote.getSymbol(),
            quote.getPrice(),
            quote.getChange(),
            quote.getChangePercent(),
            quote.getLatestTradingDay(),
            dates,
            closes));
  }
}
