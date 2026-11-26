package use_case.explain_movement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static use_case.explain_movement.TestData.DAY1;
import static use_case.explain_movement.TestData.article;

import entity.NewsArticle;
import entity.Sentiment;
import java.util.List;
import org.junit.jupiter.api.Test;

class FallbackSentimentClassifierTest {

  private static final List<NewsArticle> ONE = List.of(article("ACME news", DAY1));

  private static SentimentClassifier always(Sentiment label) {
    return articles -> List.of(label);
  }

  private static final SentimentClassifier FAILING =
      articles -> {
        throw new SentimentUnavailableException("model missing");
      };

  @Test
  void usesThePrimaryWhenItWorks() throws SentimentUnavailableException {
    SentimentClassifier classifier =
        new FallbackSentimentClassifier(always(Sentiment.POSITIVE), always(Sentiment.NEGATIVE));

    assertEquals(List.of(Sentiment.POSITIVE), classifier.classify(ONE));
  }

  @Test
  void usesTheBackupWhenThePrimaryFails() throws SentimentUnavailableException {
    SentimentClassifier classifier =
        new FallbackSentimentClassifier(FAILING, always(Sentiment.NEGATIVE));

    assertEquals(List.of(Sentiment.NEGATIVE), classifier.classify(ONE));
  }

  @Test
  void failsWhenBothFail() {
    SentimentClassifier classifier = new FallbackSentimentClassifier(FAILING, FAILING);

    assertThrows(SentimentUnavailableException.class, () -> classifier.classify(ONE));
  }
}
