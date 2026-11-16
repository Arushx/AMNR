package entity;

import java.time.LocalDate;

/**
 * The latest available price of a stock and how much it moved since the previous close.
 *
 * <p>Instances are immutable.
 */
public class Quote {

  private final String symbol;
  private final double price;
  private final double change;
  private final double changePercent;
  private final LocalDate latestTradingDay;

  /**
   * Creates a quote.
   *
   * @param symbol the stock's ticker symbol, e.g. "IBM"
   * @param price the latest price
   * @param change the price minus the previous close (negative if the price fell)
   * @param changePercent the change as a percentage of the previous close, e.g. 1.0054 for +1.0054%
   * @param latestTradingDay the trading day the price is from
   * @throws IllegalArgumentException if the symbol is blank, the price is not positive, or the day
   *     is null
   */
  public Quote(
      String symbol,
      double price,
      double change,
      double changePercent,
      LocalDate latestTradingDay) {
    if (symbol == null || symbol.isBlank()) {
      throw new IllegalArgumentException("symbol must not be blank");
    }
    if (price <= 0) {
      throw new IllegalArgumentException("price must be positive");
    }
    if (latestTradingDay == null) {
      throw new IllegalArgumentException("latestTradingDay must not be null");
    }
    this.symbol = symbol.toUpperCase();
    this.price = price;
    this.change = change;
    this.changePercent = changePercent;
    this.latestTradingDay = latestTradingDay;
  }

  public String getSymbol() {
    return symbol;
  }

  public double getPrice() {
    return price;
  }

  public double getChange() {
    return change;
  }

  public double getChangePercent() {
    return changePercent;
  }

  public LocalDate getLatestTradingDay() {
    return latestTradingDay;
  }
}
