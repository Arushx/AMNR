package interface_adapter.login;

import interface_adapter.ViewModel;

/** What the login form shows: the typed username and password, and the last error. */
public class LoginViewModel extends ViewModel {

  public static final String VIEW_NAME = "log in";

  private String username = "";
  private String password = "";
  private String error;

  /** Creates the view model with an empty form and no error. */
  public LoginViewModel() {
    super(VIEW_NAME);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Returns the message to show.
   *
   * @return the error, or null for none
   */
  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
