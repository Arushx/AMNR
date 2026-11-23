package use_case.watchlist;

/**
 * Entry point the watchlist controller calls. Every result is reported through {@link
 * WatchlistOutputBoundary}.
 */
public interface WatchlistInputBoundary {

  /**
   * Adds a ticker to the end of the user's watchlist.
   *
   * @param inputData the logged-in user and the ticker typed
   */
  void add(WatchlistInputData inputData);

  /**
   * Removes a ticker from the user's watchlist.
   *
   * @param inputData the logged-in user and the ticker typed
   */
  void remove(WatchlistInputData inputData);

  /**
   * Loads the user's watchlist.
   *
   * @param inputData the logged-in user; the ticker is ignored
   */
  void view(WatchlistInputData inputData);
}
