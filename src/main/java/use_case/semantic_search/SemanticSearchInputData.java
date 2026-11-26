package use_case.semantic_search;

/** What the user typed into the semantic search box, e.g. "best selling oil stocks". */
public class SemanticSearchInputData {

  private final String query;

  /**
   * Creates the input data.
   *
   * @param query the search sentence
   */
  public SemanticSearchInputData(String query) {
    this.query = query;
  }

  public String getQuery() {
    return query;
  }
}
