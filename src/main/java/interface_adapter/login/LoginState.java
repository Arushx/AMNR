package interface_adapter.login;

/** What the login form shows. Immutable; build a new one to change it. */
public class LoginState {

  private final String username;
  private final String password;
  private final String error;

  /** Creates an empty form with no error. */
  public LoginState() {
    this("", "", null);
  }

  /**
   * Creates a form state.
   *
   * @param username the username field
   * @param password the password field
   * @param error the message to show, or null for none
   */
  public LoginState(String username, String password, String error) {
    this.username = username;
    this.password = password;
    this.error = error;
  }

  /**
   * Returns a copy with a different error.
   *
   * @param newError the message to show, or null for none
   * @return the new state
   */
  public LoginState withError(String newError) {
    return new LoginState(username, password, newError);
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getError() {
    return error;
  }
}
