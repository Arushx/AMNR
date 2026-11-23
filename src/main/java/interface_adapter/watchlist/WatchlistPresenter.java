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
   * @param watchlistViewModel the watchlist view's state
   */
  public WatchlistPresenter(WatchlistViewModel watchlistViewModel) {
    this.watchlistViewModel = watchlistViewModel;
  }

  @Override
  public void prepareSuccessView(WatchlistOutputData outputData) {
    watchlistViewModel.setState(
        new WatchlistState(
            outputData.getUsername(), outputData.getSymbols(), outputData.getMessage(), null));
    watchlistViewModel.firePropertyChange();
  }

  @Override
  public void prepareFailView(String errorMessage) {
    watchlistViewModel.setState(watchlistViewModel.getState().withError(errorMessage));
    watchlistViewModel.firePropertyChange();
  }
}
