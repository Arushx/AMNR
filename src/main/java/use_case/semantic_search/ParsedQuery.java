package use_case.semantic_search;

/**
 * A search sentence split into what it is about and how to order the results.
 *
 * <p>For "best selling oil stocks": the topic is "oil" and the order is {@link SortOrder#LARGEST}.
 */
public class ParsedQuery {

  private final String topic;
  private final SortOrder sortOrder;

  /**
   * Creates the parsed query.
   *
   * @param topic the words that describe the kind of company, e.g. "oil"
   * @param sortOrder how to order the matches
   */
  public ParsedQuery(String topic, SortOrder sortOrder) {
    this.topic = topic;
    this.sortOrder = sortOrder;
  }

  public String getTopic() {
    return topic;
  }

  public SortOrder getSortOrder() {
    return sortOrder;
  }
}
