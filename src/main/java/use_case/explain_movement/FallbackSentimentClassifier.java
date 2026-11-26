package use_case.explain_movement;

import entity.NewsArticle;
import entity.Sentiment;
import java.util.List;

/**
 * Tries one classifier and, if it fails, uses another. For example FinBERT first, with the
 * dictionary classifier as the backup, so a model problem never stops the use case.
 *
 * <p>It is itself a {@link SentimentClassifier}, so the interactor doesn't know a fallback exists.
 */
public class FallbackSentimentClassifier implements SentimentClassifier {

  private final SentimentClassifier primary;
  private final SentimentClassifier backup;

  /**
   * Creates the fallback.
   *
   * @param primary the classifier to try first
   * @param backup the classifier to use when the first one fails
   */
  public FallbackSentimentClassifier(SentimentClassifier primary, SentimentClassifier backup) {
    this.primary = primary;
    this.backup = backup;
  }

  @Override
  public List<Sentiment> classify(List<NewsArticle> articles)
      throws SentimentUnavailableException {
    try {
      return primary.classify(articles);
    } catch (SentimentUnavailableException e) {
      return backup.classify(articles);
    }
  }
}
