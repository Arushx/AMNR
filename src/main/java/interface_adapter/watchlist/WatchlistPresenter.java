package interface_adapter.watchlist;

import use_case.watchlist.WatchlistOutputBoundary;
import use_case.watchlist.WatchlistOutputData;

/**
 * Updates the watchlist view model after an add, remove or view: on success it shows the saved
 * list and what changed; on failure it shows the error and keeps the list on screen.
 */
public class WatchlistPresenter implements WatchlistOutputBoundary {

  private final WatchlistViewModel watchlistViewModel;

  /**
   * Creates the presenter.
   *
   * @param watchlistViewModel what the watchlist view shows
   */
  public WatchlistPresenter(WatchlistViewModel watchlistViewModel) {
    this.watchlistViewModel = watchlistViewModel;
  }

  @Override
  public void prepareSuccessView(WatchlistOutputData outputData) {
    watchlistViewModel.setUsername(outputData.getUsername());
    watchlistViewModel.setSymbols(outputData.getSymbols());
    watchlistViewModel.setMessage(outputData.getMessage());
    watchlistViewModel.setError(null);
    watchlistViewModel.firePropertyChange();
  }

  @Override
  public void prepareFailView(String errorMessage) {
    watchlistViewModel.setMessage(null);
    watchlistViewModel.setError(errorMessage);
    watchlistViewModel.firePropertyChange();
  }
}
