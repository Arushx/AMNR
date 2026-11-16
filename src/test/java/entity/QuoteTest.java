package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class QuoteTest {

  private static final LocalDate DAY = LocalDate.of(2026, 11, 13);

  @Test
  void storesValuesAndUppercasesSymbol() {
    Quote quote = new Quote("ibm", 247.13, 2.46, 1.0054, DAY);

    assertEquals("IBM", quote.getSymbol());
    assertEquals(247.13, quote.getPrice());
    assertEquals(2.46, quote.getChange());
    assertEquals(1.0054, quote.getChangePercent());
    assertEquals(DAY, quote.getLatestTradingDay());
  }

  @Test
  void allowsNegativeChange() {
    Quote quote = new Quote("IBM", 240.00, -4.67, -1.9087, DAY);

    assertEquals(-4.67, quote.getChange());
  }

  @Test
  void rejectsBlankSymbol() {
    assertThrows(IllegalArgumentException.class, () -> new Quote("", 1, 0, 0, DAY));
  }

  @Test
  void rejectsNonPositivePrice() {
    assertThrows(IllegalArgumentException.class, () -> new Quote("IBM", 0, 0, 0, DAY));
  }

  @Test
  void rejectsMissingDay() {
    assertThrows(IllegalArgumentException.class, () -> new Quote("IBM", 1, 0, 0, null));
  }
}
