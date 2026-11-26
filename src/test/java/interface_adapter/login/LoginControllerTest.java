package interface_adapter.login;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import use_case.login.LoginInputBoundary;
import use_case.login.LoginInputData;

class LoginControllerTest {

  @Test
  void passesTypedValuesToUseCase() {
    LoginInputData[] received = new LoginInputData[1];
    LoginInputBoundary useCase = inputData -> received[0] = inputData;

    new LoginController(useCase).execute("andrew", "pw");

    assertEquals("andrew", received[0].getUsername());
    assertEquals("pw", received[0].getPassword());
  }
}
