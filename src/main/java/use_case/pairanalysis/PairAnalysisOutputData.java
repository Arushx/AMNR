package use_case.pairanalysis;

/** The result of a successful pair analysis. Immutable. */
public class PairAnalysisOutputData {

  private final String firstTicker;
  private final String firstName;
  private final String secondTicker;
  private final String secondName;
  private final double correlation;
  private final int sharedReturnCount;

  /**
   * Creates the output data.
   *
   * @param firstTicker the first ticker symbol
   * @param firstName the first company's name; may be empty if unknown
   * @param secondTicker the second ticker symbol
   * @param secondName the second company's name; may be empty if unknown
   * @param correlation the Pearson correlation of daily returns, between -1 and 1
   * @param sharedReturnCount how many daily returns the correlation was computed from
   */
  public PairAnalysisOutputData(
      String firstTicker,
      String firstName,
      String secondTicker,
      String secondName,
      double correlation,
      int sharedReturnCount) {
    this.firstTicker = firstTicker;
    this.firstName = firstName;
    this.secondTicker = secondTicker;
    this.secondName = secondName;
    this.correlation = correlation;
    this.sharedReturnCount = sharedReturnCount;
  }

  public String getFirstTicker() {
    return firstTicker;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getSecondTicker() {
    return secondTicker;
  }

  public String getSecondName() {
    return secondName;
  }

  public double getCorrelation() {
    return correlation;
  }

  public int getSharedReturnCount() {
    return sharedReturnCount;
  }
}
