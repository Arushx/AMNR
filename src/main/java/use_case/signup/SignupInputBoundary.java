package use_case.signup;

/** Entry point the signup controller calls to run the signup use case. */
public interface SignupInputBoundary {

  /**
   * Tries to create an account. The result is reported through {@link SignupOutputBoundary}.
   *
   * @param inputData what the user typed
   */
  void execute(SignupInputData inputData);
}
