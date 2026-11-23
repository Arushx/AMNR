package use_case.explain_movement;

/** What the use case tells the presenter when it is done. */
public interface ExplainMovementOutputBoundary {

  /**
   * Called when the price move was measured. The news part may still have failed; see {@link
   * ExplainMovementOutputData#getNewsError()}.
   *
   * @param outputData the result
   */
  void prepareSuccessView(ExplainMovementOutputData outputData);

  /**
   * Called when nothing could be explained, e.g. bad input or no price data.
   *
   * @param errorMessage a message that is safe to show to the user
   */
  void prepareFailView(String errorMessage);
}
