package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.PriceHistory;
import entity.Quote;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import use_case.StockDataUnavailableException;

/**
 * Tests the DAO with a fake client that returns saved responses instead of calling the API, so no
 * requests are used.
 */
class AlphaVantageStockDataAccessObjectTest {

  private static JSONObject load(String fileName) throws IOException {
    try (InputStream in =
        AlphaVantageStockDataAccessObjectTest.class.getResourceAsStream(
            "/alphavantage/" + fileName)) {
      return new JSONObject(new String(in.readAllBytes(), StandardCharsets.UTF_8));
    }
  }

  /** A client that answers from saved files, or fails, without touching the network. */
  private static class FakeClient extends AlphaVantageClient {
    private final boolean fail;

    FakeClient(boolean fail) {
      super("fake-key");
      this.fail = fail;
    }

    @Override
    public JSONObject fetchGlobalQuote(String symbol) throws AlphaVantageException {
      if (fail) {
        throw new AlphaVantageException("daily limit reached");
      }
      try {
        return load("global_quote_ibm.json");
      } catch (IOException e) {
        throw new AlphaVantageException("missing test file", e);
      }
    }

    @Override
    public JSONObject fetchDailyTimeSeries(String symbol) throws AlphaVantageException {
      if (fail) {
        throw new AlphaVantageException("daily limit reached");
      }
      try {
        return load("time_series_daily_ibm.json");
      } catch (IOException e) {
        throw new AlphaVantageException("missing test file", e);
      }
    }
  }

  @Test
  void getQuoteReturnsParsedQuote() throws Exception {
    AlphaVantageStockDataAccessObject dao =
        new AlphaVantageStockDataAccessObject(new FakeClient(false), new AlphaVantageParser());

    Quote quote = dao.getQuote("IBM");

    assertEquals("IBM", quote.getSymbol());
    assertEquals(247.13, quote.getPrice());
  }

  @Test
  void getDailyHistoryReturnsParsedHistory() throws Exception {
    AlphaVantageStockDataAccessObject dao =
        new AlphaVantageStockDataAccessObject(new FakeClient(false), new AlphaVantageParser());

    PriceHistory history = dao.getDailyHistory("IBM");

    assertEquals("IBM", history.getSymbol());
    assertEquals(5, history.getPoints().size());
  }

  @Test
  void apiErrorsBecomeStockDataUnavailable() {
    AlphaVantageStockDataAccessObject dao =
        new AlphaVantageStockDataAccessObject(new FakeClient(true), new AlphaVantageParser());

    StockDataUnavailableException e =
        assertThrows(StockDataUnavailableException.class, () -> dao.getQuote("IBM"));
    assertTrue(e.getMessage().contains("daily limit reached"));
    assertThrows(StockDataUnavailableException.class, () -> dao.getDailyHistory("IBM"));
  }
}
