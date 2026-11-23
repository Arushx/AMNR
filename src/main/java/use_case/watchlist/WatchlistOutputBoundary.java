package use_case.watchlist;

/** Receives the result of a watchlist action and prepares what the user sees. */
public interface WatchlistOutputBoundary {

  /**
   * Called when an add, remove or view succeeded.
   *
   * @param outputData the watchlist as it is now saved
   */
  void prepareSuccessView(WatchlistOutputData outputData);

  /**
   * Called when the action failed; the saved watchlist is unchanged.
   *
   * @param errorMessage why, in words safe to show the user
   */
  void prepareFailView(String errorMessage);
}
