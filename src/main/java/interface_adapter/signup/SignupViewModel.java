package interface_adapter.signup;

import interface_adapter.ViewModel;

/** What the signup form shows: the three typed fields and the last error. */
public class SignupViewModel extends ViewModel {

  public static final String VIEW_NAME = "sign up";

  private String username = "";
  private String password = "";
  private String repeatPassword = "";
  private String error;

  /** Creates the view model with an empty form and no error. */
  public SignupViewModel() {
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

  public String getRepeatPassword() {
    return repeatPassword;
  }

  public void setRepeatPassword(String repeatPassword) {
    this.repeatPassword = repeatPassword;
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
