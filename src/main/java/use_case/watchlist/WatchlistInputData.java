package use_case.watchlist;

/** Who is asking and which ticker they typed. Values are raw and not yet validated. */
public class WatchlistInputData {

  private final String username;
  private final String symbol;

  /**
   * Creates input data for viewing a watchlist, where no ticker is involved.
   *
   * @param username the logged-in username
   */
  public WatchlistInputData(String username) {
    this(username, null);
  }

  /**
   * Creates input data for adding or removing a ticker.
   *
   * @param username the logged-in username
   * @param symbol the ticker typed
   */
  public WatchlistInputData(String username, String symbol) {
    this.username = username;
    this.symbol = symbol;
  }

  public String getUsername() {
    return username;
  }

  /**
   * Returns the ticker typed.
   *
   * @return the ticker, or null when viewing
   */
  public String getSymbol() {
    return symbol;
  }
}
