package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import entity.PriceHistory;
import entity.PricePoint;
import entity.Quote;
import entity.StockListing;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

/** Parses real Alpha Vantage responses saved in src/test/resources/alphavantage. */
class AlphaVantageParserTest {

  private final AlphaVantageParser parser = new AlphaVantageParser();

  private static JSONObject load(String fileName) throws IOException {
    try (InputStream in =
        AlphaVantageParserTest.class.getResourceAsStream("/alphavantage/" + fileName)) {
      return new JSONObject(new String(in.readAllBytes(), StandardCharsets.UTF_8));
    }
  }

  @Test
  void parsesGlobalQuote() throws Exception {
    Quote quote = parser.parseQuote(load("global_quote_ibm.json"));

    assertEquals("IBM", quote.getSymbol());
    assertEquals(247.13, quote.getPrice());
    assertEquals(2.46, quote.getChange());
    assertEquals(1.0054, quote.getChangePercent());
    assertEquals(LocalDate.of(2026, 11, 13), quote.getLatestTradingDay());
  }

  @Test
  void emptyGlobalQuoteMeansUnknownSymbol() {
    JSONObject json = new JSONObject("{\"Global Quote\": {}}");

    assertThrows(AlphaVantageException.class, () -> parser.parseQuote(json));
  }

  @Test
  void parsesDailyHistoryOldestFirst() throws Exception {
    PriceHistory history = parser.parseDailyHistory(load("time_series_daily_ibm.json"));

    assertEquals("IBM", history.getSymbol());
    assertEquals(5, history.getPoints().size());

    PricePoint latest = history.getLatest();
    assertEquals(LocalDate.of(2026, 11, 13), latest.getDate());
    assertEquals(246.63, latest.getOpen());
    assertEquals(247.85, latest.getHigh());
    assertEquals(244.83, latest.getLow());
    assertEquals(247.13, latest.getClose());
    assertEquals(2669242, latest.getVolume());

    LocalDate first = history.getPoints().get(0).getDate();
    assertEquals(true, first.isBefore(latest.getDate()));
  }

  @Test
  void malformedTimeSeriesIsRejected() {
    JSONObject json = new JSONObject("{\"Meta Data\": {\"2. Symbol\": \"IBM\"}}");

    assertThrows(AlphaVantageException.class, () -> parser.parseDailyHistory(json));
  }

  @Test
  void badNumberIsRejected() {
    JSONObject json = new JSONObject(
        "{\"Global Quote\": {\"01. symbol\": \"IBM\", \"05. price\": \"abc\","
            + " \"07. latest trading day\": \"2026-11-13\", \"09. change\": \"1\","
            + " \"10. change percent\": \"1%\"}}");

    assertThrows(AlphaVantageException.class, () -> parser.parseQuote(json));
  }

  @Test
  void parsesListingCsvSkippingRowsWithoutData() throws Exception {
    String csv;
    try (InputStream in =
        AlphaVantageParserTest.class.getResourceAsStream("/alphavantage/listing_status_sample.csv")) {
      csv = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    List<StockListing> listings = parser.parseListings(csv);

    assertEquals(11, listings.size());
    StockListing apple = listings.get(0);
    assertEquals("AAPL", apple.getSymbol());
    assertEquals("Apple Inc", apple.getName());
    assertEquals("NASDAQ", apple.getExchange());
  }

  @Test
  void listingWithoutHeaderIsRejected() {
    assertThrows(AlphaVantageException.class, () -> parser.parseListings("not,a,listing"));
  }
}
