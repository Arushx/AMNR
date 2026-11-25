package interface_adapter.logged_in;

import interface_adapter.ViewModel;

/** What the view shown after a successful login displays. */
public class LoggedInViewModel extends ViewModel {

  public static final String VIEW_NAME = "logged in";

  private String username = "";

  /** Creates the view model with nobody logged in. */
  public LoggedInViewModel() {
    super(VIEW_NAME);
  }

  /**
   * Returns who is logged in.
   *
   * @return the username as stored, or "" when nobody is logged in
   */
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
