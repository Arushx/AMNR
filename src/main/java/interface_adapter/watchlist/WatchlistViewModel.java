package interface_adapter.watchlist;

import interface_adapter.ViewModel;

/** The view model for the logged-in user's watchlist. */
public class WatchlistViewModel extends ViewModel<WatchlistState> {

  public static final String VIEW_NAME = "watchlist";

  /** Creates the view model with nothing loaded. */
  public WatchlistViewModel() {
    super(VIEW_NAME, new WatchlistState());
  }
}
