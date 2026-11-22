package use_case.search_stock;

/** What the user searched for, exactly as typed: a ticker ("aapl") or a company name ("apple"). */
public class SearchStockInputData {

  private final String query;

  /**
   * Creates the input data.
   *
   * @param query the text the user entered
   */
  public SearchStockInputData(String query) {
    this.query = query;
  }

  public String getQuery() {
    return query;
  }
}
