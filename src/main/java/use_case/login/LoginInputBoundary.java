package use_case.login;

/** Entry point the login controller calls to run the login use case. */
public interface LoginInputBoundary {

  /**
   * Tries to log in. The result is reported through {@link LoginOutputBoundary}.
   *
   * @param inputData what the user typed
   */
  void execute(LoginInputData inputData);
}
