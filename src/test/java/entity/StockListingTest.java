package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class StockListingTest {

  @Test
  void normalisesValues() {
    StockListing listing = new StockListing(" aapl ", " Apple Inc ", "NASDAQ");

    assertEquals("AAPL", listing.getSymbol());
    assertEquals("Apple Inc", listing.getName());
    assertEquals("NASDAQ", listing.getExchange());
  }

  @Test
  void missingNameBecomesEmpty() {
    assertEquals("", new StockListing("ACFN", null, "NASDAQ").getName());
  }

  @Test
  void rejectsBlankSymbol() {
    assertThrows(IllegalArgumentException.class, () -> new StockListing(" ", "x", "y"));
  }
}
