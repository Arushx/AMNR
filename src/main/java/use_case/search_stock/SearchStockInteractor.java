package use_case.search_stock;

import entity.PriceHistory;
import entity.PricePoint;
import entity.Quote;
import entity.StockDirectory;
import entity.StockListing;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import use_case.PriceHistoryDataAccessInterface;
import use_case.QuoteDataAccessInterface;
import use_case.StockDataUnavailableException;
import use_case.StockListingDataAccessInterface;

/**
 * The Search Stock use case: find a stock by ticker or company name and show its latest quote and
 * recent price history.
 *
 * <ul>
 *   <li>A known ticker ("AAPL") goes straight to its quote and history.
 *   <li>Anything else ("apple") is searched as a company name, and the top matches are shown for
 *       the user to pick from. Picking one searches again with its ticker.
 * </ul>
 *
 * <p>It only knows the data access interfaces, never where the data comes from.
 */
public class SearchStockInteractor implements SearchStockInputBoundary {

  /** How many company-name matches to offer. */
  static final int MAX_MATCHES = 5;

  /** Letters, digits, '.' or '-', 1 to 10 characters (e.g. "AAPL", "BRK.B", "RDS-A"). */
  private static final String TICKER_PATTERN = "[A-Z0-9.\\-]{1,10}";

  private final StockListingDataAccessInterface listingDataAccess;
  private final QuoteDataAccessInterface quoteDataAccess;
  private final PriceHistoryDataAccessInterface historyDataAccess;
  private final SearchStockOutputBoundary presenter;

  /**
   * Creates the interactor.
   *
   * @param listingDataAccess provides the list of every stock, for ticker and name lookup
   * @param quoteDataAccess provides the latest quote
   * @param historyDataAccess provides the daily price history
   * @param presenter receives the result
   */
  public SearchStockInteractor(
      StockListingDataAccessInterface listingDataAccess,
      QuoteDataAccessInterface quoteDataAccess,
      PriceHistoryDataAccessInterface historyDataAccess,
      SearchStockOutputBoundary presenter) {
    this.listingDataAccess = listingDataAccess;
    this.quoteDataAccess = quoteDataAccess;
    this.historyDataAccess = historyDataAccess;
    this.presenter = presenter;
  }

  @Override
  public void execute(SearchStockInputData inputData) {
    final String query = inputData.getQuery() == null ? "" : inputData.getQuery().trim();
    if (query.isEmpty()) {
      presenter.prepareFailView("Please enter a ticker symbol or company name.");
      return;
    }
    final String upper = query.toUpperCase();
    final boolean looksLikeTicker = upper.matches(TICKER_PATTERN);

    final StockDirectory directory;
    try {
      directory = new StockDirectory(listingDataAccess.getListings());
    } catch (StockDataUnavailableException e) {
      // Without the stock list we can still try a ticker directly, but not a name search.
      if (looksLikeTicker) {
        showStock(upper);
      } else {
        presenter.prepareFailView(e.getMessage());
      }
      return;
    }

    if (looksLikeTicker && directory.findBySymbol(upper) != null) {
      showStock(upper);
      return;
    }

    final List<StockListing> matches = directory.searchByName(query, MAX_MATCHES);
    if (!matches.isEmpty()) {
      showMatches(query, matches);
    } else if (looksLikeTicker) {
      // Not in our US list and no name matches: it may still be a valid ticker elsewhere.
      showStock(upper);
    } else {
      presenter.prepareFailView("No stocks match \"" + query + "\".");
    }
  }

  /** Gets the quote and history for a ticker and reports them. */
  private void showStock(String ticker) {
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

  /** Reports the name matches for the user to choose from. */
  private void showMatches(String query, List<StockListing> matches) {
    final List<String> symbols = new ArrayList<>();
    final List<String> names = new ArrayList<>();
    final List<String> exchanges = new ArrayList<>();
    for (StockListing match : matches) {
      symbols.add(match.getSymbol());
      names.add(match.getName());
      exchanges.add(match.getExchange());
    }
    presenter.prepareMatchesView(
        new SearchStockMatchesOutputData(query, symbols, names, exchanges));
  }
}
