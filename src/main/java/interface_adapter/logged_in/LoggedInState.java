package interface_adapter.logged_in;

/** What the logged-in view shows. Immutable. */
public class LoggedInState {

  private final String username;

  /** Creates the state for when nobody is logged in. */
  public LoggedInState() {
    this("");
  }

  /**
   * Creates the state for a logged-in user.
   *
   * @param username the logged-in username
   */
  public LoggedInState(String username) {
    this.username = username;
  }

  public String getUsername() {
    return username;
  }
}
