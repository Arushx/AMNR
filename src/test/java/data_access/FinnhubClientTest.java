package data_access;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

class FinnhubClientTest {

  @Test
  void rejectsMissingApiKey() {
    assertThrows(IllegalArgumentException.class, () -> new FinnhubClient(null));
    assertThrows(IllegalArgumentException.class, () -> new FinnhubClient(" "));
  }

  @Test
  void badKeyIsAnErrorWithFinnhubsMessage() {
    FinnhubException e =
        assertThrows(
            FinnhubException.class,
            () -> FinnhubClient.checkForError(401, "{\"error\": \"Invalid API key.\"}"));
    assertEquals("Invalid API key.", e.getMessage());
  }

  @Test
  void rateLimitIsAnError() {
    FinnhubException e =
        assertThrows(FinnhubException.class, () -> FinnhubClient.checkForError(429, ""));
    assertEquals("Finnhub rate limit reached, try again in a minute", e.getMessage());
  }

  @Test
  void errorBodyWithStatus200IsAnError() {
    assertThrows(
        FinnhubException.class,
        () -> FinnhubClient.checkForError(200, "{\"error\": \"You don't have access.\"}"));
  }

  @Test
  void serverErrorIsAnError() {
    assertThrows(FinnhubException.class, () -> FinnhubClient.checkForError(500, "oops"));
  }

  @Test
  void newsArrayIsNotAnError() {
    assertDoesNotThrow(() -> FinnhubClient.checkForError(200, "[]"));
  }

  /** Calls the real API (uses 1 request). Runs only when FINNHUB_API_KEY is set. */
  @Test
  @EnabledIfEnvironmentVariable(named = "FINNHUB_API_KEY", matches = ".+")
  void fetchesRealCompanyNews() throws FinnhubException {
    FinnhubClient client = new FinnhubClient(System.getenv("FINNHUB_API_KEY"));
    LocalDate to = LocalDate.now();

    JSONArray news = client.fetchCompanyNews("AAPL", to.minusDays(7), to);

    assertDoesNotThrow(() -> new FinnhubNewsParser().parseCompanyNews(news));
  }
}
