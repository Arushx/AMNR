package use_case.pairanalysis;

/**
 * Says which of the two requested symbols don't match any known stock. At least one of them is
 * unknown. Immutable.
 */
public class PairAnalysisUnknownSymbolOutputData {

  private final String firstSymbol;
  private final String secondSymbol;
  private final boolean firstUnknown;
  private final boolean secondUnknown;

  /**
   * Creates the output data.
   *
   * @param firstSymbol the first symbol, as looked up (trimmed, upper case)
   * @param secondSymbol the second symbol, as looked up (trimmed, upper case)
   * @param firstUnknown whether no stock has the first symbol
   * @param secondUnknown whether no stock has the second symbol
   */
  public PairAnalysisUnknownSymbolOutputData(
      String firstSymbol, String secondSymbol, boolean firstUnknown, boolean secondUnknown) {
    this.firstSymbol = firstSymbol;
    this.secondSymbol = secondSymbol;
    this.firstUnknown = firstUnknown;
    this.secondUnknown = secondUnknown;
  }

  public String getFirstSymbol() {
    return firstSymbol;
  }

  public String getSecondSymbol() {
    return secondSymbol;
  }

  public boolean isFirstUnknown() {
    return firstUnknown;
  }

  public boolean isSecondUnknown() {
    return secondUnknown;
  }
}
