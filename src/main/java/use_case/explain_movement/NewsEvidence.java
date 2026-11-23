package use_case.explain_movement;

import entity.Sentiment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The news chosen to explain a price move, plus how the sentiment of all the relevant news was
 * spread, so a summary can say e.g. "mostly negative (9 of 12 articles)". Immutable.
 */
public class NewsEvidence {

  private final List<LabeledArticle> selected;
  private final int positiveCount;
  private final int negativeCount;
  private final int neutralCount;

  /**
   * Creates the evidence.
   *
   * @param selected the chosen articles, best first
   * @param allRelevant every relevant article, chosen or not; used only for the counts
   */
  public NewsEvidence(List<LabeledArticle> selected, List<LabeledArticle> allRelevant) {
    this.selected = Collections.unmodifiableList(new ArrayList<>(selected));
    int positive = 0;
    int negative = 0;
    int neutral = 0;
    for (LabeledArticle labeled : allRelevant) {
      if (labeled.getSentiment() == Sentiment.POSITIVE) {
        positive++;
      } else if (labeled.getSentiment() == Sentiment.NEGATIVE) {
        negative++;
      } else {
        neutral++;
      }
    }
    this.positiveCount = positive;
    this.negativeCount = negative;
    this.neutralCount = neutral;
  }

  /**
   * Returns the chosen articles, best first.
   *
   * @return an unmodifiable list
   */
  public List<LabeledArticle> getSelected() {
    return selected;
  }

  public int getPositiveCount() {
    return positiveCount;
  }

  public int getNegativeCount() {
    return negativeCount;
  }

  public int getNeutralCount() {
    return neutralCount;
  }

  /**
   * Returns how many relevant articles there were in all.
   *
   * @return positive + negative + neutral
   */
  public int getRelevantCount() {
    return positiveCount + negativeCount + neutralCount;
  }
}
