package use_case.explain_movement;

import entity.NewsArticle;
import entity.Sentiment;
import java.util.List;

/**
 * Labels news articles as positive, negative or neutral.
 *
 * <p>It takes a whole list at once, so an implementation that calls a model (e.g. FinBERT) can
 * send the articles in batches instead of one by one.
 */
public interface SentimentClassifier {

  /**
   * Labels each article.
   *
   * @param articles the articles to label
   * @return one label per article, in the same order
   */
  List<Sentiment> classify(List<NewsArticle> articles);
}
