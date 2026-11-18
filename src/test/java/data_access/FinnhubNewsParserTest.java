package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.NewsArticle;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.json.JSONArray;
import org.junit.jupiter.api.Test;

/** Tests the parser on a saved, made-up response (see src/test/resources/finnhub/README.md). */
class FinnhubNewsParserTest {

  static JSONArray loadSample() throws IOException {
    try (InputStream in =
        FinnhubNewsParserTest.class.getResourceAsStream("/finnhub/company_news_sample.json")) {
      return new JSONArray(new String(in.readAllBytes(), StandardCharsets.UTF_8));
    }
  }

  @Test
  void parsesValidItemsAndSkipsBrokenOnes() throws IOException {
    List<NewsArticle> articles = new FinnhubNewsParser().parseCompanyNews(loadSample());

    assertEquals(2, articles.size());
    NewsArticle first = articles.get(0);
    assertEquals("ACME shares plunge after regulators open probe", first.getHeadline());
    assertEquals("Example Wire", first.getSource());
    assertEquals("https://example.com/acme-probe", first.getUrl());
    assertEquals(LocalDateTime.of(2026, 3, 3, 8, 15), first.getPublishedAt());
  }

  @Test
  void splitsRelatedSymbols() throws IOException {
    NewsArticle second = new FinnhubNewsParser().parseCompanyNews(loadSample()).get(1);

    assertEquals(List.of("ACME", "BETA"), second.getRelatedSymbols());
    assertEquals("", second.getSummary());
  }

  @Test
  void usesNewYorkTimeForThePublishDate() throws IOException {
    // Published 23:30 in New York, which is already the next day in UTC.
    NewsArticle second = new FinnhubNewsParser().parseCompanyNews(loadSample()).get(1);

    assertEquals(LocalDate.of(2026, 3, 4), second.getPublishedDate());
  }

  @Test
  void emptyResponseGivesNoArticles() {
    assertTrue(new FinnhubNewsParser().parseCompanyNews(new JSONArray()).isEmpty());
  }
}
