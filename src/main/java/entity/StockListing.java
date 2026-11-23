package entity;

/**
 * A stock that can be searched for: its ticker symbol, company name and exchange.
 *
 * <p>Instances are immutable.
 */
public class StockListing {

  private final String symbol;
  private final String name;
  private final String exchange;

  /**
   * Creates a listing.
   *
   * @param symbol the ticker symbol, e.g. "AAPL"
   * @param name the company name, e.g. "Apple Inc"; may be empty if unknown
   * @param exchange where it trades, e.g. "NASDAQ"
   * @throws IllegalArgumentException if the symbol is blank
   */
  public StockListing(String symbol, String name, String exchange) {
    if (symbol == null || symbol.isBlank()) {
      throw new IllegalArgumentException("symbol must not be blank");
    }
    this.symbol = symbol.trim().toUpperCase();
    this.name = name == null ? "" : name.trim();
    this.exchange = exchange == null ? "" : exchange.trim();
  }

  public String getSymbol() {
    return symbol;
  }

  public String getName() {
    return name;
  }

  public String getExchange() {
    return exchange;
  }
}
