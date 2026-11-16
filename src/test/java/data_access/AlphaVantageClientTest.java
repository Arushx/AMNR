package data_access;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import entity.Quote;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

class AlphaVantageClientTest {

  @Test
  void rejectsMissingApiKey() {
    assertThrows(IllegalArgumentException.class, () -> new AlphaVantageClient(null));
    assertThrows(IllegalArgumentException.class, () -> new AlphaVantageClient("  "));
  }

  @Test
  void informationReplyIsAnError() {
    JSONObject json = new JSONObject("{\"Information\": \"The demo API key is for demo only\"}");

    AlphaVantageException e =
        assertThrows(AlphaVantageException.class, () -> AlphaVantageClient.checkForApiError(json));
    assertEquals("The demo API key is for demo only", e.getMessage());
  }

  @Test
  void errorMessageReplyIsAnError() {
    JSONObject json = new JSONObject("{\"Error Message\": \"Invalid API call\"}");

    assertThrows(AlphaVantageException.class, () -> AlphaVantageClient.checkForApiError(json));
  }

  @Test
  void dataReplyIsNotAnError() {
    JSONObject json = new JSONObject("{\"Global Quote\": {}}");

    assertDoesNotThrow(() -> AlphaVantageClient.checkForApiError(json));
  }

  /**
   * Calls the real API once. Runs only when ALPHA_VANTAGE_API_KEY is set, so it never runs on a
   * machine without a key, and uses 1 of the 25 free daily requests.
   */
  @Test
  @EnabledIfEnvironmentVariable(named = "ALPHA_VANTAGE_API_KEY", matches = ".+")
  void fetchesRealQuote() throws Exception {
    AlphaVantageClient client = new AlphaVantageClient(System.getenv("ALPHA_VANTAGE_API_KEY"));

    Quote quote = new AlphaVantageParser().parseQuote(client.fetchGlobalQuote("IBM"));

    assertEquals("IBM", quote.getSymbol());
  }
}
