package use_case.pairanalysis;

/** How the pair analysis interactor reports its result. Implemented by the presenter. */
public interface PairAnalysisOutputBoundary {

  /**
   * Shows the computed correlation.
   *
   * @param outputData the tickers compared and their returns correlation
   */
  void prepareSuccessView(PairAnalysisOutputData outputData);

  /**
   * Shows why the correlation could not be computed.
   *
   * @param errorMessage a message that is safe to show to the user
   */
  void prepareFailView(String errorMessage);
}
