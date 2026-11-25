package use_case.login;

import entity.User;
import java.util.Optional;
import use_case.UserStorageException;

/** Checks a username and password against the stored accounts. */
public class LoginInteractor implements LoginInputBoundary {

  private final LoginDataAccessInterface userDataAccess;
  private final LoginOutputBoundary presenter;

  /**
   * Creates the interactor.
   *
   * @param userDataAccess where accounts are looked up
   * @param presenter receives the result
   */
  public LoginInteractor(LoginDataAccessInterface userDataAccess, LoginOutputBoundary presenter) {
    this.userDataAccess = userDataAccess;
    this.presenter = presenter;
  }

  @Override
  public void execute(LoginInputData inputData) {
    final String username = inputData.getUsername();
    if (username == null || username.isBlank()) {
      presenter.prepareFailView("Username cannot be empty");
      return;
    }
    try {
      final Optional<User> user = userDataAccess.findByUsername(username);
      if (user.isEmpty()) {
        presenter.prepareFailView("User not found");
      } else if (!user.get().getPassword().equals(inputData.getPassword())) {
        presenter.prepareFailView("Incorrect password");
      } else {
        presenter.prepareSuccessView(new LoginOutputData(user.get().getUsername()));
      }
    } catch (UserStorageException e) {
      presenter.prepareFailView(e.getMessage());
    }
  }
}
