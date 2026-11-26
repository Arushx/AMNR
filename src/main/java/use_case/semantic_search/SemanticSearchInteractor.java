package use_case.semantic_search;

import entity.StockProfile;
import java.util.ArrayList;
import java.util.List;
import use_case.StockDataUnavailableException;

/**
 * The Semantic Search use case: find stocks from a sentence such as "best selling oil stocks".
 *
 * <p>The ranking itself is done by {@link StockRanker}; this class checks the input, asks for the
 * ranking, and turns it into output data for the presenter. Picking a result shows that stock
 * through the Search Stock use case (the view passes its ticker to Search Stock's controller).
 */
public class SemanticSearchInteractor implements SemanticSearchInputBoundary {

  /** How many matches to show. */
  static final int MAX_RESULTS = 10;

  private final StockRanker ranker;
  private final SemanticSearchOutputBoundary presenter;

  /**
   * Creates the interactor.
   *
   * @param ranker ranks the stocks for a sentence
   * @param presenter receives the result
   */
  public SemanticSearchInteractor(StockRanker ranker, SemanticSearchOutputBoundary presenter) {
    this.ranker = ranker;
    this.presenter = presenter;
  }

  @Override
  public void execute(SemanticSearchInputData inputData) {
    final String query = inputData.getQuery() == null ? "" : inputData.getQuery().trim();
    if (query.isEmpty()) {
      presenter.prepareFailView("Please describe the stocks you are looking for.");
      return;
    }
    final Ranking ranking;
    try {
      ranking = ranker.rank(query, MAX_RESULTS);
    } catch (StockDataUnavailableException e) {
      presenter.prepareFailView(e.getMessage());
      return;
    }
    if (ranking.getStocks().isEmpty()) {
      presenter.prepareFailView("No stocks match \"" + query + "\".");
      return;
    }

    final List<String> symbols = new ArrayList<>();
    final List<String> names = new ArrayList<>();
    final List<String> industries = new ArrayList<>();
    final List<String> sizes = new ArrayList<>();
    final List<Double> scores = new ArrayList<>();
    for (RankedStock stock : ranking.getStocks()) {
      final StockProfile profile = stock.getProfile();
      symbols.add(profile.getSymbol());
      names.add(profile.getName());
      industries.add(profile.getIndustry().isEmpty() ? profile.getSector() : profile.getIndustry());
      sizes.add(profile.getMarketCap().getLabel());
      scores.add(stock.getScore());
    }
    presenter.prepareSuccessView(
        new SemanticSearchOutputData(
            query,
            ranking.getQuery().getTopic(),
            ranking.getQuery().getSortOrder(),
            symbols,
            names,
            industries,
            sizes,
            scores,
            ranking.getFallbackReason()));
  }
}
