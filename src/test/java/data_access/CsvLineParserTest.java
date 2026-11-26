package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class CsvLineParserTest {

  @Test
  void splitsPlainFields() {
    assertEquals(List.of("XOM", "Energy", "Mega Cap"), CsvLineParser.parse("XOM,Energy,Mega Cap"));
  }

  @Test
  void keepsCommasInsideQuotes() {
    assertEquals(
        List.of("XOM", "Oil, Gas & Fuels", "Mega Cap"),
        CsvLineParser.parse("XOM,\"Oil, Gas & Fuels\",Mega Cap"));
  }

  @Test
  void turnsDoubledQuotesIntoOneQuote() {
    assertEquals(
        List.of("It sells \"smart\" phones."),
        CsvLineParser.parse("\"It sells \"\"smart\"\" phones.\""));
  }

  @Test
  void keepsEmptyFields() {
    assertEquals(List.of("AACB", "", "", ""), CsvLineParser.parse("AACB,,,"));
    assertEquals(List.of(""), CsvLineParser.parse(""));
  }

  @Test
  void rejectsUnclosedQuote() {
    assertThrows(IllegalArgumentException.class, () -> CsvLineParser.parse("XOM,\"Energy"));
  }
}
