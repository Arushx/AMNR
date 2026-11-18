package data_access;

import entity.NewsArticle;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Turns Finnhub company-news JSON into {@link NewsArticle} entities.
 *
 * <p>All of Finnhub's key names live here, so if the format changes, only this class changes. It
 * does no network access, which makes it easy to test with saved responses.
 */
public class FinnhubNewsParser {

  /** Trading days are in New York time, so publish times are converted to it. */
  static final ZoneId MARKET_ZONE = ZoneId.of("America/New_York");

  private static final String HEADLINE = "headline";
  private static final String SUMMARY = "summary";
  private static final String SOURCE = "source";
  private static final String URL = "url";
  private static final String DATETIME = "datetime";
  private static final String RELATED = "related";

  /**
   * Parses a company-news response.
   *
   * <p>News feeds contain the occasional broken item. An item without a headline, a link or a
   * publish time is skipped instead of failing the whole response.
   *
   * @param json the response from {@link FinnhubClient#fetchCompanyNews}
   * @return the articles that could be read, in the order Finnhub sent them
   */
  public List<NewsArticle> parseCompanyNews(JSONArray json) {
    final List<NewsArticle> articles = new ArrayList<>();
    for (int i = 0; i < json.length(); i++) {
      final JSONObject item = json.optJSONObject(i);
      if (item == null) {
        continue;
      }
      final String headline = item.optString(HEADLINE, "");
      final String url = item.optString(URL, "");
      final long epochSeconds = item.optLong(DATETIME, 0L);
      if (headline.isBlank() || url.isBlank() || epochSeconds <= 0) {
        continue;
      }
      articles.add(
          new NewsArticle(
              headline,
              item.optString(SUMMARY, ""),
              item.optString(SOURCE, ""),
              url,
              LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), MARKET_ZONE),
              parseSymbols(item.optString(RELATED, ""))));
    }
    return articles;
  }

  /** Finnhub sends related symbols as one string, e.g. "NVDA" or "NVDA,AMD". */
  private static List<String> parseSymbols(String related) {
    final List<String> symbols = new ArrayList<>();
    for (String part : related.split(",")) {
      if (!part.isBlank()) {
        symbols.add(part.trim());
      }
    }
    return symbols;
  }
}
