package app;

import data_access.SqliteUserDataAccessObject;
import interface_adapter.login.LoginController;
import interface_adapter.signup.SignupController;
import java.util.Scanner;
import use_case.UserStorageException;
import use_case.login.LoginInteractor;
import use_case.login.LoginOutputBoundary;
import use_case.login.LoginOutputData;
import use_case.signup.SignupInteractor;
import use_case.signup.SignupOutputBoundary;
import use_case.signup.SignupOutputData;

/**
 * Console stand-in for the signup and login views, backed by the real SQLite database.
 *
 * <p>Delete once the Swing views are wired in {@link AppBuilder}.
 */
public class AuthDemo {

  private static final String DEFAULT_DB_PATH = "amnr.db";

  /** Prints each result to the console in place of a presenter and view. */
  private static class ConsolePresenter implements SignupOutputBoundary, LoginOutputBoundary {

    @Override
    public void prepareSuccessView(SignupOutputData outputData) {
      System.out.println("Signed up: " + outputData.getUsername());
    }

    @Override
    public void prepareSuccessView(LoginOutputData outputData) {
      System.out.println("Welcome, " + outputData.getUsername() + "!");
    }

    @Override
    public void prepareFailView(String errorMessage) {
      System.out.println("Error: " + errorMessage);
    }
  }

  /**
   * Starts the console demo.
   *
   * @param args optional path to the database file; defaults to amnr.db in the working directory
   * @throws UserStorageException if the database can't be opened
   */
  public static void main(String[] args) throws UserStorageException {
    final String dbPath = args.length > 0 ? args[0] : DEFAULT_DB_PATH;
    final SqliteUserDataAccessObject dao = new SqliteUserDataAccessObject(dbPath);
    final ConsolePresenter presenter = new ConsolePresenter();
    final SignupController signup = new SignupController(new SignupInteractor(dao, presenter));
    final LoginController login = new LoginController(new LoginInteractor(dao, presenter));

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

  private static String ask(Scanner in, String label) {
    System.out.print(label + ": ");
    return in.hasNextLine() ? in.nextLine() : "";
  }
}
