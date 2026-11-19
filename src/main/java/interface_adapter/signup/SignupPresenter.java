package interface_adapter.signup;

import interface_adapter.ViewManagerModel;
import interface_adapter.login.LoginState;
import interface_adapter.login.LoginViewModel;
import use_case.signup.SignupOutputBoundary;
import use_case.signup.SignupOutputData;

/**
 * Updates the view models after a signup attempt: on success it clears the signup form, fills the
 * new username into the login form and switches to the login view; on failure it shows the error
 * on the signup view.
 */
public class SignupPresenter implements SignupOutputBoundary {

  private final ViewManagerModel viewManagerModel;
  private final SignupViewModel signupViewModel;
  private final LoginViewModel loginViewModel;

  /**
   * Creates the presenter.
   *
   * @param viewManagerModel switches the active view
   * @param signupViewModel the signup view's state
   * @param loginViewModel the login view's state
   */
  public SignupPresenter(
      ViewManagerModel viewManagerModel,
      SignupViewModel signupViewModel,
      LoginViewModel loginViewModel) {
    this.viewManagerModel = viewManagerModel;
    this.signupViewModel = signupViewModel;
    this.loginViewModel = loginViewModel;
  }

  @Override
  public void prepareSuccessView(SignupOutputData outputData) {
    signupViewModel.setState(new SignupState());
    signupViewModel.firePropertyChange();

    loginViewModel.setState(new LoginState(outputData.getUsername(), "", null));
    loginViewModel.firePropertyChange();

    viewManagerModel.setState(loginViewModel.getViewName());
    viewManagerModel.firePropertyChange();
  }

  @Override
  public void prepareFailView(String errorMessage) {
    signupViewModel.setState(signupViewModel.getState().withError(errorMessage));
    signupViewModel.firePropertyChange();
  }
}
