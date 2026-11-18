package use_case.explain_movement;

/** The "explain a price move" use case, as the controller sees it. */
public interface ExplainMovementInputBoundary {

  /**
   * Explains how a stock moved over a period and which news likely caused it.
   *
   * @param inputData the stock and the period
   */
  void execute(ExplainMovementInputData inputData);
}
