package use_case.watchlist;

import entity.Watchlist;
import use_case.WatchlistStorageException;

/** Adds, removes and lists the ticker symbols a logged-in user has saved. */
public class WatchlistInteractor implements WatchlistInputBoundary {

  private static final String NOT_LOGGED_IN = "You must be logged in to use the watchlist";

  private final WatchlistDataAccessInterface watchlistDataAccess;
  private final WatchlistOutputBoundary presenter;

  /**
   * Creates the interactor.
   *
   * @param watchlistDataAccess where watchlists are stored
   * @param presenter receives every result
   */
  public WatchlistInteractor(
      WatchlistDataAccessInterface watchlistDataAccess, WatchlistOutputBoundary presenter) {
    this.watchlistDataAccess = watchlistDataAccess;
    this.presenter = presenter;
  }

  @Override
  public void add(WatchlistInputData inputData) {
    final String error = validate(inputData);
    if (error != null) {
      presenter.prepareFailView(error);
      return;
    }
    final String username = inputData.getUsername();
    final String symbol = Watchlist.normalizeSymbol(inputData.getSymbol());
    try {
      final Watchlist current = watchlistDataAccess.findByUsername(username);
      if (current.contains(symbol)) {
        presenter.prepareFailView(symbol + " is already in your watchlist");
        return;
      }
      watchlistDataAccess.addSymbol(username, symbol);
      presenter.prepareSuccessView(
          new WatchlistOutputData(
              username, current.withSymbol(symbol).getSymbols(), "Added " + symbol));
    } catch (WatchlistStorageException e) {
      presenter.prepareFailView(e.getMessage());
    }
  }

  @Override
  public void remove(WatchlistInputData inputData) {
    final String error = validate(inputData);
    if (error != null) {
      presenter.prepareFailView(error);
      return;
    }
    final String username = inputData.getUsername();
    final String symbol = Watchlist.normalizeSymbol(inputData.getSymbol());
    try {
      final Watchlist current = watchlistDataAccess.findByUsername(username);
      if (!current.contains(symbol)) {
        presenter.prepareFailView(symbol + " is not in your watchlist");
        return;
      }
      watchlistDataAccess.removeSymbol(username, symbol);
      presenter.prepareSuccessView(
          new WatchlistOutputData(
              username, current.withoutSymbol(symbol).getSymbols(), "Removed " + symbol));
    } catch (WatchlistStorageException e) {
      presenter.prepareFailView(e.getMessage());
    }
  }

  @Override
  public void view(WatchlistInputData inputData) {
    final String username = inputData.getUsername();
    if (isBlank(username)) {
      presenter.prepareFailView(NOT_LOGGED_IN);
      return;
    }
    try {
      final Watchlist watchlist = watchlistDataAccess.findByUsername(username);
      presenter.prepareSuccessView(
          new WatchlistOutputData(username, watchlist.getSymbols(), null));
    } catch (WatchlistStorageException e) {
      presenter.prepareFailView(e.getMessage());
    }
  }

  /** Returns the message to show if the username or ticker is unusable, or null if both are ok. */
  private static String validate(WatchlistInputData inputData) {
    if (isBlank(inputData.getUsername())) {
      return NOT_LOGGED_IN;
    }
    final String symbol = inputData.getSymbol();
    if (isBlank(symbol)) {
      return "Ticker cannot be empty";
    }
    if (!Watchlist.isValidSymbol(symbol)) {
      return "Invalid ticker: " + symbol;
    }
    return null;
  }

  private static boolean isBlank(String text) {
    return text == null || text.isBlank();
  }
}
