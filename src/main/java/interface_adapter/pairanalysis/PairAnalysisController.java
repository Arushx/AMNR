package interface_adapter.pairanalysis;

import use_case.pairanalysis.PairAnalysisInputBoundary;
import use_case.pairanalysis.PairAnalysisInputData;

/**
 * Turns the two tickers the user typed into a call to the pair analysis use case.
 *
 * <p>Takes plain strings so it has no dependency on Swing; validation happens in the use case.
 */
public class PairAnalysisController {

  private final PairAnalysisInputBoundary pairAnalysisUseCase;

  /**
   * Creates the controller.
   *
   * @param pairAnalysisUseCase the use case to run
   */
  public PairAnalysisController(PairAnalysisInputBoundary pairAnalysisUseCase) {
    this.pairAnalysisUseCase = pairAnalysisUseCase;
  }

  /**
   * Runs the pair analysis use case.
   *
   * @param firstTicker the first ticker typed
   * @param secondTicker the second ticker typed
   */
  public void execute(String firstTicker, String secondTicker) {
    pairAnalysisUseCase.execute(new PairAnalysisInputData(firstTicker, secondTicker));
  }
}
