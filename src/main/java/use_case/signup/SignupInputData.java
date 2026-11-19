package use_case.signup;

/** What the user typed into the signup form. Values are raw and not yet validated. */
public class SignupInputData {

  private final String username;
  private final String password;
  private final String repeatPassword;

  /**
   * Creates the input data.
   *
   * @param username the requested username
   * @param password the requested password
   * @param repeatPassword the password typed a second time
   */
  public SignupInputData(String username, String password, String repeatPassword) {
    this.username = username;
    this.password = password;
    this.repeatPassword = repeatPassword;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getRepeatPassword() {
    return repeatPassword;
  }
}
