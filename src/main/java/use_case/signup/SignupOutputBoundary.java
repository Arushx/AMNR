package use_case.signup;

/** Receives the result of a signup attempt and prepares what the user sees. */
public interface SignupOutputBoundary {

  /**
   * Called when the account was created.
   *
   * @param outputData the new account's details
   */
  void prepareSuccessView(SignupOutputData outputData);

  /**
   * Called when the account could not be created.
   *
   * @param errorMessage why, in words safe to show the user
   */
  void prepareFailView(String errorMessage);
}
