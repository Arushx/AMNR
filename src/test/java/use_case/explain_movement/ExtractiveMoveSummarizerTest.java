package use_case.explain_movement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static use_case.explain_movement.TestData.DAY1;
import static use_case.explain_movement.TestData.labeled;
import static use_case.explain_movement.TestData.movement;

import entity.Sentiment;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExtractiveMoveSummarizerTest {

  private final ExtractiveMoveSummarizer summarizer = new ExtractiveMoveSummarizer();

  @Test
  void dropWithMatchingNews() {
    LabeledArticle probe = labeled("ACME shares slide after probe", DAY1.plusDays(1),
        Sentiment.NEGATIVE);
    LabeledArticle loss = labeled("ACME reports loss", DAY1.plusDays(2), Sentiment.NEGATIVE);
    LabeledArticle meeting = labeled("ACME annual meeting", DAY1, Sentiment.NEUTRAL);
    NewsEvidence evidence =
        new NewsEvidence(List.of(probe, meeting, loss), List.of(probe, meeting, loss));

    MovementSummary summary = summarizer.summarize(movement(100, 95, 88), "Acme Corp", evidence);

    assertEquals(
        "Acme Corp (ACME) fell 12.0% between Mar 2, 2026 and Mar 4, 2026, likely related to: "
            + "\"ACME shares slide after probe\" [1] (Example Wire, Mar 3) and "
            + "\"ACME reports loss\" [2] (Example Wire, Mar 4). "
            + "News in this period was mostly negative (2 of 3 articles).",
        summary.getText());
    assertEquals(List.of(probe, loss), summary.getSources());
  }

  @Test
  void citesAtMostThree() {
    List<LabeledArticle> news =
        List.of(
            labeled("ACME up 1", DAY1, Sentiment.POSITIVE),
            labeled("ACME up 2", DAY1, Sentiment.POSITIVE),
            labeled("ACME up 3", DAY1, Sentiment.POSITIVE),
            labeled("ACME up 4", DAY1, Sentiment.POSITIVE));

    MovementSummary summary =
        summarizer.summarize(movement(100, 110), "", new NewsEvidence(news, news));

    assertEquals(3, summary.getSources().size());
    assertTrue(summary.getText().startsWith("ACME rose 10.0% between"));
    assertTrue(summary.getText().contains("[1]") && summary.getText().contains("[3]"));
    assertTrue(summary.getText().endsWith("mostly positive (4 of 4 articles)."));
  }

  @Test
  void noMatchingNewsListsRelatedNewsInstead() {
    LabeledArticle good = labeled("ACME wins award", DAY1, Sentiment.POSITIVE);
    NewsEvidence evidence = new NewsEvidence(List.of(good), List.of(good));

    MovementSummary summary = summarizer.summarize(movement(100, 90), "Acme", evidence);

    assertTrue(
        summary.getText().contains(
            "No single news event clearly matches this move. Related news: "
                + "\"ACME wins award\" [1]"));
    assertTrue(summary.getText().endsWith("mostly positive (1 of 1 article)."));
    assertEquals(List.of(good), summary.getSources());
  }

  @Test
  void noNewsAtAll() {
    MovementSummary summary =
        summarizer.summarize(movement(100, 90), "Acme", new NewsEvidence(List.of(), List.of()));

    assertEquals(
        "Acme (ACME) fell 10.0% between Mar 2, 2026 and Mar 3, 2026. "
            + "No news about this company was found for this period.",
        summary.getText());
    assertTrue(summary.getSources().isEmpty());
  }

  @Test
  void flatMoveAndMixedNews() {
    List<LabeledArticle> news =
        List.of(
            labeled("ACME up", DAY1, Sentiment.POSITIVE),
            labeled("ACME down", DAY1, Sentiment.NEGATIVE));

    MovementSummary summary =
        summarizer.summarize(movement(100, 100.5), "Acme", new NewsEvidence(news, news));

    assertTrue(summary.getText().startsWith("Acme (ACME) was roughly flat (+0.5%) between"));
    assertTrue(summary.getText().endsWith("News in this period was mixed (2 articles)."));
  }
}
