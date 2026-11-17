package use_case.login;

/** The result of a successful login. */
public class LoginOutputData {

  private final String username;

  /**
   * Creates the output data.
   *
   * @param username the logged-in username, as stored (which may differ in case from what was
   *     typed)
   */
  public LoginOutputData(String username) {
    this.username = username;
  }

  public String getUsername() {
    return username;
  }
}
