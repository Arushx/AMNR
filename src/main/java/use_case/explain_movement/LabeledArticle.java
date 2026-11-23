package use_case.explain_movement;

import entity.NewsArticle;
import entity.Sentiment;

/** A news article together with the sentiment label it was given. Immutable. */
public class LabeledArticle {

  private final NewsArticle article;
  private final Sentiment sentiment;

  /**
   * Pairs an article with its label.
   *
   * @param article the article
   * @param sentiment its label
   * @throws IllegalArgumentException if either is null
   */
  public LabeledArticle(NewsArticle article, Sentiment sentiment) {
    if (article == null || sentiment == null) {
      throw new IllegalArgumentException("article and sentiment must not be null");
    }
    this.article = article;
    this.sentiment = sentiment;
  }

  public NewsArticle getArticle() {
    return article;
  }

  public Sentiment getSentiment() {
    return sentiment;
  }
}
