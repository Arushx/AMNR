package interface_adapter.signup;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import use_case.signup.SignupInputBoundary;
import use_case.signup.SignupInputData;

class SignupControllerTest {

  @Test
  void passesTypedValuesToUseCase() {
    SignupInputData[] received = new SignupInputData[1];
    SignupInputBoundary useCase = inputData -> received[0] = inputData;

    new SignupController(useCase).execute("andrew", "pw", "pw2");

    assertEquals("andrew", received[0].getUsername());
    assertEquals("pw", received[0].getPassword());
    assertEquals("pw2", received[0].getRepeatPassword());
  }
}
