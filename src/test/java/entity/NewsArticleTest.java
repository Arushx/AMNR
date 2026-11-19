package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class NewsArticleTest {

  private static final LocalDateTime TIME = LocalDateTime.of(2026, 3, 3, 8, 15);

  private static NewsArticle article(String headline, String url) {
    return new NewsArticle(headline, "summary", "Example Wire", url, TIME, List.of("acme"));
  }

  @Test
  void keepsFieldsAndNormalizesSymbols() {
    NewsArticle a = article("  ACME falls  ", "https://example.com/a");

    assertEquals("ACME falls", a.getHeadline());
    assertEquals(LocalDate.of(2026, 3, 3), a.getPublishedDate());
    assertEquals(List.of("ACME"), a.getRelatedSymbols());
    assertTrue(a.isTaggedWith("Acme"));
    assertFalse(a.isTaggedWith("BETA"));
  }

  @Test
  void rejectsBlankHeadlineOrUrl() {
    assertThrows(IllegalArgumentException.class, () -> article(" ", "https://example.com/a"));
    assertThrows(IllegalArgumentException.class, () -> article("ACME falls", ""));
  }

  @Test
  void rejectsMissingTime() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new NewsArticle("h", "", "", "https://example.com", null, List.of()));
  }

  @Test
  void relatedSymbolsCannotBeChanged() {
    NewsArticle a = article("ACME falls", "https://example.com/a");

    assertThrows(UnsupportedOperationException.class, () -> a.getRelatedSymbols().add("X"));
  }
}
