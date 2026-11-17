package interface_adapter.login;

import interface_adapter.ViewModel;

/** The view model for the login view. */
public class LoginViewModel extends ViewModel<LoginState> {

  public static final String VIEW_NAME = "log in";

  /** Creates the view model with an empty form. */
  public LoginViewModel() {
    super(VIEW_NAME, new LoginState());
  }
}
