package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One news article about one or more companies.
 *
 * <p>Instances are immutable. The publish time is in US market time (New York), so it can be
 * compared directly with trading days.
 */
public class NewsArticle {

  private final String headline;
  private final String summary;
  private final String source;
  private final String url;
  private final LocalDateTime publishedAt;
  private final List<String> relatedSymbols;

  /**
   * Creates a news article.
   *
   * @param headline the article's title
   * @param summary a short summary; may be empty, but not null
   * @param source who published it, e.g. "Reuters"; may be empty, but not null
   * @param url the link to the full article
   * @param publishedAt when it was published, in New York time
   * @param relatedSymbols the ticker symbols the article is about; may be empty
   * @throws IllegalArgumentException if the headline or url is blank, or any argument is null
   */
  public NewsArticle(
      String headline,
      String summary,
      String source,
      String url,
      LocalDateTime publishedAt,
      List<String> relatedSymbols) {
    if (headline == null || headline.isBlank()) {
      throw new IllegalArgumentException("headline must not be blank");
    }
    if (url == null || url.isBlank()) {
      throw new IllegalArgumentException("url must not be blank");
    }
    if (summary == null || source == null) {
      throw new IllegalArgumentException("summary and source must not be null");
    }
    if (publishedAt == null) {
      throw new IllegalArgumentException("publishedAt must not be null");
    }
    if (relatedSymbols == null) {
      throw new IllegalArgumentException("relatedSymbols must not be null");
    }
    final List<String> symbols = new ArrayList<>();
    for (String symbol : relatedSymbols) {
      symbols.add(symbol.trim().toUpperCase());
    }
    this.headline = headline.trim();
    this.summary = summary.trim();
    this.source = source.trim();
    this.url = url.trim();
    this.publishedAt = publishedAt;
    this.relatedSymbols = Collections.unmodifiableList(symbols);
  }

  public String getHeadline() {
    return headline;
  }

  public String getSummary() {
    return summary;
  }

  public String getSource() {
    return source;
  }

  public String getUrl() {
    return url;
  }

  public LocalDateTime getPublishedAt() {
    return publishedAt;
  }

  /**
   * Returns the day the article was published, in New York time.
   *
   * @return the publish date
   */
  public LocalDate getPublishedDate() {
    return publishedAt.toLocalDate();
  }

  /**
   * Returns the ticker symbols the article is about.
   *
   * @return an unmodifiable list of upper-case symbols
   */
  public List<String> getRelatedSymbols() {
    return relatedSymbols;
  }

  /**
   * Tells whether the article is tagged with the given ticker symbol.
   *
   * @param symbol a ticker symbol, in any case
   * @return true if the symbol is one of the related symbols
   */
  public boolean isTaggedWith(String symbol) {
    return symbol != null && relatedSymbols.contains(symbol.trim().toUpperCase());
  }
}
