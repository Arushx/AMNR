package use_case.login;

/** Receives the result of a login attempt and prepares what the user sees. */
public interface LoginOutputBoundary {

  /**
   * Called when the username and password matched.
   *
   * @param outputData the logged-in user's details
   */
  void prepareSuccessView(LoginOutputData outputData);

  /**
   * Called when the login failed.
   *
   * @param errorMessage why, in words safe to show the user
   */
  void prepareFailView(String errorMessage);
}
