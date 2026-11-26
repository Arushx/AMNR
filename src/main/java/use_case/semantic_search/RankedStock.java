package use_case.semantic_search;

import entity.StockProfile;

/** A stock profile together with how well it matched a search. */
public class RankedStock {

  private final StockProfile profile;
  private final double score;

  /**
   * Creates the ranked stock.
   *
   * @param profile the stock's profile
   * @param score how well it matched; higher is better
   */
  public RankedStock(StockProfile profile, double score) {
    this.profile = profile;
    this.score = score;
  }

  public StockProfile getProfile() {
    return profile;
  }

  public double getScore() {
    return score;
  }
}
