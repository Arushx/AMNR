package interface_adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import data_access.SqliteUserDataAccessObject;
import interface_adapter.login.LoginController;
import interface_adapter.signup.SignupController;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.UserStorageException;
import use_case.login.LoginInteractor;
import use_case.login.LoginOutputBoundary;
import use_case.login.LoginOutputData;
import use_case.signup.SignupInteractor;
import use_case.signup.SignupOutputBoundary;
import use_case.signup.SignupOutputData;

/** Drives signup and login through the controllers against a real SQLite file, with no view. */
class AuthFlowTest {

  @TempDir Path tempDir;

  /** Logs every result as "signup ok: x", "login failed: y", ... in call order. */
  private static class LoggingPresenter implements SignupOutputBoundary, LoginOutputBoundary {
    final List<String> log = new ArrayList<>();

    @Override
    public void prepareSuccessView(SignupOutputData outputData) {
      log.add("signup ok: " + outputData.getUsername());
    }

    @Override
    public void prepareSuccessView(LoginOutputData outputData) {
      log.add("login ok: " + outputData.getUsername());
    }

    @Override
    public void prepareFailView(String errorMessage) {
      log.add("failed: " + errorMessage);
    }
  }

  @Test
  void signupThenLoginAcrossRestart() throws UserStorageException {
    String dbPath = tempDir.resolve("amnr.db").toString();
    LoggingPresenter presenter = new LoggingPresenter();

    SqliteUserDataAccessObject dao = new SqliteUserDataAccessObject(dbPath);
    SignupController signup = new SignupController(new SignupInteractor(dao, presenter));
    signup.execute("demo", "demo", "demo");
    signup.execute("demo", "other", "other");

    SqliteUserDataAccessObject reopened = new SqliteUserDataAccessObject(dbPath);
    LoginController login = new LoginController(new LoginInteractor(reopened, presenter));
    login.execute("demo", "wrong");
    login.execute("DEMO", "demo");

    assertEquals(
        List.of(
            "signup ok: demo",
            "failed: Username already exists",
            "failed: Incorrect password",
            "login ok: demo"),
        presenter.log);
  }
}
