package use_case.signup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.InMemoryUserDataAccessObject;
import use_case.UserStorageException;

class SignupInteractorTest {

  /** Records which presenter method the interactor called. */
  private static class RecordingPresenter implements SignupOutputBoundary {
    SignupOutputData success;
    String error;

    @Override
    public void prepareSuccessView(SignupOutputData outputData) {
      success = outputData;
    }

    @Override
    public void prepareFailView(String errorMessage) {
      error = errorMessage;
    }
  }

  private InMemoryUserDataAccessObject dao;
  private RecordingPresenter presenter;
  private SignupInteractor interactor;

  @BeforeEach
  void setUp() {
    dao = new InMemoryUserDataAccessObject();
    presenter = new RecordingPresenter();
    interactor = new SignupInteractor(dao, presenter);
  }

  private void assertFailedWith(String message) {
    assertEquals(message, presenter.error);
    assertNull(presenter.success);
  }

  @Test
  void savesUserAndReportsSuccess() throws UserStorageException {
    interactor.execute(new SignupInputData("andrew", "pw", "pw"));

    assertEquals("andrew", presenter.success.getUsername());
    assertNull(presenter.error);
    assertEquals("pw", dao.findByUsername("andrew").get().getPassword());
  }

  @Test
  void rejectsBlankUsername() {
    interactor.execute(new SignupInputData(" ", "pw", "pw"));

    assertFailedWith("Username cannot be empty");
  }

  @Test
  void rejectsNullUsername() {
    interactor.execute(new SignupInputData(null, "pw", "pw"));

    assertFailedWith("Username cannot be empty");
  }

  @Test
  void rejectsUsernameWithSpaces() {
    interactor.execute(new SignupInputData("andrew tu", "pw", "pw"));

    assertFailedWith("Username cannot contain spaces");
  }

  @Test
  void rejectsBlankPassword() {
    interactor.execute(new SignupInputData("andrew", "", ""));

    assertFailedWith("Password cannot be empty");
  }

  @Test
  void rejectsMismatchedPasswords() throws UserStorageException {
    interactor.execute(new SignupInputData("andrew", "pw", "other"));

    assertFailedWith("Passwords do not match");
    assertFalse(dao.existsByUsername("andrew"));
  }

  @Test
  void rejectsTakenUsernameIgnoringCase() {
    interactor.execute(new SignupInputData("andrew", "pw", "pw"));
    presenter.success = null;

    interactor.execute(new SignupInputData("ANDREW", "pw2", "pw2"));

    assertFailedWith("Username already exists");
  }

  @Test
  void reportsStorageFailure() {
    dao.setFailing(true);

    interactor.execute(new SignupInputData("andrew", "pw", "pw"));

    assertFailedWith("Database unavailable");
  }

  @Test
  void doesNotSaveWhenValidationFails() throws UserStorageException {
    interactor.execute(new SignupInputData("andrew tu", "pw", "pw"));

    assertTrue(dao.findByUsername("andrew tu").isEmpty());
  }
}
