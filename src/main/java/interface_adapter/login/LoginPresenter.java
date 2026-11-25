package interface_adapter.login;

import interface_adapter.ViewManagerModel;
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
   * @param loginViewModel what the login view shows
   * @param loggedInViewModel what the logged-in view shows
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
    loggedInViewModel.setUsername(outputData.getUsername());
    loggedInViewModel.firePropertyChange();

    loginViewModel.setUsername("");
    loginViewModel.setPassword("");
    loginViewModel.setError(null);
    loginViewModel.firePropertyChange();

    viewManagerModel.setActiveView(loggedInViewModel.getViewName());
    viewManagerModel.firePropertyChange();
  }

  @Override
  public void prepareFailView(String errorMessage) {
    loginViewModel.setError(errorMessage);
    loginViewModel.firePropertyChange();
  }
}
