package use_case.search_stock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import entity.PriceHistory;
import entity.PricePoint;
import entity.Quote;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import use_case.PriceHistoryDataAccessInterface;
import use_case.QuoteDataAccessInterface;
import use_case.StockDataUnavailableException;

/** Unit tests with a fake data source and a fake presenter: no network, no Swing. */
class SearchStockInteractorTest {

  private static final LocalDate DAY = LocalDate.of(2026, 11, 13);

  /** Knows one stock, IBM, and records which symbols were asked for. */
  private static class FakeStockData
      implements QuoteDataAccessInterface, PriceHistoryDataAccessInterface {
    final List<String> requested = new ArrayList<>();
    boolean unavailable;

    @Override
    public Quote getQuote(String symbol) throws StockDataUnavailableException {
      requested.add(symbol);
      check(symbol);
      return new Quote("IBM", 247.13, 2.46, 1.0054, DAY);
    }

    @Override
    public PriceHistory getDailyHistory(String symbol) throws StockDataUnavailableException {
      requested.add(symbol);
      check(symbol);
      return new PriceHistory(
          "IBM",
          List.of(
              new PricePoint(DAY, 246.63, 247.85, 244.83, 247.13, 100),
              new PricePoint(DAY.minusDays(1), 248.30, 248.77, 244.37, 244.67, 100)));
    }

    private void check(String symbol) throws StockDataUnavailableException {
      if (unavailable) {
        throw new StockDataUnavailableException("daily request limit reached");
      }
      if (!symbol.equals("IBM")) {
        throw new StockDataUnavailableException("No quote found for that symbol");
      }
    }
  }

  /** Remembers what the interactor reported. */
  private static class RecordingPresenter implements SearchStockOutputBoundary {
    SearchStockOutputData success;
    String error;

    @Override
    public void prepareSuccessView(SearchStockOutputData outputData) {
      success = outputData;
    }

    @Override
    public void prepareFailView(String errorMessage) {
      error = errorMessage;
    }
  }

  private final FakeStockData data = new FakeStockData();
  private final RecordingPresenter presenter = new RecordingPresenter();
  private final SearchStockInteractor interactor =
      new SearchStockInteractor(data, data, presenter);

  @Test
  void successReturnsQuoteAndHistoryOldestFirst() {
    interactor.execute(new SearchStockInputData("IBM"));

    if (presenter.error != null) {
      fail("Unexpected failure: " + presenter.error);
    }
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
  void tickerIsTrimmedAndUppercasedBeforeLookup() {
    interactor.execute(new SearchStockInputData("  ibm "));

    assertEquals(List.of("IBM", "IBM"), data.requested);
    assertEquals("IBM", presenter.success.getSymbol());
  }

  @Test
  void blankTickerFailsWithoutCallingTheApi() {
    interactor.execute(new SearchStockInputData("   "));

    assertEquals("Please enter a ticker symbol.", presenter.error);
    assertTrue(data.requested.isEmpty());
  }

  @Test
  void nullTickerFailsWithoutCallingTheApi() {
    interactor.execute(new SearchStockInputData(null));

    assertEquals("Please enter a ticker symbol.", presenter.error);
    assertTrue(data.requested.isEmpty());
  }

  @Test
  void invalidCharactersFailWithoutCallingTheApi() {
    interactor.execute(new SearchStockInputData("AP PL!"));

    assertTrue(presenter.error.contains("isn't a valid ticker"));
    assertTrue(data.requested.isEmpty());
  }

  @Test
  void unknownTickerReportsTheDataError() {
    interactor.execute(new SearchStockInputData("XYZ"));

    assertEquals("No quote found for that symbol", presenter.error);
    assertNull(presenter.success);
  }

  @Test
  void unavailableDataReportsTheDataError() {
    data.unavailable = true;

    interactor.execute(new SearchStockInputData("IBM"));

    assertEquals("daily request limit reached", presenter.error);
    assertNull(presenter.success);
  }
}
