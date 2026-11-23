package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class WatchlistTest {

  @Test
  void storesUsernameAndSymbolsInOrder() {
    Watchlist watchlist = new Watchlist("andrew", List.of("MSFT", "AAPL"));

    assertEquals("andrew", watchlist.getUsername());
    assertEquals(List.of("MSFT", "AAPL"), watchlist.getSymbols());
  }

  @Test
  void normalizesSymbolsOnConstruction() {
    Watchlist watchlist = new Watchlist("andrew", List.of(" aapl ", "brk.b"));

    assertEquals(List.of("AAPL", "BRK.B"), watchlist.getSymbols());
  }

  @Test
  void rejectsBlankUsername() {
    assertThrows(IllegalArgumentException.class, () -> new Watchlist(" ", List.of()));
    assertThrows(IllegalArgumentException.class, () -> new Watchlist(null, List.of()));
  }

  @Test
  void rejectsNullSymbolList() {
    assertThrows(IllegalArgumentException.class, () -> new Watchlist("andrew", null));
  }

  @Test
  void rejectsDuplicateOrInvalidSymbolsOnConstruction() {
    assertThrows(
        IllegalArgumentException.class, () -> new Watchlist("andrew", List.of("AAPL", "aapl")));
    assertThrows(
        IllegalArgumentException.class, () -> new Watchlist("andrew", List.of("NOT VALID")));
  }

  @Test
  void copiesTheGivenList() {
    List<String> symbols = new ArrayList<>(List.of("AAPL"));
    Watchlist watchlist = new Watchlist("andrew", symbols);

    symbols.add("MSFT");

    assertEquals(List.of("AAPL"), watchlist.getSymbols());
  }

  @Test
  void symbolsCannotBeModified() {
    Watchlist watchlist = new Watchlist("andrew", List.of("AAPL"));

    assertThrows(UnsupportedOperationException.class, () -> watchlist.getSymbols().add("MSFT"));
  }

  @Test
  void validSymbols() {
    assertTrue(Watchlist.isValidSymbol("AAPL"));
    assertTrue(Watchlist.isValidSymbol("f"));
    assertTrue(Watchlist.isValidSymbol(" googl "));
    assertTrue(Watchlist.isValidSymbol("BRK.B"));
  }

  @Test
  void invalidSymbols() {
    assertFalse(Watchlist.isValidSymbol(null));
    assertFalse(Watchlist.isValidSymbol(""));
    assertFalse(Watchlist.isValidSymbol("   "));
    assertFalse(Watchlist.isValidSymbol("AAPL1"));
    assertFalse(Watchlist.isValidSymbol("TOOLONG"));
    assertFalse(Watchlist.isValidSymbol("BRK."));
    assertFalse(Watchlist.isValidSymbol("BRK.BB"));
    assertFalse(Watchlist.isValidSymbol("AA PL"));
  }

  @Test
  void normalizeSymbolTrimsAndUppercases() {
    assertEquals("AAPL", Watchlist.normalizeSymbol(" aapl "));
  }

  @Test
  void normalizeSymbolRejectsInvalid() {
    assertThrows(IllegalArgumentException.class, () -> Watchlist.normalizeSymbol("AAPL1"));
  }

  @Test
  void containsIgnoresCaseAndSpaces() {
    Watchlist watchlist = new Watchlist("andrew", List.of("AAPL"));

    assertTrue(watchlist.contains(" aapl"));
    assertFalse(watchlist.contains("MSFT"));
    assertFalse(watchlist.contains("not valid"));
  }

  @Test
  void withSymbolAppendsWithoutChangingOriginal() {
    Watchlist original = new Watchlist("andrew", List.of("AAPL"));

    Watchlist updated = original.withSymbol("msft");

    assertEquals(List.of("AAPL", "MSFT"), updated.getSymbols());
    assertEquals(List.of("AAPL"), original.getSymbols());
    assertEquals("andrew", updated.getUsername());
  }

  @Test
  void withSymbolRejectsDuplicate() {
    Watchlist watchlist = new Watchlist("andrew", List.of("AAPL"));

    assertThrows(IllegalArgumentException.class, () -> watchlist.withSymbol("aapl"));
  }

  @Test
  void withoutSymbolRemovesWithoutChangingOriginal() {
    Watchlist original = new Watchlist("andrew", List.of("AAPL", "MSFT"));

    Watchlist updated = original.withoutSymbol("aapl");

    assertEquals(List.of("MSFT"), updated.getSymbols());
    assertEquals(List.of("AAPL", "MSFT"), original.getSymbols());
  }

  @Test
  void withoutSymbolRejectsMissing() {
    Watchlist watchlist = new Watchlist("andrew", List.of("AAPL"));

    assertThrows(IllegalArgumentException.class, () -> watchlist.withoutSymbol("MSFT"));
  }
}
