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

    assertEquals(LoginViewModel.VIEW_NAME, viewManagerModel.getActiveView());
    assertEquals(1, viewManagerEvents.size());
    assertEquals("andrew", loginViewModel.getUsername());
    assertNull(loginViewModel.getError());
  }

  @Test
  void successClearsSignupForm() {
    signupViewModel.setUsername("andrew");
    signupViewModel.setPassword("pw");
    signupViewModel.setRepeatPassword("pw");
    signupViewModel.setError("old error");

    presenter.prepareSuccessView(new SignupOutputData("andrew"));

    assertEquals("", signupViewModel.getUsername());
    assertEquals("", signupViewModel.getPassword());
    assertEquals("", signupViewModel.getRepeatPassword());
    assertNull(signupViewModel.getError());
  }

  @Test
  void failureShowsErrorAndStaysOnSignup() {
    signupViewModel.setUsername("andrew");
    signupViewModel.setPassword("pw");
    signupViewModel.setRepeatPassword("pw2");

    presenter.prepareFailView("Passwords do not match");

    assertEquals("Passwords do not match", signupViewModel.getError());
    assertEquals("andrew", signupViewModel.getUsername());
    assertEquals(1, signupEvents.size());
    assertTrue(viewManagerEvents.isEmpty());
  }
}
