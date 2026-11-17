package use_case.search_stock;

/** What the user asked for: the ticker symbol they typed, exactly as typed. */
public class SearchStockInputData {

  private final String ticker;

  /**
   * Creates the input data.
   *
   * @param ticker the text the user entered, e.g. "aapl" or " IBM "
   */
  public SearchStockInputData(String ticker) {
    this.ticker = ticker;
  }

  public String getTicker() {
    return ticker;
  }
}
