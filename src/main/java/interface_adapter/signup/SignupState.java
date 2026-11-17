package interface_adapter.signup;

/** What the signup form shows. Immutable; build a new one to change it. */
public class SignupState {

  private final String username;
  private final String password;
  private final String repeatPassword;
  private final String error;

  /** Creates an empty form with no error. */
  public SignupState() {
    this("", "", "", null);
  }

  /**
   * Creates a form state.
   *
   * @param username the username field
   * @param password the password field
   * @param repeatPassword the repeat password field
   * @param error the message to show, or null for none
   */
  public SignupState(String username, String password, String repeatPassword, String error) {
    this.username = username;
    this.password = password;
    this.repeatPassword = repeatPassword;
    this.error = error;
  }

  /**
   * Returns a copy with a different error.
   *
   * @param newError the message to show, or null for none
   * @return the new state
   */
  public SignupState withError(String newError) {
    return new SignupState(username, password, repeatPassword, newError);
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

  public String getError() {
    return error;
  }
}
