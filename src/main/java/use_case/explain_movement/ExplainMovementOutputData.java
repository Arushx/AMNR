package use_case.explain_movement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The result of the use case, as plain data (no entities), so the presenter doesn't depend on the
 * entity layer.
 */
public class ExplainMovementOutputData {

  private final String symbol;
  private final String companyName;
  private final LocalDate start;
  private final LocalDate end;
  private final double startClose;
  private final double endClose;
  private final double changePercent;
  private final String direction;
  private final String summary;
  private final List<Source> sources;
  private final String newsError;

  /**
   * Creates the output.
   *
   * @param symbol the ticker symbol
   * @param companyName the company's name; may be blank
   * @param start the first trading day in the period
   * @param end the last trading day in the period
   * @param startClose the close on the first day
   * @param endClose the close on the last day
   * @param changePercent the change, in percent; negative for a drop
   * @param direction "UP", "DOWN" or "FLAT"
   * @param summary the explanation; empty if the news couldn't be loaded
   * @param sources the cited articles, in citation order ([1] first)
   * @param newsError why the news couldn't be loaded; empty if it was loaded
   */
  public ExplainMovementOutputData(
      String symbol,
      String companyName,
      LocalDate start,
      LocalDate end,
      double startClose,
      double endClose,
      double changePercent,
      String direction,
      String summary,
      List<Source> sources,
      String newsError) {
    this.symbol = symbol;
    this.companyName = companyName;
    this.start = start;
    this.end = end;
    this.startClose = startClose;
    this.endClose = endClose;
    this.changePercent = changePercent;
    this.direction = direction;
    this.summary = summary;
    this.sources = Collections.unmodifiableList(new ArrayList<>(sources));
    this.newsError = newsError;
  }

  public String getSymbol() {
    return symbol;
  }

  public String getCompanyName() {
    return companyName;
  }

  public LocalDate getStart() {
    return start;
  }

  public LocalDate getEnd() {
    return end;
  }

  public double getStartClose() {
    return startClose;
  }

  public double getEndClose() {
    return endClose;
  }

  public double getChangePercent() {
    return changePercent;
  }

  public String getDirection() {
    return direction;
  }

  public String getSummary() {
    return summary;
  }

  public List<Source> getSources() {
    return sources;
  }

  public String getNewsError() {
    return newsError;
  }

  /** One cited article, as plain data. */
  public static class Source {
    private final String headline;
    private final String publisher;
    private final String url;
    private final LocalDateTime publishedAt;
    private final String sentiment;

    /**
     * Creates a source.
     *
     * @param headline the article's title
     * @param publisher who published it; may be empty
     * @param url the link to the article
     * @param publishedAt when it was published, New York time
     * @param sentiment "POSITIVE", "NEGATIVE" or "NEUTRAL"
     */
    public Source(
        String headline, String publisher, String url, LocalDateTime publishedAt,
        String sentiment) {
      this.headline = headline;
      this.publisher = publisher;
      this.url = url;
      this.publishedAt = publishedAt;
      this.sentiment = sentiment;
    }

    public String getHeadline() {
      return headline;
    }

    public String getPublisher() {
      return publisher;
    }

    public String getUrl() {
      return url;
    }

    public LocalDateTime getPublishedAt() {
      return publishedAt;
    }

    public String getSentiment() {
      return sentiment;
    }
  }
}
