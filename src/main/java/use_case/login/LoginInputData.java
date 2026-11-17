package use_case.login;

/** What the user typed into the login form. Values are raw and not yet validated. */
public class LoginInputData {

  private final String username;
  private final String password;

  /**
   * Creates the input data.
   *
   * @param username the username typed
   * @param password the password typed
   */
  public LoginInputData(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }
}
