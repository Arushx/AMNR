package use_case.pairanalysis;

/** The pair analysis use case, as seen by its controller. Implemented by the interactor. */
public interface PairAnalysisInputBoundary {

  /**
   * Computes the correlation of daily returns for the two requested stocks.
   *
   * @param inputData the two ticker symbols to compare
   */
  void execute(PairAnalysisInputData inputData);
}
