package use_case.signup;

/** The result of a successful signup. */
public class SignupOutputData {

  private final String username;

  /**
   * Creates the output data.
   *
   * @param username the username of the new account
   */
  public SignupOutputData(String username) {
    this.username = username;
  }

  public String getUsername() {
    return username;
  }
}
