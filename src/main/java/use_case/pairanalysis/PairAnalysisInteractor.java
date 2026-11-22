package use_case.pairanalysis;

import entity.PriceHistory;
import entity.backtesting.analysis.Pair;
import use_case.PriceHistoryDataAccessInterface;
import use_case.StockDataUnavailableException;

/**
 * Computes the correlation of daily returns for two stocks over their fetched price histories.
 *
 * <p>Depends only on abstractions: it gets prices through {@link PriceHistoryDataAccessInterface}
 * and reports through {@link PairAnalysisOutputBoundary}, so it never knows where the data comes
 * from or how the result is displayed.
 */
public class PairAnalysisInteractor implements PairAnalysisInputBoundary {

  private final PriceHistoryDataAccessInterface priceHistoryDataAccess;
  private final PairAnalysisOutputBoundary presenter;

  /**
   * Creates the interactor.
   *
   * @param priceHistoryDataAccess provides each stock's daily price history
   * @param presenter receives the result
   */
  public PairAnalysisInteractor(
      PriceHistoryDataAccessInterface priceHistoryDataAccess,
      PairAnalysisOutputBoundary presenter) {
    this.priceHistoryDataAccess = priceHistoryDataAccess;
    this.presenter = presenter;
  }

  @Override
  public void execute(PairAnalysisInputData inputData) {
    final String firstTicker = normalize(inputData.getFirstTicker());
    final String secondTicker = normalize(inputData.getSecondTicker());

    if (firstTicker.isEmpty() || secondTicker.isEmpty()) {
      presenter.prepareFailView("Enter two ticker symbols.");
      return;
    }
    if (firstTicker.equals(secondTicker)) {
      presenter.prepareFailView("Choose two different stocks.");
      return;
    }

    try {
      final PriceHistory firstHistory = priceHistoryDataAccess.getDailyHistory(firstTicker);
      final PriceHistory secondHistory = priceHistoryDataAccess.getDailyHistory(secondTicker);
      final Pair pair = new Pair(firstHistory, secondHistory);

      final double correlation = pair.getReturnsCorrelation();
      presenter.prepareSuccessView(
          new PairAnalysisOutputData(
              pair.getFirstTicker(),
              pair.getSecondTicker(),
              correlation,
              pair.getSharedReturnCount()));
    } catch (StockDataUnavailableException e) {
      presenter.prepareFailView(e.getMessage());
    } catch (IllegalStateException e) {
      presenter.prepareFailView(
          "Cannot compare " + firstTicker + " and " + secondTicker + ": " + e.getMessage());
    }
  }

  private static String normalize(String ticker) {
    if (ticker == null) {
      return "";
    }
    return ticker.trim().toUpperCase();
  }
}
