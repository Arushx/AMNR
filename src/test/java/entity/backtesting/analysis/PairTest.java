package entity.backtesting.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import entity.PriceHistory;
import entity.PricePoint;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PairTest {

  private static final double DELTA = 1e-9;

  private static PricePoint day(int dayOfMonth, double close) {
    return new PricePoint(LocalDate.of(2026, 11, dayOfMonth), close, close, close, close, 100);
  }

  /** Builds a history whose closes start on Nov 1 and run on consecutive days. */
  private static PriceHistory history(String symbol, double... closes) {
    final List<PricePoint> points = new ArrayList<>();
    for (int i = 0; i < closes.length; i++) {
      points.add(day(i + 1, closes[i]));
    }
    return new PriceHistory(symbol, points);
  }

  @Test
  void identicalReturnsHaveCorrelationOne() {
    // Second stock is always double the first, so its returns are identical.
    final Pair pair =
        new Pair(history("A", 10, 11, 10.5, 12, 11), history("B", 20, 22, 21, 24, 22));

    assertEquals(1.0, pair.getReturnsCorrelation(), DELTA);
    assertEquals(4, pair.getSharedReturnCount());
  }

  @Test
  void oppositeReturnsHaveCorrelationMinusOne() {
    // Returns: A = +10%, -10%, +10%; B = -10%, +10%, -10%.
    final Pair pair =
        new Pair(history("A", 100, 110, 99, 108.9), history("B", 100, 90, 99, 89.1));

    assertEquals(-1.0, pair.getReturnsCorrelation(), DELTA);
  }

  @Test
  void onlySharedDatesAreUsed() {
    // B is missing Nov 3; A's extra day must be skipped, not paired with B's Nov 4.
    final PriceHistory first = history("A", 10, 11, 50, 12, 13);
    final PriceHistory second =
        new PriceHistory("B", List.of(day(1, 20), day(2, 22), day(4, 24), day(5, 26)));
    final Pair pair = new Pair(first, second);

    // Shared dates: Nov 1, 2, 4, 5 -> 3 returns.
    assertEquals(3, pair.getSharedReturnCount());
    // A returns: 0.1, 12/11-1, 13/12-1 ; B returns: 0.1, 24/22-1, 26/24-1 (same shape).
    assertEquals(1.0, pair.getReturnsCorrelation(), DELTA);
  }

  @Test
  void fewerThanThreeSharedDatesIsRejected() {
    final Pair pair = new Pair(history("A", 10, 11), history("B", 20, 22));

    assertEquals(1, pair.getSharedReturnCount());
    assertThrows(IllegalStateException.class, pair::getReturnsCorrelation);
  }

  @Test
  void constantPricesAreRejected() {
    final Pair pair = new Pair(history("A", 10, 10, 10, 10), history("B", 20, 22, 21, 23));

    assertThrows(IllegalStateException.class, pair::getReturnsCorrelation);
  }

  @Test
  void nullHistoryIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Pair(null, history("B", 1, 2, 3)));
    assertThrows(IllegalArgumentException.class, () -> new Pair(history("A", 1, 2, 3), null));
  }

  @Test
  void exposesTickers() {
    final Pair pair = new Pair(history("ko", 1, 2), history("pep", 1, 2));

    assertEquals("KO", pair.getFirstTicker());
    assertEquals("PEP", pair.getSecondTicker());
  }
}
