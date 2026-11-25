package interface_adapter.watchlist;

import interface_adapter.ViewModel;
import java.util.List;

/** What the watchlist view shows: whose list it is, the saved symbols, and the last outcome. */
public class WatchlistViewModel extends ViewModel {

  public static final String VIEW_NAME = "watchlist";

  private String username = "";
  private List<String> symbols = List.of();
  private String message;
  private String error;

  /** Creates the view model with nothing loaded. */
  public WatchlistViewModel() {
    super(VIEW_NAME);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
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
   * Replaces the displayed symbols with a copy of the given list.
   *
   * @param symbols the saved symbols, in the order they were added
   */
  public void setSymbols(List<String> symbols) {
    this.symbols = List.copyOf(symbols);
  }

  /**
   * Returns what just changed.
   *
   * @return e.g. "Added AAPL", or null for none
   */
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Returns the message to show.
   *
   * @return the error, or null for none
   */
  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
