package use_case.pairanalysis;

import entity.PriceHistory;
import entity.StockDirectory;
import entity.StockListing;
import entity.backtesting.analysis.Pair;
import use_case.PriceHistoryDataAccessInterface;
import use_case.StockDataUnavailableException;
import use_case.StockListingDataAccessInterface;

/**
 * Computes the correlation of daily returns for two stocks over their fetched price histories.
 *
 * <p>Before fetching any prices it checks both symbols against the list of known stocks, so a typo
 * costs no API request and the user is told exactly which symbol is wrong.
 *
 * <p>Depends only on abstractions: it gets data through {@link StockListingDataAccessInterface}
 * and {@link PriceHistoryDataAccessInterface} and reports through {@link
 * PairAnalysisOutputBoundary}, so it never knows where the data comes from or how the result is
 * displayed.
 */
public class PairAnalysisInteractor implements PairAnalysisInputBoundary {

  private final StockListingDataAccessInterface listingDataAccess;
  private final PriceHistoryDataAccessInterface priceHistoryDataAccess;
  private final PairAnalysisOutputBoundary presenter;
  private StockDirectory directory;

  /**
   * Creates the interactor.
   *
   * @param listingDataAccess provides every known stock, used to check the symbols exist
   * @param priceHistoryDataAccess provides each stock's daily price history
   * @param presenter receives the result
   */
  public PairAnalysisInteractor(
      StockListingDataAccessInterface listingDataAccess,
      PriceHistoryDataAccessInterface priceHistoryDataAccess,
      PairAnalysisOutputBoundary presenter) {
    this.listingDataAccess = listingDataAccess;
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
      final StockDirectory stocks = getDirectory();
      final StockListing firstListing = stocks.findBySymbol(firstTicker);
      final StockListing secondListing = stocks.findBySymbol(secondTicker);
      if (firstListing == null || secondListing == null) {
        presenter.prepareUnknownSymbolView(
            new PairAnalysisUnknownSymbolOutputData(
                firstTicker, secondTicker, firstListing == null, secondListing == null));
        return;
      }

      final PriceHistory firstHistory = priceHistoryDataAccess.getDailyHistory(firstTicker);
      final PriceHistory secondHistory = priceHistoryDataAccess.getDailyHistory(secondTicker);
      final Pair pair = new Pair(firstHistory, secondHistory);

      final double correlation = pair.getReturnsCorrelation();
      presenter.prepareSuccessView(
          new PairAnalysisOutputData(
              pair.getFirstTicker(),
              firstListing.getName(),
              pair.getSecondTicker(),
              secondListing.getName(),
              correlation,
              pair.getSharedReturnCount()));
    } catch (StockDataUnavailableException e) {
      presenter.prepareFailView(e.getMessage());
    } catch (IllegalStateException e) {
      presenter.prepareFailView(
          "Cannot compare " + firstTicker + " and " + secondTicker + ": " + e.getMessage());
    }
  }

  /** Loads the list of known stocks on first use and keeps it for later requests. */
  private StockDirectory getDirectory() throws StockDataUnavailableException {
    if (directory == null) {
      directory = new StockDirectory(listingDataAccess.getListings());
    }
    return directory;
  }

  private static String normalize(String ticker) {
    if (ticker == null) {
      return "";
    }
    return ticker.trim().toUpperCase();
  }
}
