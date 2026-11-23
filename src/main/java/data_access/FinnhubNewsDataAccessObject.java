package data_access;

import entity.NewsArticle;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import use_case.NewsDataAccessInterface;
import use_case.NewsUnavailableException;

/**
 * Provides company news from Finnhub to the use cases.
 *
 * <p>It implements the news data access interface declared in the use case layer, uses {@link
 * FinnhubClient} to reach the API and {@link FinnhubNewsParser} to build entities, and turns
 * Finnhub errors into {@link NewsUnavailableException}, so use cases never see a Finnhub type.
 *
 * <p>Finnhub caps how many articles one request returns, so long ranges are fetched in chunks of
 * {@value #CHUNK_DAYS} days. The same article can appear in two chunks, so duplicates (same link)
 * are removed.
 */
public class FinnhubNewsDataAccessObject implements NewsDataAccessInterface {

  static final int CHUNK_DAYS = 30;

  private final FinnhubClient client;
  private final FinnhubNewsParser parser;

  /**
   * Creates the data access object.
   *
   * @param client sends requests to Finnhub
   * @param parser turns responses into entities
   */
  public FinnhubNewsDataAccessObject(FinnhubClient client, FinnhubNewsParser parser) {
    this.client = client;
    this.parser = parser;
  }

  @Override
  public List<NewsArticle> getNews(String symbol, LocalDate from, LocalDate to)
      throws NewsUnavailableException {
    if (from.isAfter(to)) {
      throw new IllegalArgumentException("from must not be after to");
    }
    final Map<String, NewsArticle> byUrl = new LinkedHashMap<>();
    LocalDate chunkStart = from;
    while (!chunkStart.isAfter(to)) {
      final LocalDate chunkEnd = min(chunkStart.plusDays(CHUNK_DAYS - 1), to);
      try {
        for (NewsArticle article :
            parser.parseCompanyNews(client.fetchCompanyNews(symbol, chunkStart, chunkEnd))) {
          if (isWithin(article, from, to)) {
            byUrl.putIfAbsent(article.getUrl(), article);
          }
        }
      } catch (FinnhubException e) {
        throw new NewsUnavailableException(
            "Could not get news for " + symbol + ": " + e.getMessage());
      }
      chunkStart = chunkEnd.plusDays(1);
    }
    final List<NewsArticle> articles = new ArrayList<>(byUrl.values());
    articles.sort(Comparator.comparing(NewsArticle::getPublishedAt));
    return articles;
  }

  private static boolean isWithin(NewsArticle article, LocalDate from, LocalDate to) {
    final LocalDate day = article.getPublishedDate();
    return !day.isBefore(from) && !day.isAfter(to);
  }

  private static LocalDate min(LocalDate a, LocalDate b) {
    return a.isBefore(b) ? a : b;
  }
}
