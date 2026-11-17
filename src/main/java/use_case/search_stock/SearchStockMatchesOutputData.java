package use_case.search_stock;

import java.util.Collections;
import java.util.List;

/**
 * The result of a search that wasn't a known ticker: the best company-name matches for the user
 * to choose from, best first.
 *
 * <p>The lists line up: {@code symbols.get(i)} is the ticker of {@code names.get(i)}, which trades
 * on {@code exchanges.get(i)}.
 */
public class SearchStockMatchesOutputData {

  private final String query;
  private final List<String> symbols;
  private final List<String> names;
  private final List<String> exchanges;

  /**
   * Creates the output data.
   *
   * @param query what the user typed
   * @param symbols the matching tickers
   * @param names the matching company names
   * @param exchanges where each match trades
   */
  public SearchStockMatchesOutputData(
      String query, List<String> symbols, List<String> names, List<String> exchanges) {
    this.query = query;
    this.symbols = Collections.unmodifiableList(symbols);
    this.names = Collections.unmodifiableList(names);
    this.exchanges = Collections.unmodifiableList(exchanges);
  }

  public String getQuery() {
    return query;
  }

  public List<String> getSymbols() {
    return symbols;
  }

  public List<String> getNames() {
    return names;
  }

  public List<String> getExchanges() {
    return exchanges;
  }
}
