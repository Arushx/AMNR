package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class StockDirectoryTest {

  private final StockDirectory directory =
      new StockDirectory(
          List.of(
              new StockListing("PTIV", "Pineapple Financial Inc", "NYSE MKT"),
              new StockListing("AAPY", "Kurv Yield Premium Strategy Apple (AAPL) ETF", "BATS"),
              new StockListing("APLE", "Apple Hospitality REIT Inc", "NYSE"),
              new StockListing("AAPL", "Apple Inc", "NASDAQ"),
              new StockListing("ACFN", "", "NASDAQ"),
              new StockListing("IBM", "International Business Machines Corp", "NYSE")));

  private static List<String> symbols(List<StockListing> listings) {
    List<String> symbols = new ArrayList<>();
    for (StockListing listing : listings) {
      symbols.add(listing.getSymbol());
    }
    return symbols;
  }

  @Test
  void findsTickerIgnoringCase() {
    assertEquals("Apple Inc", directory.findBySymbol(" aapl ").getName());
    assertNull(directory.findBySymbol("ZZZZ"));
    assertNull(directory.findBySymbol(null));
  }

  @Test
  void ranksNameMatchesBestFirst() {
    // starts-with (shorter first), then word starts-with, then contains anywhere
    assertEquals(
        List.of("AAPL", "APLE", "AAPY", "PTIV"), symbols(directory.searchByName("apple", 10)));
  }

  @Test
  void exactNameRanksFirst() {
    assertEquals("AAPL", directory.searchByName("APPLE INC", 10).get(0).getSymbol());
  }

  @Test
  void limitsTheNumberOfMatches() {
    assertEquals(List.of("AAPL", "APLE"), symbols(directory.searchByName("apple", 2)));
  }

  @Test
  void blankOrUnmatchedQueryGivesNoMatches() {
    assertTrue(directory.searchByName("  ", 5).isEmpty());
    assertTrue(directory.searchByName("zzzz", 5).isEmpty());
  }

  @Test
  void rejectsNullList() {
    assertThrows(IllegalArgumentException.class, () -> new StockDirectory(null));
  }
}
