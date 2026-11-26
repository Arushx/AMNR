package use_case.semantic_search;

import java.util.Collections;
import java.util.List;

/** The result of ranking stocks for a search sentence. */
public class Ranking {

  private final ParsedQuery query;
  private final List<RankedStock> stocks;
  private final String fallbackReason;

  /**
   * Creates the ranking.
   *
   * @param query the parsed search
   * @param stocks the matches, in display order
   * @param fallbackReason why keyword matching was used instead of meaning, or null if it wasn't
   */
  public Ranking(ParsedQuery query, List<RankedStock> stocks, String fallbackReason) {
    this.query = query;
    this.stocks = Collections.unmodifiableList(stocks);
    this.fallbackReason = fallbackReason;
  }

  public ParsedQuery getQuery() {
    return query;
  }

  public List<RankedStock> getStocks() {
    return stocks;
  }

  /**
   * Says whether keyword matching was used because the embedding model couldn't run.
   *
   * @return true if the fallback was used
   */
  public boolean usedFallback() {
    return fallbackReason != null;
  }

  public String getFallbackReason() {
    return fallbackReason;
  }
}
