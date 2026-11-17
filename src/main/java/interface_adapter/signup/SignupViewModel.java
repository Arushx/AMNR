package interface_adapter.signup;

import interface_adapter.ViewModel;

/** The view model for the signup view. */
public class SignupViewModel extends ViewModel<SignupState> {

  public static final String VIEW_NAME = "sign up";

  /** Creates the view model with an empty form. */
  public SignupViewModel() {
    super(VIEW_NAME, new SignupState());
  }
}
