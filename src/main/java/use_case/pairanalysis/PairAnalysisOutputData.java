package use_case.pairanalysis;

/** The result of a successful pair analysis. Immutable. */
public class PairAnalysisOutputData {

  private final String firstTicker;
  private final String secondTicker;
  private final double correlation;
  private final int sharedReturnCount;

  /**
   * Creates the output data.
   *
   * @param firstTicker the first ticker symbol
   * @param secondTicker the second ticker symbol
   * @param correlation the Pearson correlation of daily returns, between -1 and 1
   * @param sharedReturnCount how many daily returns the correlation was computed from
   */
  public PairAnalysisOutputData(
      String firstTicker, String secondTicker, double correlation, int sharedReturnCount) {
    this.firstTicker = firstTicker;
    this.secondTicker = secondTicker;
    this.correlation = correlation;
    this.sharedReturnCount = sharedReturnCount;
  }

  public String getFirstTicker() {
    return firstTicker;
  }

  public String getSecondTicker() {
    return secondTicker;
  }

  public double getCorrelation() {
    return correlation;
  }

  public int getSharedReturnCount() {
    return sharedReturnCount;
  }
}
