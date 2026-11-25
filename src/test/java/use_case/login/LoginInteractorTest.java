package use_case.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.InMemoryUserDataAccessObject;
import use_case.UserStorageException;

class LoginInteractorTest {

  /** Records which presenter method the interactor called. */
  private static class RecordingPresenter implements LoginOutputBoundary {
    LoginOutputData success;
    String error;

    @Override
    public void prepareSuccessView(LoginOutputData outputData) {
      success = outputData;
    }

    @Override
    public void prepareFailView(String errorMessage) {
      error = errorMessage;
    }
  }

  private InMemoryUserDataAccessObject dao;
  private RecordingPresenter presenter;
  private LoginInteractor interactor;

  @BeforeEach
  void setUp() throws UserStorageException {
    dao = new InMemoryUserDataAccessObject();
    dao.save(new User("Andrew", "pw"));
    presenter = new RecordingPresenter();
    interactor = new LoginInteractor(dao, presenter);
  }

  private void assertFailedWith(String message) {
    assertEquals(message, presenter.error);
    assertNull(presenter.success);
  }

  @Test
  void logsInWithCorrectPassword() {
    interactor.execute(new LoginInputData("Andrew", "pw"));

    assertEquals("Andrew", presenter.success.getUsername());
    assertNull(presenter.error);
  }

  @Test
  void returnsStoredUsernameWhenTypedInOtherCase() {
    interactor.execute(new LoginInputData("andrew", "pw"));

    assertEquals("Andrew", presenter.success.getUsername());
  }

  @Test
  void rejectsUnknownUser() {
    interactor.execute(new LoginInputData("nobody", "pw"));

    assertFailedWith("User not found");
  }

  @Test
  void rejectsWrongPassword() {
    interactor.execute(new LoginInputData("Andrew", "wrong"));

    assertFailedWith("Incorrect password");
  }

  @Test
  void passwordIsCaseSensitive() {
    interactor.execute(new LoginInputData("Andrew", "PW"));

    assertFailedWith("Incorrect password");
  }

  @Test
  void rejectsBlankUsername() {
    interactor.execute(new LoginInputData("", "pw"));

    assertFailedWith("Username cannot be empty");
  }

  @Test
  void rejectsNullUsername() {
    interactor.execute(new LoginInputData(null, "pw"));

    assertFailedWith("Username cannot be empty");
  }

  @Test
  void reportsStorageFailure() {
    dao.setFailing(true);

    interactor.execute(new LoginInputData("Andrew", "pw"));

    assertFailedWith("Database unavailable");
  }
}
