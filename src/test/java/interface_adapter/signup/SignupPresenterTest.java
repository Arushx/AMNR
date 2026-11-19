package interface_adapter.signup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import interface_adapter.ViewManagerModel;
import interface_adapter.login.LoginViewModel;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.signup.SignupOutputData;

class SignupPresenterTest {

  private ViewManagerModel viewManagerModel;
  private SignupViewModel signupViewModel;
  private LoginViewModel loginViewModel;
  private SignupPresenter presenter;
  private List<PropertyChangeEvent> viewManagerEvents;
  private List<PropertyChangeEvent> signupEvents;

  @BeforeEach
  void setUp() {
    viewManagerModel = new ViewManagerModel();
    signupViewModel = new SignupViewModel();
    loginViewModel = new LoginViewModel();
    presenter = new SignupPresenter(viewManagerModel, signupViewModel, loginViewModel);
    viewManagerEvents = new ArrayList<>();
    signupEvents = new ArrayList<>();
    viewManagerModel.addPropertyChangeListener(viewManagerEvents::add);
    signupViewModel.addPropertyChangeListener(signupEvents::add);
  }

  @Test
  void successSwitchesToLoginWithUsernameFilledIn() {
    presenter.prepareSuccessView(new SignupOutputData("andrew"));

    assertEquals(LoginViewModel.VIEW_NAME, viewManagerModel.getState());
    assertEquals(1, viewManagerEvents.size());
    assertEquals("andrew", loginViewModel.getState().getUsername());
    assertNull(loginViewModel.getState().getError());
  }

  @Test
  void successClearsSignupForm() {
    signupViewModel.setState(new SignupState("andrew", "pw", "pw", "old error"));

    presenter.prepareSuccessView(new SignupOutputData("andrew"));

    SignupState state = signupViewModel.getState();
    assertEquals("", state.getUsername());
    assertEquals("", state.getPassword());
    assertNull(state.getError());
  }

  @Test
  void failureShowsErrorAndStaysOnSignup() {
    signupViewModel.setState(new SignupState("andrew", "pw", "pw2", null));

    presenter.prepareFailView("Passwords do not match");

    assertEquals("Passwords do not match", signupViewModel.getState().getError());
    assertEquals("andrew", signupViewModel.getState().getUsername());
    assertEquals(1, signupEvents.size());
    assertTrue(viewManagerEvents.isEmpty());
  }
}
