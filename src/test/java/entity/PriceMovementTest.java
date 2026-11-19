package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PriceMovementTest {

  private static final LocalDate DAY1 = LocalDate.of(2026, 3, 2);

  /** Builds a history with one close per day, starting on DAY1. */
  private static PriceHistory closes(double... closes) {
    List<PricePoint> points = new ArrayList<>();
    for (int i = 0; i < closes.length; i++) {
      points.add(new PricePoint(DAY1.plusDays(i), closes[i], closes[i], closes[i], closes[i], 100));
    }
    return new PriceHistory("ACME", points);
  }

  @Test
  void measuresADrop() {
    PriceMovement move = PriceMovement.of(closes(100, 98, 90, 91, 88));

    assertEquals("ACME", move.getSymbol());
    assertEquals(DAY1, move.getStart());
    assertEquals(DAY1.plusDays(4), move.getEnd());
    assertEquals(100, move.getStartClose());
    assertEquals(88, move.getEndClose());
    assertEquals(-12.0, move.getChangePercent(), 1e-9);
    assertEquals(Direction.DOWN, move.getDirection());
  }

  @Test
  void biggestDaysFollowTheOverallDirection() {
    // Daily moves: -2%, -8.2%, +1.1%, -3.3%. The +1.1% day is ignored for a drop.
    PriceMovement move = PriceMovement.of(closes(100, 98, 90, 91, 88));

    assertEquals(
        List.of(DAY1.plusDays(2), DAY1.plusDays(4), DAY1.plusDays(1)), move.getBiggestMoveDays());
  }

  @Test
  void measuresARise() {
    PriceMovement move = PriceMovement.of(closes(50, 55));

    assertEquals(10.0, move.getChangePercent(), 1e-9);
    assertEquals(Direction.UP, move.getDirection());
    assertEquals(List.of(DAY1.plusDays(1)), move.getBiggestMoveDays());
  }

  @Test
  void smallChangeIsFlatAndUsesMovesEitherWay() {
    // Ends 0.5% higher; biggest daily move is the -5% day.
    PriceMovement move = PriceMovement.of(closes(100, 95, 100.5));

    assertEquals(Direction.FLAT, move.getDirection());
    assertEquals(DAY1.plusDays(2), move.getBiggestMoveDays().get(0));
    assertEquals(DAY1.plusDays(1), move.getBiggestMoveDays().get(1));
  }

  @Test
  void dropWithNoDownDaysStillGivesDays() {
    // Can't happen with real closes, but the rule falls back to moves in any direction.
    PriceMovement move = PriceMovement.of(closes(100, 100, 100));

    assertEquals(Direction.FLAT, move.getDirection());
    assertEquals(2, move.getBiggestMoveDays().size());
  }

  @Test
  void needsTwoDays() {
    assertThrows(IllegalArgumentException.class, () -> PriceMovement.of(closes(100)));
    assertThrows(IllegalArgumentException.class, () -> PriceMovement.of(closes()));
  }
}
