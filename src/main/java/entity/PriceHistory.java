package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The daily price history of one stock, ordered from the oldest day to the newest.
 *
 * <p>Instances are immutable. Methods that narrow the history return a new {@code PriceHistory}.
 */
public class PriceHistory {

  private final String symbol;
  private final List<PricePoint> points;

  /**
   * Creates a price history. The points may be given in any order; they are stored sorted by date.
   *
   * @param symbol the stock's ticker symbol, e.g. "IBM"
   * @param points the daily prices; may be empty
   * @throws IllegalArgumentException if the symbol is blank, the points are null, or two points
   *     share the same date
   */
  public PriceHistory(String symbol, List<PricePoint> points) {
    if (symbol == null || symbol.isBlank()) {
      throw new IllegalArgumentException("symbol must not be blank");
    }
    if (points == null) {
      throw new IllegalArgumentException("points must not be null");
    }
    final List<PricePoint> sorted = new ArrayList<>(points);
    sorted.sort(Comparator.comparing(PricePoint::getDate));
    for (int i = 1; i < sorted.size(); i++) {
      if (sorted.get(i).getDate().equals(sorted.get(i - 1).getDate())) {
        throw new IllegalArgumentException("duplicate date: " + sorted.get(i).getDate());
      }
    }
    this.symbol = symbol.toUpperCase();
    this.points = Collections.unmodifiableList(sorted);
  }

  public String getSymbol() {
    return symbol;
  }

  /**
   * Returns the daily prices, oldest first.
   *
   * @return an unmodifiable list of price points
   */
  public List<PricePoint> getPoints() {
    return points;
  }

  public boolean isEmpty() {
    return points.isEmpty();
  }

  /**
   * Returns only the days from {@code start} to {@code end}, both included.
   *
   * @param start the first day to keep
   * @param end the last day to keep
   * @return a new history containing the days in the range (empty if none match)
   * @throws IllegalArgumentException if start is after end
   */
  public PriceHistory between(LocalDate start, LocalDate end) {
    if (start.isAfter(end)) {
      throw new IllegalArgumentException("start must not be after end");
    }
    final List<PricePoint> inRange = new ArrayList<>();
    for (PricePoint point : points) {
      if (!point.getDate().isBefore(start) && !point.getDate().isAfter(end)) {
        inRange.add(point);
      }
    }
    return new PriceHistory(symbol, inRange);
  }

  /**
   * Returns the most recent day in the history.
   *
   * @return the newest price point
   * @throws IllegalStateException if the history is empty
   */
  public PricePoint getLatest() {
    requireNotEmpty();
    return points.get(points.size() - 1);
  }

  /**
   * Returns the day with the highest closing price (the peak).
   *
   * @return the price point with the highest close; the earliest one if tied
   * @throws IllegalStateException if the history is empty
   */
  public PricePoint getHighestClose() {
    requireNotEmpty();
    PricePoint highest = points.get(0);
    for (PricePoint point : points) {
      if (point.getClose() > highest.getClose()) {
        highest = point;
      }
    }
    return highest;
  }

  /**
   * Returns the day with the lowest closing price (the dip).
   *
   * @return the price point with the lowest close; the earliest one if tied
   * @throws IllegalStateException if the history is empty
   */
  public PricePoint getLowestClose() {
    requireNotEmpty();
    PricePoint lowest = points.get(0);
    for (PricePoint point : points) {
      if (point.getClose() < lowest.getClose()) {
        lowest = point;
      }
    }
    return lowest;
  }

  private void requireNotEmpty() {
    if (points.isEmpty()) {
      throw new IllegalStateException("price history for " + symbol + " is empty");
    }
  }
}
