package interface_adapter.semantic_search;

import java.util.ArrayList;
import java.util.List;

/** Everything the semantic search view shows, already formatted as text. */
public class SemanticSearchState {

  private String summary = "";
  private List<String> resultLabels = new ArrayList<>();
  private List<String> resultSymbols = new ArrayList<>();
  private String error;

  /**
   * Returns a one-line description of the results, e.g. "oil: biggest companies first".
   *
   * @return the summary, or an empty string before the first search
   */
  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  /**
   * Returns one line per result, e.g. "XOM  Exxon Mobil Corporation  ·  Oil, Gas &amp; Fuels".
   *
   * @return the labels, in display order
   */
  public List<String> getResultLabels() {
    return resultLabels;
  }

  public void setResultLabels(List<String> resultLabels) {
    this.resultLabels = resultLabels;
  }

  /**
   * Returns the ticker of each result, lined up with {@link #getResultLabels()}. The view passes
   * the chosen one to Search Stock's controller to show that stock.
   *
   * @return the tickers
   */
  public List<String> getResultSymbols() {
    return resultSymbols;
  }

  public void setResultSymbols(List<String> resultSymbols) {
    this.resultSymbols = resultSymbols;
  }

  /**
   * Returns the error to show, if the last search failed.
   *
   * @return the message, or null if there is none
   */
  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
