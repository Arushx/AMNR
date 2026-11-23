package interface_adapter.pairanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import use_case.pairanalysis.PairAnalysisInputBoundary;
import use_case.pairanalysis.PairAnalysisInputData;

class PairAnalysisControllerTest {

  @Test
  void passesTickersToUseCaseUnchanged() {
    final RecordingUseCase useCase = new RecordingUseCase();
    final PairAnalysisController controller = new PairAnalysisController(useCase);

    controller.execute(" ko ", "PEP");

    assertNotNull(useCase.received);
    assertEquals(" ko ", useCase.received.getFirstTicker());
    assertEquals("PEP", useCase.received.getSecondTicker());
  }

  /** Remembers the input it was given. */
  private static class RecordingUseCase implements PairAnalysisInputBoundary {
    private PairAnalysisInputData received;

    @Override
    public void execute(PairAnalysisInputData inputData) {
      received = inputData;
    }
  }
}
