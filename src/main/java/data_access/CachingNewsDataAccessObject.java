package data_access;

import entity.NewsArticle;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import use_case.NewsDataAccessInterface;
import use_case.NewsUnavailableException;

/**
 * Saves news to JSON files so the same request doesn't use the API again (Decorator pattern).
 *
 * <p>It wraps another {@link NewsDataAccessInterface} and implements the same interface, so the
 * use case can't tell the difference. One file is kept per symbol and date range. A range that
 * reaches today or later is never cached, because more news can still come out. A missing or broken
 * cache file just means the news is fetched again; a cache that can't be written is ignored.
 */
public class CachingNewsDataAccessObject implements NewsDataAccessInterface {

  private static final String HEADLINE = "headline";
  private static final String SUMMARY = "summary";
  private static final String SOURCE = "source";
  private static final String URL = "url";
  private static final String PUBLISHED_AT = "publishedAt";
  private static final String RELATED = "related";

  private final NewsDataAccessInterface source;
  private final Path cacheDirectory;
  private final Clock clock;

  /**
   * Creates the cache.
   *
   * @param source where news comes from when it isn't cached, e.g. Finnhub
   * @param cacheDirectory the folder for cache files; created when first needed
   * @param clock tells what day it is in New York, to know which ranges are finished
   */
  public CachingNewsDataAccessObject(
      NewsDataAccessInterface source, Path cacheDirectory, Clock clock) {
    this.source = source;
    this.cacheDirectory = cacheDirectory;
    this.clock = clock;
  }

  @Override
  public List<NewsArticle> getNews(String symbol, LocalDate from, LocalDate to)
      throws NewsUnavailableException {
    final boolean cacheable = to.isBefore(LocalDate.now(clock.withZone(
        FinnhubNewsParser.MARKET_ZONE)));
    final Path file = cacheDirectory.resolve(fileName(symbol, from, to));
    if (cacheable) {
      final List<NewsArticle> cached = read(file);
      if (cached != null) {
        return cached;
      }
    }
    final List<NewsArticle> news = source.getNews(symbol, from, to);
    if (cacheable) {
      write(file, news);
    }
    return news;
  }

  /**
   * Returns the cache file name for a request, e.g. "NVDA_2026-03-01_2026-03-31.json".
   *
   * @param symbol the ticker symbol
   * @param from the first day
   * @param to the last day
   * @return the file name
   */
  static String fileName(String symbol, LocalDate from, LocalDate to) {
    final String safeSymbol = symbol.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9.]", "_");
    return safeSymbol + "_" + from + "_" + to + ".json";
  }

  private static List<NewsArticle> read(Path file) {
    if (!Files.isRegularFile(file)) {
      return null;
    }
    try {
      final JSONArray json = new JSONArray(Files.readString(file, StandardCharsets.UTF_8));
      final List<NewsArticle> articles = new ArrayList<>();
      for (int i = 0; i < json.length(); i++) {
        articles.add(fromJson(json.getJSONObject(i)));
      }
      return articles;
    } catch (IOException | JSONException | DateTimeParseException | IllegalArgumentException e) {
      return null;
    }
  }

  private void write(Path file, List<NewsArticle> news) {
    final JSONArray json = new JSONArray();
    for (NewsArticle article : news) {
      json.put(toJson(article));
    }
    try {
      Files.createDirectories(cacheDirectory);
      Files.writeString(file, json.toString(2), StandardCharsets.UTF_8);
    } catch (IOException e) {
      // The cache only saves API calls; the news is still returned.
    }
  }

  private static JSONObject toJson(NewsArticle article) {
    return new JSONObject()
        .put(HEADLINE, article.getHeadline())
        .put(SUMMARY, article.getSummary())
        .put(SOURCE, article.getSource())
        .put(URL, article.getUrl())
        .put(PUBLISHED_AT, article.getPublishedAt().toString())
        .put(RELATED, new JSONArray(article.getRelatedSymbols()));
  }

  private static NewsArticle fromJson(JSONObject json) {
    final List<String> related = new ArrayList<>();
    final JSONArray symbols = json.getJSONArray(RELATED);
    for (int i = 0; i < symbols.length(); i++) {
      related.add(symbols.getString(i));
    }
    return new NewsArticle(
        json.getString(HEADLINE),
        json.getString(SUMMARY),
        json.getString(SOURCE),
        json.getString(URL),
        LocalDateTime.parse(json.getString(PUBLISHED_AT)),
        related);
  }
}
