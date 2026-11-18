package entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * How a stock's price moved over a period: from the first to the last trading day of a {@link
 * PriceHistory}.
 *
 * <p>Besides the overall change, it keeps the days with the largest daily moves, because news
 * published around those days is the most likely explanation of the move. Instances are immutable.
 */
public class PriceMovement {

  /** A change smaller than this many percent, either way, counts as FLAT. */
  public static final double FLAT_THRESHOLD_PERCENT = 1.0;

  /** How many of the largest daily moves are kept. */
  public static final int BIGGEST_DAYS = 3;

  private final String symbol;
  private final LocalDate start;
  private final LocalDate end;
  private final double startClose;
  private final double endClose;
  private final double changePercent;
  private final Direction direction;
  private final List<LocalDate> biggestMoveDays;

  private PriceMovement(
      String symbol,
      PricePoint first,
      PricePoint last,
      double changePercent,
      Direction direction,
      List<LocalDate> biggestMoveDays) {
    this.symbol = symbol;
    this.start = first.getDate();
    this.end = last.getDate();
    this.startClose = first.getClose();
    this.endClose = last.getClose();
    this.changePercent = changePercent;
    this.direction = direction;
    this.biggestMoveDays = Collections.unmodifiableList(biggestMoveDays);
  }

  /**
   * Works out how the price moved over a whole history.
   *
   * <p>The change is from the first day's close to the last day's close. A daily move is the change
   * from one day's close to the next day's close. The biggest daily moves in the same direction as
   * the overall move are kept (any direction if the overall move is FLAT, or if no day moved that
   * way), largest first.
   *
   * @param history the prices for the period, e.g. {@code fullHistory.between(start, end)}
   * @return the movement
   * @throws IllegalArgumentException if the history has fewer than two days
   */
  public static PriceMovement of(PriceHistory history) {
    final List<PricePoint> points = history.getPoints();
    if (points.size() < 2) {
      throw new IllegalArgumentException("need at least two trading days to measure a move");
    }
    final PricePoint first = points.get(0);
    final PricePoint last = points.get(points.size() - 1);
    final double changePercent = percentChange(first.getClose(), last.getClose());
    final Direction direction = directionOf(changePercent);
    return new PriceMovement(
        history.getSymbol(), first, last, changePercent, direction,
        biggestMoveDays(points, direction));
  }

  private static Direction directionOf(double changePercent) {
    if (Math.abs(changePercent) < FLAT_THRESHOLD_PERCENT) {
      return Direction.FLAT;
    }
    return changePercent > 0 ? Direction.UP : Direction.DOWN;
  }

  private static List<LocalDate> biggestMoveDays(List<PricePoint> points, Direction direction) {
    final List<DailyMove> moves = new ArrayList<>();
    for (int i = 1; i < points.size(); i++) {
      final double change = percentChange(points.get(i - 1).getClose(), points.get(i).getClose());
      moves.add(new DailyMove(points.get(i).getDate(), change));
    }
    List<DailyMove> candidates = new ArrayList<>();
    for (DailyMove move : moves) {
      if (direction == Direction.UP && move.change > 0
          || direction == Direction.DOWN && move.change < 0) {
        candidates.add(move);
      }
    }
    if (candidates.isEmpty()) {
      candidates = moves;
    }
    candidates.sort(
        Comparator.comparingDouble((DailyMove move) -> Math.abs(move.change)).reversed());
    final List<LocalDate> days = new ArrayList<>();
    for (int i = 0; i < Math.min(BIGGEST_DAYS, candidates.size()); i++) {
      days.add(candidates.get(i).date);
    }
    return days;
  }

  /** One day's change from the previous day's close. */
  private static final class DailyMove {
    private final LocalDate date;
    private final double change;

    private DailyMove(LocalDate date, double change) {
      this.date = date;
      this.change = change;
    }
  }

  private static double percentChange(double from, double to) {
    return (to - from) / from * 100.0;
  }

  public String getSymbol() {
    return symbol;
  }

  /**
   * Returns the first trading day of the period.
   *
   * @return the start date
   */
  public LocalDate getStart() {
    return start;
  }

  /**
   * Returns the last trading day of the period.
   *
   * @return the end date
   */
  public LocalDate getEnd() {
    return end;
  }

  public double getStartClose() {
    return startClose;
  }

  public double getEndClose() {
    return endClose;
  }

  /**
   * Returns the change from the first close to the last close, in percent.
   *
   * @return e.g. -12.4 for a 12.4% drop
   */
  public double getChangePercent() {
    return changePercent;
  }

  public Direction getDirection() {
    return direction;
  }

  /**
   * Returns the days with the largest daily moves, largest first.
   *
   * @return an unmodifiable list of at most {@value #BIGGEST_DAYS} dates
   */
  public List<LocalDate> getBiggestMoveDays() {
    return biggestMoveDays;
  }
}
