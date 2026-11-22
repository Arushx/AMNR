package use_case.pairanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.PriceHistory;
import entity.PricePoint;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.PriceHistoryDataAccessInterface;
import use_case.StockDataUnavailableException;

class PairAnalysisInteractorTest {

  private static final double DELTA = 1e-9;

  private FakePriceHistoryDataAccess dataAccess;
  private RecordingPresenter presenter;
  private PairAnalysisInteractor interactor;

  @BeforeEach
  void setUp() {
    dataAccess = new FakePriceHistoryDataAccess();
    presenter = new RecordingPresenter();
    interactor = new PairAnalysisInteractor(dataAccess, presenter);
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
  void successReportsTickersCorrelationAndCount() {
    dataAccess.put(history("KO", 10, 11, 10.5, 12));
    dataAccess.put(history("PEP", 20, 22, 21, 24));

    interactor.execute(new PairAnalysisInputData(" ko ", "pep"));

    assertNull(presenter.error);
    assertNotNull(presenter.output);
    assertEquals("KO", presenter.output.getFirstTicker());
    assertEquals("PEP", presenter.output.getSecondTicker());
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
  void unavailableDataFailsWithItsMessage() {
    dataAccess.put(history("KO", 10, 11, 12));

    interactor.execute(new PairAnalysisInputData("KO", "NOPE"));

    assertEquals("no data for NOPE", presenter.error);
    assertNull(presenter.output);
  }

  @Test
  void tooFewSharedDatesFails() {
    dataAccess.put(history("KO", 10, 11));
    dataAccess.put(history("PEP", 20, 22));

    interactor.execute(new PairAnalysisInputData("KO", "PEP"));

    assertNull(presenter.output);
    assertTrue(presenter.error.startsWith("Cannot compare KO and PEP"));
  }

  /** In-memory stand-in for the Alpha Vantage data access object. */
  private static class FakePriceHistoryDataAccess implements PriceHistoryDataAccessInterface {
    private final Map<String, PriceHistory> histories = new HashMap<>();

    void put(PriceHistory history) {
      histories.put(history.getSymbol(), history);
    }

    @Override
    public PriceHistory getDailyHistory(String symbol) throws StockDataUnavailableException {
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
    private String error;

    @Override
    public void prepareSuccessView(PairAnalysisOutputData outputData) {
      output = outputData;
    }

    @Override
    public void prepareFailView(String errorMessage) {
      error = errorMessage;
    }
  }
}
