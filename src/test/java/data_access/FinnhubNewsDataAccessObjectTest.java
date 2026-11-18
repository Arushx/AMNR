package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.NewsArticle;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;
import use_case.NewsUnavailableException;

/** Tests the DAO with a fake client, so no requests are used. */
class FinnhubNewsDataAccessObjectTest {

  /** A client that answers every request with the saved sample, or fails. */
  private static class FakeClient extends FinnhubClient {
    private final boolean fail;
    private final List<LocalDate[]> requests = new ArrayList<>();

    FakeClient(boolean fail) {
      super("fake-key");
      this.fail = fail;
    }

    @Override
    public JSONArray fetchCompanyNews(String symbol, LocalDate from, LocalDate to)
        throws FinnhubException {
      requests.add(new LocalDate[] {from, to});
      if (fail) {
        throw new FinnhubException("Invalid API key.");
      }
      try {
        return FinnhubNewsParserTest.loadSample();
      } catch (IOException e) {
        throw new FinnhubException("missing test file", e);
      }
    }
  }

  @Test
  void returnsArticlesInRangeOldestFirst() throws NewsUnavailableException {
    FinnhubNewsDataAccessObject dao =
        new FinnhubNewsDataAccessObject(new FakeClient(false), new FinnhubNewsParser());

    List<NewsArticle> news =
        dao.getNews("ACME", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 10));

    assertEquals(2, news.size());
    assertTrue(news.get(0).getPublishedAt().isBefore(news.get(1).getPublishedAt()));
  }

  @Test
  void dropsArticlesOutsideTheRange() throws NewsUnavailableException {
    FinnhubNewsDataAccessObject dao =
        new FinnhubNewsDataAccessObject(new FakeClient(false), new FinnhubNewsParser());

    List<NewsArticle> news =
        dao.getNews("ACME", LocalDate.of(2026, 3, 4), LocalDate.of(2026, 3, 10));

    assertEquals(1, news.size());
    assertEquals("https://example.com/acme-beta", news.get(0).getUrl());
  }

  @Test
  void splitsLongRangesIntoChunksAndRemovesDuplicates() throws NewsUnavailableException {
    FakeClient client = new FakeClient(false);
    FinnhubNewsDataAccessObject dao =
        new FinnhubNewsDataAccessObject(client, new FinnhubNewsParser());

    // 70 days: chunks of 30 + 30 + 10. Every chunk returns the same two articles.
    List<NewsArticle> news =
        dao.getNews("ACME", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 11));

    assertEquals(3, client.requests.size());
    assertEquals(LocalDate.of(2026, 1, 30), client.requests.get(0)[1]);
    assertEquals(LocalDate.of(2026, 1, 31), client.requests.get(1)[0]);
    assertEquals(LocalDate.of(2026, 3, 11), client.requests.get(2)[1]);
    assertEquals(2, news.size());
  }

  @Test
  void finnhubErrorBecomesNewsUnavailable() {
    FinnhubNewsDataAccessObject dao =
        new FinnhubNewsDataAccessObject(new FakeClient(true), new FinnhubNewsParser());

    NewsUnavailableException e =
        assertThrows(
            NewsUnavailableException.class,
            () -> dao.getNews("ACME", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 2)));
    assertEquals("Could not get news for ACME: Invalid API key.", e.getMessage());
  }
}
