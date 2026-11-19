package use_case.explain_movement;

import entity.NewsArticle;
import entity.PriceHistory;
import entity.PriceMovement;
import entity.PricePoint;
import entity.Sentiment;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Small builders shared by the tests in this package. All names and news are made up. */
final class TestData {

  static final LocalDate DAY1 = LocalDate.of(2026, 3, 2);

  private TestData() {}

  /** A history for "ACME" with one close per calendar day, starting on DAY1. */
  static PriceHistory history(double... closes) {
    List<PricePoint> points = new ArrayList<>();
    for (int i = 0; i < closes.length; i++) {
      points.add(new PricePoint(DAY1.plusDays(i), closes[i], closes[i], closes[i], closes[i], 100));
    }
    return new PriceHistory("ACME", points);
  }

  static PriceMovement movement(double... closes) {
    return PriceMovement.of(history(closes));
  }

  /** An article tagged ACME, published at 9:00 on the given day. */
  static NewsArticle article(String headline, LocalDate day) {
    return article(headline, day, List.of("ACME"));
  }

  static NewsArticle article(String headline, LocalDate day, List<String> symbols) {
    return new NewsArticle(
        headline, "", "Example Wire",
        "https://example.com/" + Math.abs(headline.hashCode()) + "/" + day,
        day.atTime(9, 0), symbols);
  }

  static LabeledArticle labeled(String headline, LocalDate day, Sentiment sentiment) {
    return new LabeledArticle(article(headline, day), sentiment);
  }
}
