package app;

import data_access.SqliteUserDataAccessObject;
import interface_adapter.ViewManagerModel;
import interface_adapter.logged_in.LoggedInState;
import interface_adapter.logged_in.LoggedInViewModel;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginPresenter;
import interface_adapter.login.LoginState;
import interface_adapter.login.LoginViewModel;
import interface_adapter.signup.SignupController;
import interface_adapter.signup.SignupPresenter;
import interface_adapter.signup.SignupState;
import interface_adapter.signup.SignupViewModel;
import java.util.Scanner;
import use_case.UserStorageException;
import use_case.login.LoginInteractor;
import use_case.signup.SignupInteractor;

/**
 * Console stand-in for the signup and login views, backed by the real SQLite database.
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
   * @throws UserStorageException if the database can't be opened
   */
  public static void main(String[] args) throws UserStorageException {
    final String dbPath = args.length > 0 ? args[0] : DEFAULT_DB_PATH;
    final SqliteUserDataAccessObject dao = new SqliteUserDataAccessObject(dbPath);

    final ViewManagerModel viewManagerModel = new ViewManagerModel();
    final SignupViewModel signupViewModel = new SignupViewModel();
    final LoginViewModel loginViewModel = new LoginViewModel();
    final LoggedInViewModel loggedInViewModel = new LoggedInViewModel();
    addConsoleListeners(viewManagerModel, signupViewModel, loginViewModel, loggedInViewModel);

    final SignupController signup =
        new SignupController(
            new SignupInteractor(
                dao, new SignupPresenter(viewManagerModel, signupViewModel, loginViewModel)));
    final LoginController login =
        new LoginController(
            new LoginInteractor(
                dao, new LoginPresenter(viewManagerModel, loginViewModel, loggedInViewModel)));

    System.out.println("Using database: " + dbPath);
    final Scanner in = new Scanner(System.in);
    while (true) {
      System.out.print("\n[1] Sign up  [2] Log in  [q] Quit > ");
      if (!in.hasNextLine()) {
        return;
      }
      final String choice = in.nextLine().trim();
      if ("1".equals(choice)) {
        signup.execute(ask(in, "Username"), ask(in, "Password"), ask(in, "Repeat password"));
      } else if ("2".equals(choice)) {
        login.execute(ask(in, "Username"), ask(in, "Password"));
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
        event -> System.out.println("[view] now showing: " + event.getNewValue()));
    signupViewModel.addPropertyChangeListener(
        event -> printError("sign up", ((SignupState) event.getNewValue()).getError()));
    loginViewModel.addPropertyChangeListener(
        event -> printError("log in", ((LoginState) event.getNewValue()).getError()));
    loggedInViewModel.addPropertyChangeListener(
        event ->
            System.out.println(
                "[logged in] Welcome, " + ((LoggedInState) event.getNewValue()).getUsername()));
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
