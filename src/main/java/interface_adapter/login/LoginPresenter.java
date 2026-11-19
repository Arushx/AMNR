package interface_adapter.login;

import interface_adapter.ViewManagerModel;
import interface_adapter.logged_in.LoggedInState;
import interface_adapter.logged_in.LoggedInViewModel;
import use_case.login.LoginOutputBoundary;
import use_case.login.LoginOutputData;

/**
 * Updates the view models after a login attempt: on success it records the logged-in user, clears
 * the login form and switches to the logged-in view; on failure it shows the error on the login
 * view.
 */
public class LoginPresenter implements LoginOutputBoundary {

  private final ViewManagerModel viewManagerModel;
  private final LoginViewModel loginViewModel;
  private final LoggedInViewModel loggedInViewModel;

  /**
   * Creates the presenter.
   *
   * @param viewManagerModel switches the active view
   * @param loginViewModel the login view's state
   * @param loggedInViewModel the logged-in view's state
   */
  public LoginPresenter(
      ViewManagerModel viewManagerModel,
      LoginViewModel loginViewModel,
      LoggedInViewModel loggedInViewModel) {
    this.viewManagerModel = viewManagerModel;
    this.loginViewModel = loginViewModel;
    this.loggedInViewModel = loggedInViewModel;
  }

  @Override
  public void prepareSuccessView(LoginOutputData outputData) {
    loggedInViewModel.setState(new LoggedInState(outputData.getUsername()));
    loggedInViewModel.firePropertyChange();

    loginViewModel.setState(new LoginState());
    loginViewModel.firePropertyChange();

    viewManagerModel.setState(loggedInViewModel.getViewName());
    viewManagerModel.firePropertyChange();
  }

  @Override
  public void prepareFailView(String errorMessage) {
    loginViewModel.setState(loginViewModel.getState().withError(errorMessage));
    loginViewModel.firePropertyChange();
  }
}
