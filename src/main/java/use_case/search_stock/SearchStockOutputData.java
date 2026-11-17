package use_case.search_stock;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * The result of a successful search: the latest quote and the recent closing prices.
 *
 * <p>Holds plain values rather than entities, so the presenter doesn't depend on the entity layer's
 * classes. The two lists line up: {@code dates.get(i)} is the day of {@code closes.get(i)}, oldest
 * first.
 */
public class SearchStockOutputData {

  private final String symbol;
  private final double price;
  private final double change;
  private final double changePercent;
  private final LocalDate latestTradingDay;
  private final List<LocalDate> dates;
  private final List<Double> closes;

  /**
   * Creates the output data.
   *
   * @param symbol the ticker symbol, e.g. "IBM"
   * @param price the latest price
   * @param change the change since the previous close
   * @param changePercent the change as a percentage, e.g. 1.0054
   * @param latestTradingDay the day the price is from
   * @param dates the trading days of the history, oldest first
   * @param closes the closing price on each of those days
   */
  public SearchStockOutputData(
      String symbol,
      double price,
      double change,
      double changePercent,
      LocalDate latestTradingDay,
      List<LocalDate> dates,
      List<Double> closes) {
    this.symbol = symbol;
    this.price = price;
    this.change = change;
    this.changePercent = changePercent;
    this.latestTradingDay = latestTradingDay;
    this.dates = Collections.unmodifiableList(dates);
    this.closes = Collections.unmodifiableList(closes);
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

  public List<LocalDate> getDates() {
    return dates;
  }

  public List<Double> getCloses() {
    return closes;
  }
}
