package use_case.explain_movement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static use_case.explain_movement.TestData.DAY1;
import static use_case.explain_movement.TestData.article;
import static use_case.explain_movement.TestData.labeled;
import static use_case.explain_movement.TestData.movement;

import entity.NewsArticle;
import entity.PriceMovement;
import entity.Sentiment;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EvidenceSelectorTest {

  private final EvidenceSelector selector = new EvidenceSelector();

  @Test
  void keepsArticlesTaggedWithTheTickerOrNamingTheCompany() {
    NewsArticle tagged = article("Chip stocks mixed", DAY1);
    NewsArticle named = article("Acme Corp cuts outlook", DAY1, List.of());
    NewsArticle unrelated = article("Oil prices rise", DAY1, List.of("XOM"));

    List<NewsArticle> relevant =
        selector.filterRelevant(List.of(tagged, named, unrelated), "ACME", "Acme Corporation");

    assertEquals(List.of(tagged, named), relevant);
  }

  @Test
  void tickerMustBeInCapitals() {
    assertTrue(EvidenceSelector.headlineNames(article("ON shares jump", DAY1), "ON", ""));
    assertFalse(EvidenceSelector.headlineNames(article("Shares on the move", DAY1), "ON", ""));
  }

  @Test
  void twoWordNamesMatchOnTheirFirstWord() {
    assertTrue(
        EvidenceSelector.headlineNames(
            article("Meta unveils new headset", DAY1), "META", "Meta Platforms, Inc."));
    assertFalse(
        EvidenceSelector.headlineNames(
            article("Bank stocks fall", DAY1), "BAC", "Bank of America Corporation"));
  }

  @Test
  void shortNameDropsSuffixes() {
    assertEquals("NVIDIA", EvidenceSelector.shortName("NVIDIA Corporation"));
    assertEquals("APPLE", EvidenceSelector.shortName("Apple Inc."));
    assertEquals("", EvidenceSelector.shortName(null));
  }

  @Test
  void scoresHeadlineSentimentAndTiming() {
    // Drop with the biggest day on DAY1 + 2.
    PriceMovement drop = movement(100, 99, 90, 89);

    LabeledArticle best = labeled("ACME plunges", DAY1.plusDays(2), Sentiment.NEGATIVE);
    LabeledArticle wrongTone = labeled("ACME plunges", DAY1.plusDays(2), Sentiment.POSITIVE);
    LabeledArticle tooEarly = labeled("Market wrap", DAY1.minusDays(10), Sentiment.NEGATIVE);

    assertEquals(3 + 2 + 2, selector.score(best, drop, "Acme"));
    assertEquals(3 + 2, selector.score(wrongTone, drop, "Acme"));
    assertEquals(2, selector.score(tooEarly, drop, "Acme"));
  }

  @Test
  void newsUpToThreeDaysBeforeABigDayCounts() {
    // Only one down day: DAY1 + 5.
    PriceMovement drop = movement(100, 100, 100, 100, 100, 80);
    LabeledArticle friday = labeled("Market wrap", DAY1.plusDays(2), Sentiment.NEUTRAL);
    LabeledArticle tooEarly = labeled("Market wrap", DAY1.plusDays(1), Sentiment.NEUTRAL);

    assertEquals(2, selector.score(friday, drop, "Acme"));
    assertEquals(0, selector.score(tooEarly, drop, "Acme"));
  }

  @Test
  void selectsBestFirstAndCountsAllRelevant() {
    PriceMovement drop = movement(100, 99, 90, 89);
    LabeledArticle weak = labeled("Market wrap", DAY1, Sentiment.NEUTRAL);
    LabeledArticle strong = labeled("ACME plunges", DAY1.plusDays(2), Sentiment.NEGATIVE);

    NewsEvidence evidence = selector.select(List.of(weak, strong), drop, "Acme");

    assertEquals(List.of(strong, weak), evidence.getSelected());
    assertEquals(1, evidence.getNegativeCount());
    assertEquals(1, evidence.getNeutralCount());
    assertEquals(2, evidence.getRelevantCount());
  }

  @Test
  void keepsAtMostFifteen() {
    List<LabeledArticle> many = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      many.add(labeled("ACME news " + i, DAY1, Sentiment.NEUTRAL));
    }

    NewsEvidence evidence = selector.select(many, movement(100, 90), "Acme");

    assertEquals(EvidenceSelector.MAX_SELECTED, evidence.getSelected().size());
    assertEquals(20, evidence.getRelevantCount());
  }

  @Test
  void flatMovesNeverMatchSentiment() {
    assertFalse(
        EvidenceSelector.matchesDirection(Sentiment.NEGATIVE, entity.Direction.FLAT));
    assertTrue(EvidenceSelector.matchesDirection(Sentiment.POSITIVE, entity.Direction.UP));
  }
}
