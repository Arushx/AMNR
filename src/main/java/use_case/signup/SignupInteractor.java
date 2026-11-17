package use_case.signup;

import entity.User;
import use_case.UserStorageException;

/**
 * Validates a new username and password and saves the account.
 *
 * <p>Input is checked here, before {@link User} is built, so the user gets a readable message
 * instead of the entity's {@link IllegalArgumentException}.
 */
public class SignupInteractor implements SignupInputBoundary {

  private final SignupDataAccessInterface userDataAccess;
  private final SignupOutputBoundary presenter;

  /**
   * Creates the interactor.
   *
   * @param userDataAccess where accounts are checked and saved
   * @param presenter receives the result
   */
  public SignupInteractor(
      SignupDataAccessInterface userDataAccess, SignupOutputBoundary presenter) {
    this.userDataAccess = userDataAccess;
    this.presenter = presenter;
  }

  @Override
  public void execute(SignupInputData inputData) {
    final String error = validate(inputData);
    if (error != null) {
      presenter.prepareFailView(error);
      return;
    }
    try {
      if (userDataAccess.existsByUsername(inputData.getUsername())) {
        presenter.prepareFailView("Username already exists");
        return;
      }
      userDataAccess.save(new User(inputData.getUsername(), inputData.getPassword()));
      presenter.prepareSuccessView(new SignupOutputData(inputData.getUsername()));
    } catch (UserStorageException e) {
      presenter.prepareFailView(e.getMessage());
    }
  }

  /** Returns the first problem with the input, or null if it is valid. */
  private static String validate(SignupInputData inputData) {
    final String username = inputData.getUsername();
    final String password = inputData.getPassword();
    if (username == null || username.isBlank()) {
      return "Username cannot be empty";
    }
    if (username.chars().anyMatch(Character::isWhitespace)) {
      return "Username cannot contain spaces";
    }
    if (password == null || password.isBlank()) {
      return "Password cannot be empty";
    }
    if (!password.equals(inputData.getRepeatPassword())) {
      return "Passwords do not match";
    }
    return null;
  }
}
