package interface_adapter.logged_in;

import interface_adapter.ViewModel;

/** The view model for the view shown after a successful login. */
public class LoggedInViewModel extends ViewModel<LoggedInState> {

  public static final String VIEW_NAME = "logged in";

  /** Creates the view model with nobody logged in. */
  public LoggedInViewModel() {
    super(VIEW_NAME, new LoggedInState());
  }
}
