package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.NewsArticle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.NewsDataAccessInterface;
import use_case.NewsUnavailableException;

class CachingNewsDataAccessObjectTest {

  /** Noon UTC on Mar 20, 2026: Mar 20 in New York too. */
  private static final Clock NOW =
      Clock.fixed(Instant.parse("2026-03-20T12:00:00Z"), ZoneOffset.UTC);

  private static final LocalDate MAR1 = LocalDate.of(2026, 3, 1);
  private static final LocalDate MAR10 = LocalDate.of(2026, 3, 10);

  private static final NewsArticle ARTICLE =
      new NewsArticle(
          "ACME shares slide", "Made-up summary.", "Example Wire", "https://example.com/a",
          LocalDateTime.of(2026, 3, 3, 8, 15), List.of("ACME", "BETA"));

  @TempDir Path cacheDir;

  /** Counts calls, and can be switched to fail. */
  private static class CountingSource implements NewsDataAccessInterface {
    private int calls;
    private boolean fail;

    @Override
    public List<NewsArticle> getNews(String symbol, LocalDate from, LocalDate to)
        throws NewsUnavailableException {
      calls++;
      if (fail) {
        throw new NewsUnavailableException("Finnhub is down");
      }
      return List.of(ARTICLE);
    }
  }

  @Test
  void secondRequestComesFromTheFile() throws NewsUnavailableException {
    CountingSource source = new CountingSource();
    CachingNewsDataAccessObject cache = new CachingNewsDataAccessObject(source, cacheDir, NOW);

    cache.getNews("acme", MAR1, MAR10);
    source.fail = true;
    List<NewsArticle> again = cache.getNews("ACME", MAR1, MAR10);

    assertEquals(1, source.calls);
    NewsArticle loaded = again.get(0);
    assertEquals(ARTICLE.getHeadline(), loaded.getHeadline());
    assertEquals(ARTICLE.getSummary(), loaded.getSummary());
    assertEquals(ARTICLE.getSource(), loaded.getSource());
    assertEquals(ARTICLE.getUrl(), loaded.getUrl());
    assertEquals(ARTICLE.getPublishedAt(), loaded.getPublishedAt());
    assertEquals(ARTICLE.getRelatedSymbols(), loaded.getRelatedSymbols());
    assertTrue(Files.exists(cacheDir.resolve("ACME_2026-03-01_2026-03-10.json")));
  }

  @Test
  void survivesARestart() throws NewsUnavailableException {
    new CachingNewsDataAccessObject(new CountingSource(), cacheDir, NOW)
        .getNews("ACME", MAR1, MAR10);

    CountingSource fresh = new CountingSource();
    new CachingNewsDataAccessObject(fresh, cacheDir, NOW).getNews("ACME", MAR1, MAR10);

    assertEquals(0, fresh.calls);
  }

  @Test
  void rangesReachingTodayAreNotCached() throws NewsUnavailableException {
    CountingSource source = new CountingSource();
    CachingNewsDataAccessObject cache = new CachingNewsDataAccessObject(source, cacheDir, NOW);

    cache.getNews("ACME", MAR1, LocalDate.of(2026, 3, 20));
    cache.getNews("ACME", MAR1, LocalDate.of(2026, 3, 20));

    assertEquals(2, source.calls);
    assertFalse(Files.exists(cacheDir.resolve("ACME_2026-03-01_2026-03-20.json")));
  }

  @Test
  void brokenFileIsIgnored() throws IOException, NewsUnavailableException {
    Files.writeString(cacheDir.resolve("ACME_2026-03-01_2026-03-10.json"), "not json");
    CountingSource source = new CountingSource();

    List<NewsArticle> news =
        new CachingNewsDataAccessObject(source, cacheDir, NOW).getNews("ACME", MAR1, MAR10);

    assertEquals(1, source.calls);
    assertEquals(1, news.size());
  }

  @Test
  void failuresAreNotCached() {
    CountingSource source = new CountingSource();
    source.fail = true;
    CachingNewsDataAccessObject cache = new CachingNewsDataAccessObject(source, cacheDir, NOW);

    assertThrows(NewsUnavailableException.class, () -> cache.getNews("ACME", MAR1, MAR10));
    assertFalse(Files.exists(cacheDir.resolve("ACME_2026-03-01_2026-03-10.json")));
  }

  @Test
  void fileNameIsSafe() {
    assertEquals(
        "BRK.B_2026-03-01_2026-03-10.json",
        CachingNewsDataAccessObject.fileName(" brk.b ", MAR1, MAR10));
    assertEquals(
        "__ETC_2026-03-01_2026-03-10.json",
        CachingNewsDataAccessObject.fileName("/\\etc", MAR1, MAR10));
  }
}
