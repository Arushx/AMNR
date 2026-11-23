package interface_adapter.watchlist;

import java.util.List;

/** What the watchlist view shows. Immutable; build a new one to change it. */
public class WatchlistState {

  private final String username;
  private final List<String> symbols;
  private final String message;
  private final String error;

  /** Creates the state for when nothing has been loaded. */
  public WatchlistState() {
    this("", List.of(), null, null);
  }

  /**
   * Creates a state.
   *
   * @param username whose watchlist is shown
   * @param symbols the saved symbols, in the order they were added
   * @param message what just changed, e.g. "Added AAPL", or null for none
   * @param error the message to show, or null for none
   */
  public WatchlistState(String username, List<String> symbols, String message, String error) {
    this.username = username;
    this.symbols = List.copyOf(symbols);
    this.message = message;
    this.error = error;
  }

  /**
   * Returns a copy showing an error, with the same symbols and no change message.
   *
   * @param newError the message to show
   * @return the new state
   */
  public WatchlistState withError(String newError) {
    return new WatchlistState(username, symbols, null, newError);
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

  public String getMessage() {
    return message;
  }

  public String getError() {
    return error;
  }
}
