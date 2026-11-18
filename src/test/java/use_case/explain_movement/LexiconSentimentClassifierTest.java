package use_case.explain_movement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import entity.NewsArticle;
import entity.Sentiment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Uses a tiny hand-made lexicon, so the rules are tested without loading the real files. */
class LexiconSentimentClassifierTest {

  private final LexiconSentimentClassifier classifier =
      new LexiconSentimentClassifier(
          new SentimentLexicon(
              Set.of("strong", "surge", "beat"), Set.of("plunge", "probe", "loss", "sell-off")));

  private static NewsArticle article(String headline, String summary) {
    return new NewsArticle(
        headline, summary, "Example Wire", "https://example.com/a",
        LocalDateTime.of(2026, 3, 3, 9, 0), List.of("ACME"));
  }

  @Test
  void negativeHeadline() {
    assertEquals(
        Sentiment.NEGATIVE, classifier.classify(article("ACME shares plunge after probe", "")));
  }

  @Test
  void positiveHeadline() {
    assertEquals(
        Sentiment.POSITIVE, classifier.classify(article("ACME beats estimates, stock surges",
            "Results were strong.")));
  }

  @Test
  void noSentimentWordsIsNeutral() {
    assertEquals(Sentiment.NEUTRAL, classifier.classify(article("ACME holds annual meeting", "")));
  }

  @Test
  void headlineCountsMoreThanSummary() {
    // Headline: -1 x 2 = -2. Summary: +1. Total -1.
    assertEquals(
        Sentiment.NEGATIVE,
        classifier.classify(article("ACME reports loss", "Sales were strong in Asia.")));
  }

  @Test
  void negatedPositiveWordIsNotCounted() {
    assertEquals(
        Sentiment.NEUTRAL, classifier.classify(article("Demand for ACME chips not strong", "")));
  }

  @Test
  void hyphenatedWordIsMatchedWhole() {
    assertEquals(
        Sentiment.NEGATIVE, classifier.classify(article("Chip stocks in sell-off", "")));
  }

  @Test
  void classifiesAListInOrder() {
    List<Sentiment> labels =
        classifier.classify(
            List.of(article("ACME plunge", ""), article("ACME surge", ""), article("ACME", "")));

    assertEquals(List.of(Sentiment.NEGATIVE, Sentiment.POSITIVE, Sentiment.NEUTRAL), labels);
  }
}
