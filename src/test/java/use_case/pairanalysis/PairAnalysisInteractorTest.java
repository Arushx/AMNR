package use_case.pairanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.PriceHistory;
import entity.PricePoint;
import entity.StockListing;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.PriceHistoryDataAccessInterface;
import use_case.StockDataUnavailableException;
import use_case.StockListingDataAccessInterface;

class PairAnalysisInteractorTest {

  private static final double DELTA = 1e-9;

  private FakeListingDataAccess listingDataAccess;
  private FakePriceHistoryDataAccess priceDataAccess;
  private RecordingPresenter presenter;
  private PairAnalysisInteractor interactor;

  @BeforeEach
  void setUp() {
    listingDataAccess =
        new FakeListingDataAccess(
            List.of(
                new StockListing("KO", "Coca-Cola Co", "NYSE"),
                new StockListing("PEP", "PepsiCo Inc", "NASDAQ"),
                new StockListing("IBM", "International Business Machines", "NYSE")));
    priceDataAccess = new FakePriceHistoryDataAccess();
    presenter = new RecordingPresenter();
    interactor = new PairAnalysisInteractor(listingDataAccess, priceDataAccess, presenter);
  }

  private static PriceHistory history(String symbol, double... closes) {
    final List<PricePoint> points = new ArrayList<>();
    for (int i = 0; i < closes.length; i++) {
      final double close = closes[i];
      points.add(new PricePoint(LocalDate.of(2026, 11, i + 1), close, close, close, close, 100));
    }
    return new PriceHistory(symbol, points);
  }

  @Test
  void successReportsTickersNamesCorrelationAndCount() {
    priceDataAccess.put(history("KO", 10, 11, 10.5, 12));
    priceDataAccess.put(history("PEP", 20, 22, 21, 24));

    interactor.execute(new PairAnalysisInputData(" ko ", "pep"));

    assertNull(presenter.error);
    assertNotNull(presenter.output);
    assertEquals("KO", presenter.output.getFirstTicker());
    assertEquals("Coca-Cola Co", presenter.output.getFirstName());
    assertEquals("PEP", presenter.output.getSecondTicker());
    assertEquals("PepsiCo Inc", presenter.output.getSecondName());
    assertEquals(1.0, presenter.output.getCorrelation(), DELTA);
    assertEquals(3, presenter.output.getSharedReturnCount());
  }

  @Test
  void blankTickerFails() {
    interactor.execute(new PairAnalysisInputData("KO", "  "));

    assertEquals("Enter two ticker symbols.", presenter.error);
    assertNull(presenter.output);
  }

  @Test
  void nullTickerFails() {
    interactor.execute(new PairAnalysisInputData(null, "KO"));

    assertEquals("Enter two ticker symbols.", presenter.error);
  }

  @Test
  void sameTickerTwiceFails() {
    interactor.execute(new PairAnalysisInputData("KO", "ko"));

    assertEquals("Choose two different stocks.", presenter.error);
    assertNull(presenter.output);
  }

  @Test
  void unknownFirstSymbolIsReportedWithoutFetchingPrices() {
    interactor.execute(new PairAnalysisInputData("zzzz", "KO"));

    assertNotNull(presenter.unknown);
    assertEquals("ZZZZ", presenter.unknown.getFirstSymbol());
    assertTrue(presenter.unknown.isFirstUnknown());
    assertFalse(presenter.unknown.isSecondUnknown());
    assertEquals(0, priceDataAccess.requests);
    assertNull(presenter.output);
    assertNull(presenter.error);
  }

  @Test
  void unknownSecondSymbolIsReported() {
    interactor.execute(new PairAnalysisInputData("KO", "PEPX"));

    assertFalse(presenter.unknown.isFirstUnknown());
    assertTrue(presenter.unknown.isSecondUnknown());
    assertEquals("PEPX", presenter.unknown.getSecondSymbol());
  }

  @Test
  void bothSymbolsUnknownAreReported() {
    interactor.execute(new PairAnalysisInputData("AAAA", "BBBB"));

    assertTrue(presenter.unknown.isFirstUnknown());
    assertTrue(presenter.unknown.isSecondUnknown());
    assertEquals(0, priceDataAccess.requests);
  }

  @Test
  void listingUnavailableFailsWithItsMessage() {
    listingDataAccess.failWith = "Could not load the stock list";

    interactor.execute(new PairAnalysisInputData("KO", "PEP"));

    assertEquals("Could not load the stock list", presenter.error);
    assertEquals(0, priceDataAccess.requests);
  }

  @Test
  void listingIsLoadedOnlyOnce() {
    interactor.execute(new PairAnalysisInputData("KO", "ZZZZ"));
    interactor.execute(new PairAnalysisInputData("PEP", "ZZZZ"));

    assertEquals(1, listingDataAccess.requests);
  }

  @Test
  void unavailablePriceDataFailsWithItsMessage() {
    priceDataAccess.put(history("KO", 10, 11, 12));

    interactor.execute(new PairAnalysisInputData("KO", "IBM"));

    assertEquals("no data for IBM", presenter.error);
    assertNull(presenter.output);
  }

  @Test
  void tooFewSharedDatesFails() {
    priceDataAccess.put(history("KO", 10, 11));
    priceDataAccess.put(history("PEP", 20, 22));

    interactor.execute(new PairAnalysisInputData("KO", "PEP"));

    assertNull(presenter.output);
    assertTrue(presenter.error.startsWith("Cannot compare KO and PEP"));
  }

  /** In-memory stand-in for the stock list file. */
  private static class FakeListingDataAccess implements StockListingDataAccessInterface {
    private final List<StockListing> listings;
    private String failWith;
    private int requests;

    FakeListingDataAccess(List<StockListing> listings) {
      this.listings = listings;
    }

    @Override
    public List<StockListing> getListings() throws StockDataUnavailableException {
      requests++;
      if (failWith != null) {
        throw new StockDataUnavailableException(failWith);
      }
      return listings;
    }
  }

  /** In-memory stand-in for the Alpha Vantage data access object. */
  private static class FakePriceHistoryDataAccess implements PriceHistoryDataAccessInterface {
    private final Map<String, PriceHistory> histories = new HashMap<>();
    private int requests;

    void put(PriceHistory history) {
      histories.put(history.getSymbol(), history);
    }

    @Override
    public PriceHistory getDailyHistory(String symbol) throws StockDataUnavailableException {
      requests++;
      final PriceHistory history = histories.get(symbol);
      if (history == null) {
        throw new StockDataUnavailableException("no data for " + symbol);
      }
      return history;
    }
  }

  /** Remembers what the interactor sent so tests can check it. */
  private static class RecordingPresenter implements PairAnalysisOutputBoundary {
    private PairAnalysisOutputData output;
    private PairAnalysisUnknownSymbolOutputData unknown;
    private String error;

    @Override
    public void prepareSuccessView(PairAnalysisOutputData outputData) {
      output = outputData;
    }

    @Override
    public void prepareUnknownSymbolView(PairAnalysisUnknownSymbolOutputData outputData) {
      unknown = outputData;
    }

    @Override
    public void prepareFailView(String errorMessage) {
      error = errorMessage;
    }
  }
}
