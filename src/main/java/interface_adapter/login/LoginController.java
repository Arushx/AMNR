package interface_adapter.login;

import use_case.login.LoginInputBoundary;
import use_case.login.LoginInputData;

/**
 * Turns what the user typed on the login form into a call to the login use case.
 *
 * <p>Takes plain strings so it has no dependency on Swing; validation happens in the use case.
 */
public class LoginController {

  private final LoginInputBoundary loginUseCase;

  /**
   * Creates the controller.
   *
   * @param loginUseCase the use case to run
   */
  public LoginController(LoginInputBoundary loginUseCase) {
    this.loginUseCase = loginUseCase;
  }

  /**
   * Runs the login use case.
   *
   * @param username the username typed
   * @param password the password typed
   */
  public void execute(String username, String password) {
    loginUseCase.execute(new LoginInputData(username, password));
  }
}
