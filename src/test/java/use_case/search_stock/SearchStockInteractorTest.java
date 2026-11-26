package use_case.search_stock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.PriceHistory;
import entity.PricePoint;
import entity.Quote;
import entity.StockListing;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import use_case.PriceHistoryDataAccessInterface;
import use_case.QuoteDataAccessInterface;
import use_case.RelatedStockFinder;
import use_case.StockDataUnavailableException;
import use_case.StockListingDataAccessInterface;

/** Unit tests with fake data sources and a fake presenter: no network, no files, no Swing. */
class SearchStockInteractorTest {

  private static final LocalDate DAY = LocalDate.of(2026, 11, 13);

  /** A small stock list. */
  private static class FakeListings implements StockListingDataAccessInterface {
    boolean unavailable;

    @Override
    public List<StockListing> getListings() throws StockDataUnavailableException {
      if (unavailable) {
        throw new StockDataUnavailableException("Could not load the stock list");
      }
      return List.of(
          new StockListing("AAPL", "Apple Inc", "NASDAQ"),
          new StockListing("APLE", "Apple Hospitality REIT Inc", "NYSE"),
          new StockListing("PTIV", "Pineapple Financial Inc", "NYSE MKT"),
          new StockListing("IBM", "International Business Machines Corp", "NYSE"),
          new StockListing("A1", "Apple Orchard Co", "X"),
          new StockListing("A2", "Apple Tree Holdings", "X"),
          new StockListing("A3", "Apple Valley Corp", "X"));
    }
  }

  /** Has prices for any symbol except "NOPE", and records which symbols were asked for. */
  private static class FakeStockData
      implements QuoteDataAccessInterface, PriceHistoryDataAccessInterface {
    final List<String> requested = new ArrayList<>();
    boolean unavailable;

    @Override
    public Quote getQuote(String symbol) throws StockDataUnavailableException {
      requested.add(symbol);
      check(symbol);
      return new Quote(symbol, 247.13, 2.46, 1.0054, DAY);
    }

    @Override
    public PriceHistory getDailyHistory(String symbol) throws StockDataUnavailableException {
      requested.add(symbol);
      check(symbol);
      return new PriceHistory(
          symbol,
          List.of(
              new PricePoint(DAY, 246.63, 247.85, 244.83, 247.13, 100),
              new PricePoint(DAY.minusDays(1), 248.30, 248.77, 244.37, 244.67, 100)));
    }

    private void check(String symbol) throws StockDataUnavailableException {
      if (unavailable) {
        throw new StockDataUnavailableException("daily request limit reached");
      }
      if (symbol.equals("NOPE")) {
        throw new StockDataUnavailableException("No quote found for that symbol");
      }
    }
  }

  /** Remembers what the interactor reported. */
  private static class RecordingPresenter implements SearchStockOutputBoundary {
    SearchStockOutputData success;
    SearchStockMatchesOutputData matches;
    String error;

    @Override
    public void prepareSuccessView(SearchStockOutputData outputData) {
      success = outputData;
    }

    @Override
    public void prepareMatchesView(SearchStockMatchesOutputData outputData) {
      matches = outputData;
    }

    @Override
    public void prepareFailView(String errorMessage) {
      error = errorMessage;
    }
  }

  private final FakeListings listings = new FakeListings();
  private final FakeStockData data = new FakeStockData();
  private final RecordingPresenter presenter = new RecordingPresenter();
  private final SearchStockInteractor interactor =
      new SearchStockInteractor(listings, data, data, presenter);

  private void search(String query) {
    interactor.execute(new SearchStockInputData(query));
  }

  @Test
  void knownTickerShowsQuoteAndHistoryOldestFirst() {
    search("IBM");

    assertNull(presenter.error);
    SearchStockOutputData out = presenter.success;
    assertEquals("IBM", out.getSymbol());
    assertEquals(247.13, out.getPrice());
    assertEquals(2.46, out.getChange());
    assertEquals(1.0054, out.getChangePercent());
    assertEquals(DAY, out.getLatestTradingDay());
    assertEquals(List.of(DAY.minusDays(1), DAY), out.getDates());
    assertEquals(List.of(244.67, 247.13), out.getCloses());
  }

  @Test
  void tickerIsTrimmedAndUppercased() {
    search("  aapl ");

    assertEquals(List.of("AAPL", "AAPL"), data.requested);
    assertEquals("AAPL", presenter.success.getSymbol());
  }

  @Test
  void companyNameShowsTopFiveMatchesBestFirstWithoutCallingTheApi() {
    search("apple");

    assertNull(presenter.success);
    SearchStockMatchesOutputData out = presenter.matches;
    assertEquals("apple", out.getQuery());
    assertEquals(SearchStockInteractor.MAX_MATCHES, out.getSymbols().size());
    assertEquals("AAPL", out.getSymbols().get(0));
    assertEquals("Apple Inc", out.getNames().get(0));
    assertEquals("NASDAQ", out.getExchanges().get(0));
    assertTrue(data.requested.isEmpty());
  }

  @Test
  void pickingAMatchSearchesItsTicker() {
    search("apple");
    String picked = presenter.matches.getSymbols().get(0);

    search(picked);

    assertEquals("AAPL", presenter.success.getSymbol());
  }

  @Test
  void nothingMatchingAndNotATickerFails() {
    search("no such company");

    assertEquals("No stocks match \"no such company\".", presenter.error);
    assertTrue(data.requested.isEmpty());
  }

  @Test
  void tickerNotInListWithNoNameMatchIsStillTried() {
    search("SHOP.TO");

    assertEquals("SHOP.TO", presenter.success.getSymbol());
  }

  @Test
  void unknownTickerReportsTheDataError() {
    search("NOPE");

    assertEquals("No quote found for that symbol", presenter.error);
    assertNull(presenter.success);
  }

  @Test
  void blankSearchFailsWithoutCallingTheApi() {
    search("   ");

    assertEquals("Please enter a ticker symbol or company name.", presenter.error);
    assertTrue(data.requested.isEmpty());
  }

  @Test
  void nullSearchFailsWithoutCallingTheApi() {
    search(null);

    assertEquals("Please enter a ticker symbol or company name.", presenter.error);
    assertTrue(data.requested.isEmpty());
  }

  @Test
  void withoutTheStockListATickerIsStillTried() {
    listings.unavailable = true;

    search("IBM");

    assertEquals("IBM", presenter.success.getSymbol());
  }

  @Test
  void withoutTheStockListANameSearchFails() {
    listings.unavailable = true;

    search("apple inc");

    assertEquals("Could not load the stock list", presenter.error);
  }

  @Test
  void unavailablePriceDataReportsTheDataError() {
    data.unavailable = true;

    search("IBM");

    assertEquals("daily request limit reached", presenter.error);
    assertNull(presenter.success);
  }

  /** Suggests fixed stocks for any sentence, or fails, and records what it was asked. */
  private static class FakeRelatedFinder implements RelatedStockFinder {
    final List<String> queries = new ArrayList<>();
    List<StockListing> related =
        List.of(new StockListing("XOM", "Exxon Mobil Corporation", ""),
            new StockListing("CVX", "Chevron Corporation", ""));
    boolean unavailable;

    @Override
    public List<StockListing> findRelated(String query, int limit)
        throws StockDataUnavailableException {
      queries.add(query);
      if (unavailable) {
        throw new StockDataUnavailableException("profiles missing");
      }
      return related;
    }
  }

  private SearchStockInteractor withFinder(FakeRelatedFinder finder) {
    return new SearchStockInteractor(listings, data, data, presenter, finder);
  }

  @Test
  void sentenceWithNoNameMatchOffersRelatedStocks() {
    FakeRelatedFinder finder = new FakeRelatedFinder();

    withFinder(finder).execute(new SearchStockInputData("best selling oil stocks"));

    assertEquals(List.of("best selling oil stocks"), finder.queries);
    assertEquals(List.of("XOM", "CVX"), presenter.matches.getSymbols());
    assertEquals(List.of("Exxon Mobil Corporation", "Chevron Corporation"),
        presenter.matches.getNames());
    assertNull(presenter.error);
    assertTrue(data.requested.isEmpty());
  }

  @Test
  void nameMatchesAreShownWithoutAskingForRelatedStocks() {
    FakeRelatedFinder finder = new FakeRelatedFinder();

    withFinder(finder).execute(new SearchStockInputData("apple"));

    assertTrue(finder.queries.isEmpty());
    assertEquals("AAPL", presenter.matches.getSymbols().get(0));
  }

  @Test
  void noRelatedStocksStillReportsNoMatch() {
    FakeRelatedFinder finder = new FakeRelatedFinder();
    finder.related = List.of();

    withFinder(finder).execute(new SearchStockInputData("no such company"));

    assertEquals("No stocks match \"no such company\".", presenter.error);
  }

  @Test
  void failingRelatedFinderStillReportsNoMatch() {
    FakeRelatedFinder finder = new FakeRelatedFinder();
    finder.unavailable = true;

    withFinder(finder).execute(new SearchStockInputData("no such company"));

    assertEquals("No stocks match \"no such company\".", presenter.error);
  }
}
