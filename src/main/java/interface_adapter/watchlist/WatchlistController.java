package interface_adapter.watchlist;

import use_case.watchlist.WatchlistInputBoundary;
import use_case.watchlist.WatchlistInputData;

/**
 * Turns watchlist actions in the view into calls to the watchlist use case.
 *
 * <p>Takes plain strings so it has no dependency on Swing; the view passes the username from
 * {@code LoggedInState}. Validation happens in the use case.
 */
public class WatchlistController {

  private final WatchlistInputBoundary watchlistUseCase;

  /**
   * Creates the controller.
   *
   * @param watchlistUseCase the use case to run
   */
  public WatchlistController(WatchlistInputBoundary watchlistUseCase) {
    this.watchlistUseCase = watchlistUseCase;
  }

  /**
   * Adds a ticker to the user's watchlist.
   *
   * @param username the logged-in username
   * @param symbol the ticker typed
   */
  public void add(String username, String symbol) {
    watchlistUseCase.add(new WatchlistInputData(username, symbol));
  }

  /**
   * Removes a ticker from the user's watchlist.
   *
   * @param username the logged-in username
   * @param symbol the ticker to remove
   */
  public void remove(String username, String symbol) {
    watchlistUseCase.remove(new WatchlistInputData(username, symbol));
  }

  /**
   * Loads the user's watchlist.
   *
   * @param username the logged-in username
   */
  public void view(String username) {
    watchlistUseCase.view(new WatchlistInputData(username));
  }
}
