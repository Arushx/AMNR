package use_case.explain_movement;

import entity.PriceMovement;

/**
 * Tries one summarizer and, if it fails, uses another. For example, a language-model summarizer
 * with {@link ExtractiveMoveSummarizer} as the backup, so the user always gets a summary.
 *
 * <p>Both parts are {@link MoveSummarizer}s, so this is itself one too (Composite/Decorator), and
 * the interactor doesn't know a fallback exists.
 */
public class FallbackMoveSummarizer implements MoveSummarizer {

  private final MoveSummarizer primary;
  private final MoveSummarizer backup;

  /**
   * Creates the fallback.
   *
   * @param primary the summarizer to try first
   * @param backup the summarizer to use when the first one fails
   */
  public FallbackMoveSummarizer(MoveSummarizer primary, MoveSummarizer backup) {
    this.primary = primary;
    this.backup = backup;
  }

  @Override
  public MovementSummary summarize(
      PriceMovement movement, String companyName, NewsEvidence evidence)
      throws SummaryUnavailableException {
    try {
      return primary.summarize(movement, companyName, evidence);
    } catch (SummaryUnavailableException e) {
      return backup.summarize(movement, companyName, evidence);
    }
  }
}
