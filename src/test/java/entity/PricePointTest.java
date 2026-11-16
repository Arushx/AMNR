package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PricePointTest {

  private static final LocalDate DAY = LocalDate.of(2026, 11, 13);

  @Test
  void storesAllValues() {
    PricePoint point = new PricePoint(DAY, 246.63, 247.85, 244.83, 247.13, 2669242);

    assertEquals(DAY, point.getDate());
    assertEquals(246.63, point.getOpen());
    assertEquals(247.85, point.getHigh());
    assertEquals(244.83, point.getLow());
    assertEquals(247.13, point.getClose());
    assertEquals(2669242, point.getVolume());
  }

  @Test
  void rejectsMissingDate() {
    assertThrows(IllegalArgumentException.class,
        () -> new PricePoint(null, 1, 2, 1, 1, 0));
  }

  @Test
  void rejectsNonPositivePrice() {
    assertThrows(IllegalArgumentException.class,
        () -> new PricePoint(DAY, 0, 2, 1, 1, 0));
  }

  @Test
  void rejectsLowAboveHigh() {
    assertThrows(IllegalArgumentException.class,
        () -> new PricePoint(DAY, 5, 4, 6, 5, 0));
  }

  @Test
  void rejectsNegativeVolume() {
    assertThrows(IllegalArgumentException.class,
        () -> new PricePoint(DAY, 1, 2, 1, 1, -1));
  }
}
