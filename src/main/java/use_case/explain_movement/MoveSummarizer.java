package use_case.explain_movement;

import entity.PriceMovement;

/**
 * Writes a short "why did the stock move" summary from a price move and the news chosen for it.
 *
 * <p>The default is {@link ExtractiveMoveSummarizer}, which builds the sentence from headlines. A
 * language-model summarizer can be added later as another implementation, without changing the
 * interactor.
 */
public interface MoveSummarizer {

  /**
   * Summarizes a price move.
   *
   * @param movement the price move
   * @param companyName the company's name, for the text; may be blank
   * @param evidence the chosen news and the sentiment counts
   * @return the summary and the articles it cites
   * @throws SummaryUnavailableException if no summary can be produced
   */
  MovementSummary summarize(PriceMovement movement, String companyName, NewsEvidence evidence)
      throws SummaryUnavailableException;
}
