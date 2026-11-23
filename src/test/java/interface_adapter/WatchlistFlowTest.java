package interface_adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import data_access.SqliteUserDataAccessObject;
import data_access.SqliteWatchlistDataAccessObject;
import interface_adapter.logged_in.LoggedInViewModel;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginPresenter;
import interface_adapter.login.LoginViewModel;
import interface_adapter.signup.SignupController;
import interface_adapter.signup.SignupPresenter;
import interface_adapter.signup.SignupViewModel;
import interface_adapter.watchlist.WatchlistController;
import interface_adapter.watchlist.WatchlistPresenter;
import interface_adapter.watchlist.WatchlistState;
import interface_adapter.watchlist.WatchlistViewModel;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.UserStorageException;
import use_case.WatchlistStorageException;
import use_case.login.LoginInteractor;
import use_case.signup.SignupInteractor;
import use_case.watchlist.WatchlistInteractor;

/**
 * Drives signup, login and the watchlist from the controllers to the view models against a real
 * SQLite file, with no view.
 */
class WatchlistFlowTest {

  @TempDir Path tempDir;

  private String dbPath;
  private final ViewManagerModel viewManagerModel = new ViewManagerModel();
  private final LoginViewModel loginViewModel = new LoginViewModel();
  private final LoggedInViewModel loggedInViewModel = new LoggedInViewModel();
  private final WatchlistViewModel watchlistViewModel = new WatchlistViewModel();

  @Test
  void addViewRemoveAcrossRestart() throws UserStorageException, WatchlistStorageException {
    dbPath = tempDir.resolve("amnr.db").toString();
    SqliteUserDataAccessObject users = new SqliteUserDataAccessObject(dbPath);
    signUp(users, "demo");
    signUp(users, "other");
    logIn(users, "demo");
    final String username = loggedInViewModel.getState().getUsername();

    WatchlistController watchlist = newWatchlistController();
    watchlist.add(username, "aapl");
    watchlist.add(username, "MSFT");
    assertEquals(List.of("AAPL", "MSFT"), state().getSymbols());
    assertEquals("Added MSFT", state().getMessage());

    watchlist.add(username, "AAPL");
    assertEquals("AAPL is already in your watchlist", state().getError());
    assertEquals(List.of("AAPL", "MSFT"), state().getSymbols());

    WatchlistController reopened = newWatchlistController();
    reopened.view(username);
    assertEquals(List.of("AAPL", "MSFT"), state().getSymbols());
    assertNull(state().getError());

    reopened.remove(username, "aapl");
    assertEquals("Removed AAPL", state().getMessage());
    reopened.view(username);
    assertEquals(List.of("MSFT"), state().getSymbols());

    logIn(users, "other");
    reopened.view(loggedInViewModel.getState().getUsername());
    assertEquals("other", state().getUsername());
    assertEquals(List.of(), state().getSymbols());
  }

  private WatchlistController newWatchlistController() throws WatchlistStorageException {
    return new WatchlistController(
        new WatchlistInteractor(
            new SqliteWatchlistDataAccessObject(dbPath),
            new WatchlistPresenter(watchlistViewModel)));
  }

  private void signUp(SqliteUserDataAccessObject users, String username) {
    new SignupController(
            new SignupInteractor(
                users,
                new SignupPresenter(viewManagerModel, new SignupViewModel(), loginViewModel)))
        .execute(username, "pw", "pw");
  }

  private void logIn(SqliteUserDataAccessObject users, String username) {
    new LoginController(
            new LoginInteractor(
                users, new LoginPresenter(viewManagerModel, loginViewModel, loggedInViewModel)))
        .execute(username, "pw");
    assertEquals(username, loggedInViewModel.getState().getUsername());
  }

  private WatchlistState state() {
    return watchlistViewModel.getState();
  }
}
