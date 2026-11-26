package interface_adapter.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import interface_adapter.ViewManagerModel;
import interface_adapter.logged_in.LoggedInViewModel;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.login.LoginOutputData;

class LoginPresenterTest {

  private ViewManagerModel viewManagerModel;
  private LoginViewModel loginViewModel;
  private LoggedInViewModel loggedInViewModel;
  private LoginPresenter presenter;
  private List<PropertyChangeEvent> viewManagerEvents;
  private List<PropertyChangeEvent> loggedInEvents;
  private List<PropertyChangeEvent> loginEvents;

  @BeforeEach
  void setUp() {
    viewManagerModel = new ViewManagerModel();
    loginViewModel = new LoginViewModel();
    loggedInViewModel = new LoggedInViewModel();
    presenter = new LoginPresenter(viewManagerModel, loginViewModel, loggedInViewModel);
    viewManagerEvents = new ArrayList<>();
    loggedInEvents = new ArrayList<>();
    loginEvents = new ArrayList<>();
    viewManagerModel.addPropertyChangeListener(viewManagerEvents::add);
    loggedInViewModel.addPropertyChangeListener(loggedInEvents::add);
    loginViewModel.addPropertyChangeListener(loginEvents::add);
  }

  @Test
  void successSwitchesToLoggedInWithUsername() {
    presenter.prepareSuccessView(new LoginOutputData("Andrew"));

    assertEquals(LoggedInViewModel.VIEW_NAME, viewManagerModel.getState());
    assertEquals(1, viewManagerEvents.size());
    assertEquals("Andrew", loggedInViewModel.getState().getUsername());
    assertEquals(1, loggedInEvents.size());
  }

  @Test
  void successClearsLoginForm() {
    loginViewModel.setState(new LoginState("Andrew", "pw", "old error"));

    presenter.prepareSuccessView(new LoginOutputData("Andrew"));

    assertEquals("", loginViewModel.getState().getUsername());
    assertEquals("", loginViewModel.getState().getPassword());
    assertNull(loginViewModel.getState().getError());
  }

  @Test
  void failureShowsErrorAndStaysOnLogin() {
    loginViewModel.setState(new LoginState("Andrew", "wrong", null));

    presenter.prepareFailView("Incorrect password");

    assertEquals("Incorrect password", loginViewModel.getState().getError());
    assertEquals("Andrew", loginViewModel.getState().getUsername());
    assertEquals(1, loginEvents.size());
    assertTrue(viewManagerEvents.isEmpty());
    assertTrue(loggedInEvents.isEmpty());
  }
}
