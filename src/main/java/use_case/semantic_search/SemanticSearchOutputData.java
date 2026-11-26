package use_case.semantic_search;

import java.util.Collections;
import java.util.List;

/**
 * The stocks that matched a search sentence, in display order.
 *
 * <p>The lists line up: entry {@code i} of each list describes the same stock.
 */
public class SemanticSearchOutputData {

  private final String query;
  private final String topic;
  private final SortOrder sortOrder;
  private final List<String> symbols;
  private final List<String> names;
  private final List<String> industries;
  private final List<String> sizes;
  private final List<Double> scores;
  private final String fallbackReason;

  /**
   * Creates the output data.
   *
   * @param query what the user typed
   * @param topic what was searched for after removing ranking and filler words, e.g. "oil"
   * @param sortOrder how the results are ordered
   * @param symbols the tickers
   * @param names the company names
   * @param industries each company's industry (or sector, if the industry is unknown)
   * @param sizes each company's size label, e.g. "Mega Cap"; empty if unknown
   * @param scores how well each one matched; higher is better
   * @param fallbackReason why keyword matching was used instead of meaning, or null
   */
  public SemanticSearchOutputData(
      String query,
      String topic,
      SortOrder sortOrder,
      List<String> symbols,
      List<String> names,
      List<String> industries,
      List<String> sizes,
      List<Double> scores,
      String fallbackReason) {
    this.query = query;
    this.topic = topic;
    this.sortOrder = sortOrder;
    this.symbols = Collections.unmodifiableList(symbols);
    this.names = Collections.unmodifiableList(names);
    this.industries = Collections.unmodifiableList(industries);
    this.sizes = Collections.unmodifiableList(sizes);
    this.scores = Collections.unmodifiableList(scores);
    this.fallbackReason = fallbackReason;
  }

  public String getQuery() {
    return query;
  }

  public String getTopic() {
    return topic;
  }

  public SortOrder getSortOrder() {
    return sortOrder;
  }

  public List<String> getSymbols() {
    return symbols;
  }

  public List<String> getNames() {
    return names;
  }

  public List<String> getIndustries() {
    return industries;
  }

  public List<String> getSizes() {
    return sizes;
  }

  public List<Double> getScores() {
    return scores;
  }

  public String getFallbackReason() {
    return fallbackReason;
  }
}
