package app;

import data_access.SqliteUserDataAccessObject;
import data_access.SqliteWatchlistDataAccessObject;
import interface_adapter.ViewManagerModel;
import interface_adapter.logged_in.LoggedInViewModel;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginPresenter;
import interface_adapter.login.LoginViewModel;
import interface_adapter.signup.SignupController;
import interface_adapter.signup.SignupPresenter;
import interface_adapter.signup.SignupViewModel;
import interface_adapter.watchlist.WatchlistController;
import interface_adapter.watchlist.WatchlistPresenter;
import interface_adapter.watchlist.WatchlistViewModel;
import java.util.Scanner;
import use_case.UserStorageException;
import use_case.WatchlistStorageException;
import use_case.login.LoginInteractor;
import use_case.signup.SignupInteractor;
import use_case.watchlist.WatchlistInteractor;

/**
 * Console stand-in for the signup, login and watchlist views, backed by the real SQLite database.
 *
 * <p>Wires the same controllers, interactors, presenters and view models the Swing app will use;
 * console listeners on the view models take the place of the views. Delete once the Swing views
 * are wired in {@link AppBuilder}.
 */
public class AuthDemo {

  private static final String DEFAULT_DB_PATH = "amnr.db";

  /**
   * Starts the console demo.
   *
   * @param args optional path to the database file; defaults to amnr.db in the working directory
   * @throws UserStorageException if the user database can't be opened
   * @throws WatchlistStorageException if the watchlist database can't be opened
   */
  public static void main(String[] args) throws UserStorageException, WatchlistStorageException {
    final String dbPath = args.length > 0 ? args[0] : DEFAULT_DB_PATH;
    final SqliteUserDataAccessObject dao = new SqliteUserDataAccessObject(dbPath);
    final SqliteWatchlistDataAccessObject watchlistDao =
        new SqliteWatchlistDataAccessObject(dbPath);

    final ViewManagerModel viewManagerModel = new ViewManagerModel();
    final SignupViewModel signupViewModel = new SignupViewModel();
    final LoginViewModel loginViewModel = new LoginViewModel();
    final LoggedInViewModel loggedInViewModel = new LoggedInViewModel();
    final WatchlistViewModel watchlistViewModel = new WatchlistViewModel();
    addConsoleListeners(viewManagerModel, signupViewModel, loginViewModel, loggedInViewModel);
    addWatchlistListener(watchlistViewModel);

    final SignupController signup =
        new SignupController(
            new SignupInteractor(
                dao, new SignupPresenter(viewManagerModel, signupViewModel, loginViewModel)));
    final LoginController login =
        new LoginController(
            new LoginInteractor(
                dao, new LoginPresenter(viewManagerModel, loginViewModel, loggedInViewModel)));
    final WatchlistController watchlist =
        new WatchlistController(
            new WatchlistInteractor(watchlistDao, new WatchlistPresenter(watchlistViewModel)));

    System.out.println("Using database: " + dbPath);
    final Scanner in = new Scanner(System.in);
    while (true) {
      System.out.print(
          "\n[1] Sign up  [2] Log in  [3] Add ticker  [4] Remove ticker  [5] View watchlist"
              + "  [q] Quit > ");
      if (!in.hasNextLine()) {
        return;
      }
      final String choice = in.nextLine().trim();
      if ("1".equals(choice)) {
        signup.execute(ask(in, "Username"), ask(in, "Password"), ask(in, "Repeat password"));
      } else if ("2".equals(choice)) {
        login.execute(ask(in, "Username"), ask(in, "Password"));
        if (LoggedInViewModel.VIEW_NAME.equals(viewManagerModel.getActiveView())) {
          watchlist.view(loggedInViewModel.getUsername());
        }
      } else if ("3".equals(choice)) {
        watchlist.add(loggedInViewModel.getUsername(), ask(in, "Ticker"));
      } else if ("4".equals(choice)) {
        watchlist.remove(loggedInViewModel.getUsername(), ask(in, "Ticker"));
      } else if ("5".equals(choice)) {
        watchlist.view(loggedInViewModel.getUsername());
      } else if ("q".equalsIgnoreCase(choice)) {
        return;
      }
    }
  }

  /** Prints what each view would display when its view model fires. */
  private static void addConsoleListeners(
      ViewManagerModel viewManagerModel,
      SignupViewModel signupViewModel,
      LoginViewModel loginViewModel,
      LoggedInViewModel loggedInViewModel) {
    viewManagerModel.addPropertyChangeListener(
        event -> System.out.println("[view] now showing: " + viewManagerModel.getActiveView()));
    signupViewModel.addPropertyChangeListener(
        event -> printError("sign up", signupViewModel.getError()));
    loginViewModel.addPropertyChangeListener(
        event -> printError("log in", loginViewModel.getError()));
    loggedInViewModel.addPropertyChangeListener(
        event -> System.out.println("[logged in] Welcome, " + loggedInViewModel.getUsername()));
  }

  /** Prints the watchlist, or the error, whenever the watchlist view model fires. */
  private static void addWatchlistListener(WatchlistViewModel watchlistViewModel) {
    watchlistViewModel.addPropertyChangeListener(
        event -> {
          if (watchlistViewModel.getError() != null) {
            printError("watchlist", watchlistViewModel.getError());
            return;
          }
          if (watchlistViewModel.getMessage() != null) {
            System.out.println("[watchlist] " + watchlistViewModel.getMessage());
          }
          System.out.println(
              "[watchlist] "
                  + watchlistViewModel.getUsername()
                  + ": "
                  + watchlistViewModel.getSymbols());
        });
  }

  private static void printError(String viewName, String error) {
    if (error != null) {
      System.out.println("[" + viewName + "] Error: " + error);
    }
  }

  private static String ask(Scanner in, String label) {
    System.out.print(label + ": ");
    return in.hasNextLine() ? in.nextLine() : "";
  }
}
