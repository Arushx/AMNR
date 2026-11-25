package use_case.watchlist;

import java.util.List;

/** A user's watchlist after a successful add, remove or view. Immutable. */
public class WatchlistOutputData {

  private final String username;
  private final List<String> symbols;
  private final String message;

  /**
   * Creates the output data.
   *
   * @param username the owner's username
   * @param symbols the saved symbols, in the order they were added
   * @param message what changed, e.g. "Added AAPL", or null when nothing changed
   */
  public WatchlistOutputData(String username, List<String> symbols, String message) {
    this.username = username;
    this.symbols = List.copyOf(symbols);
    this.message = message;
  }

  public String getUsername() {
    return username;
  }

  /**
   * Returns the saved symbols.
   *
   * @return the symbols in the order they were added; unmodifiable
   */
  public List<String> getSymbols() {
    return symbols;
  }

  /**
   * Returns what changed.
   *
   * @return e.g. "Added AAPL", or null after a view
   */
  public String getMessage() {
    return message;
  }
}
