package interface_adapter.signup;

import use_case.signup.SignupInputBoundary;
import use_case.signup.SignupInputData;

/**
 * Turns what the user typed on the signup form into a call to the signup use case.
 *
 * <p>Takes plain strings so it has no dependency on Swing; validation happens in the use case.
 */
public class SignupController {

  private final SignupInputBoundary signupUseCase;

  /**
   * Creates the controller.
   *
   * @param signupUseCase the use case to run
   */
  public SignupController(SignupInputBoundary signupUseCase) {
    this.signupUseCase = signupUseCase;
  }

  /**
   * Runs the signup use case.
   *
   * @param username the requested username
   * @param password the requested password
   * @param repeatPassword the password typed a second time
   */
  public void execute(String username, String password, String repeatPassword) {
    signupUseCase.execute(new SignupInputData(username, password, repeatPassword));
  }
}
