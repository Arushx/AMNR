package interface_adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import data_access.SqliteUserDataAccessObject;
import interface_adapter.logged_in.LoggedInViewModel;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginPresenter;
import interface_adapter.login.LoginViewModel;
import interface_adapter.signup.SignupController;
import interface_adapter.signup.SignupPresenter;
import interface_adapter.signup.SignupViewModel;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.UserStorageException;
import use_case.login.LoginInteractor;
import use_case.signup.SignupInteractor;

/**
 * Drives signup and login from the controllers to the view models against a real SQLite file,
 * with no view.
 */
class AuthFlowTest {

  @TempDir Path tempDir;

  @Test
  void signupThenLoginAcrossRestart() throws UserStorageException {
    String dbPath = tempDir.resolve("amnr.db").toString();
    ViewManagerModel viewManagerModel = new ViewManagerModel();
    SignupViewModel signupViewModel = new SignupViewModel();
    LoginViewModel loginViewModel = new LoginViewModel();
    LoggedInViewModel loggedInViewModel = new LoggedInViewModel();

    SqliteUserDataAccessObject dao = new SqliteUserDataAccessObject(dbPath);
    SignupController signup =
        new SignupController(
            new SignupInteractor(
                dao, new SignupPresenter(viewManagerModel, signupViewModel, loginViewModel)));

    signup.execute("demo", "demo", "demo");
    assertEquals(LoginViewModel.VIEW_NAME, viewManagerModel.getActiveView());
    assertEquals("demo", loginViewModel.getUsername());

    signup.execute("demo", "other", "other");
    assertEquals("Username already exists", signupViewModel.getError());

    SqliteUserDataAccessObject reopened = new SqliteUserDataAccessObject(dbPath);
    LoginController login =
        new LoginController(
            new LoginInteractor(
                reopened,
                new LoginPresenter(viewManagerModel, loginViewModel, loggedInViewModel)));

    login.execute("demo", "wrong");
    assertEquals("Incorrect password", loginViewModel.getError());
    assertEquals(LoginViewModel.VIEW_NAME, viewManagerModel.getActiveView());

    login.execute("DEMO", "demo");
    assertEquals(LoggedInViewModel.VIEW_NAME, viewManagerModel.getActiveView());
    assertEquals("demo", loggedInViewModel.getUsername());
    assertNull(loginViewModel.getError());
  }
}
