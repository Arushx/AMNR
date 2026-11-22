package use_case.pairanalysis;

/** The two ticker symbols the user wants to compare, exactly as entered. Immutable. */
public class PairAnalysisInputData {

  private final String firstTicker;
  private final String secondTicker;

  /**
   * Creates the input data.
   *
   * @param firstTicker the first ticker symbol, e.g. "KO"
   * @param secondTicker the second ticker symbol, e.g. "PEP"
   */
  public PairAnalysisInputData(String firstTicker, String secondTicker) {
    this.firstTicker = firstTicker;
    this.secondTicker = secondTicker;
  }

  public String getFirstTicker() {
    return firstTicker;
  }

  public String getSecondTicker() {
    return secondTicker;
  }
}
