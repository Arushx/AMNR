package use_case.semantic_search;

import entity.StockProfile;
import java.util.List;

/**
 * Scores how well each stock profile matches a search topic (the Strategy pattern).
 *
 * <p>There are two strategies: {@link SemanticProfileMatcher} compares meanings with embeddings,
 * and {@link KeywordProfileMatcher} counts matching words. The search uses the first and falls
 * back to the second if embeddings aren't available on this computer.
 */
public interface ProfileMatcher {

  /**
   * Scores every profile against the topic.
   *
   * @param topic what the user is looking for, e.g. "oil"
   * @param profiles the profiles to score
   * @return one score per profile, in the same order; higher is a better match, and a score of 0
   *     or less means "not a match"
   * @throws EmbeddingUnavailableException if this strategy can't run
   */
  double[] score(String topic, List<StockProfile> profiles) throws EmbeddingUnavailableException;
}
