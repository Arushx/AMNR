package entity;

import java.time.LocalDate;

/**
 * One trading day of prices for a stock: open, high, low, close and volume.
 *
 * <p>Instances are immutable: once created, a price point never changes.
 */
public class PricePoint {

  private final LocalDate date;
  private final double open;
  private final double high;
  private final double low;
  private final double close;
  private final long volume;

  /**
   * Creates a price point.
   *
   * @param date the trading day
   * @param open the first traded price of the day
   * @param high the highest price of the day
   * @param low the lowest price of the day
   * @param close the last traded price of the day
   * @param volume the number of shares traded that day
   * @throws IllegalArgumentException if the date is null, a price is not positive, the low is
   *     above the high, or the volume is negative
   */
  public PricePoint(
      LocalDate date, double open, double high, double low, double close, long volume) {
    if (date == null) {
      throw new IllegalArgumentException("date must not be null");
    }
    if (open <= 0 || high <= 0 || low <= 0 || close <= 0) {
      throw new IllegalArgumentException("prices must be positive");
    }
    if (low > high) {
      throw new IllegalArgumentException("low must not be above high");
    }
    if (volume < 0) {
      throw new IllegalArgumentException("volume must not be negative");
    }
    this.date = date;
    this.open = open;
    this.high = high;
    this.low = low;
    this.close = close;
    this.volume = volume;
  }

  public LocalDate getDate() {
    return date;
  }

  public double getOpen() {
    return open;
  }

  public double getHigh() {
    return high;
  }

  public double getLow() {
    return low;
  }

  public double getClose() {
    return close;
  }

  public long getVolume() {
    return volume;
  }
}
