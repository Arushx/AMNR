package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PriceHistoryTest {

  private static PricePoint day(int dayOfMonth, double close) {
    return new PricePoint(LocalDate.of(2026, 11, dayOfMonth), close, close, close, close, 100);
  }

  @Test
  void sortsPointsOldestFirst() {
    PriceHistory history = new PriceHistory("ibm", List.of(day(13, 3), day(11, 1), day(12, 2)));

    assertEquals("IBM", history.getSymbol());
    assertEquals(LocalDate.of(2026, 11, 11), history.getPoints().get(0).getDate());
    assertEquals(LocalDate.of(2026, 11, 13), history.getLatest().getDate());
  }

  @Test
  void rejectsDuplicateDates() {
    assertThrows(IllegalArgumentException.class,
        () -> new PriceHistory("IBM", List.of(day(11, 1), day(11, 2))));
  }

  @Test
  void rejectsBlankSymbol() {
    assertThrows(IllegalArgumentException.class, () -> new PriceHistory(" ", List.of()));
  }

  @Test
  void pointsCannotBeModified() {
    PriceHistory history = new PriceHistory("IBM", List.of(day(11, 1)));

    assertThrows(UnsupportedOperationException.class,
        () -> history.getPoints().add(day(12, 2)));
  }

  @Test
  void betweenKeepsOnlyDaysInRangeInclusive() {
    PriceHistory history =
        new PriceHistory("IBM", List.of(day(10, 1), day(11, 2), day(12, 3), day(13, 4)));

    PriceHistory middle =
        history.between(LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 12));

    assertEquals(2, middle.getPoints().size());
    assertEquals(LocalDate.of(2026, 11, 11), middle.getPoints().get(0).getDate());
    assertEquals(LocalDate.of(2026, 11, 12), middle.getLatest().getDate());
  }

  @Test
  void betweenRejectsStartAfterEnd() {
    PriceHistory history = new PriceHistory("IBM", List.of(day(11, 1)));

    assertThrows(IllegalArgumentException.class,
        () -> history.between(LocalDate.of(2026, 11, 12), LocalDate.of(2026, 11, 11)));
  }

  @Test
  void findsPeakAndDip() {
    PriceHistory history = new PriceHistory("IBM", List.of(day(11, 5), day(12, 9), day(13, 2)));

    assertEquals(LocalDate.of(2026, 11, 12), history.getHighestClose().getDate());
    assertEquals(LocalDate.of(2026, 11, 13), history.getLowestClose().getDate());
  }

  @Test
  void emptyHistoryHasNoLatestPeakOrDip() {
    PriceHistory empty = new PriceHistory("IBM", List.of());

    assertTrue(empty.isEmpty());
    assertThrows(IllegalStateException.class, empty::getLatest);
    assertThrows(IllegalStateException.class, empty::getHighestClose);
    assertThrows(IllegalStateException.class, empty::getLowestClose);
  }
}
