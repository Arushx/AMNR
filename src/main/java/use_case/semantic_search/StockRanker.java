package use_case.semantic_search;

import entity.MarketCapClass;
import entity.StockListing;
import entity.StockProfile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import use_case.RelatedStockFinder;
import use_case.StockDataUnavailableException;
import use_case.StockProfileDataAccessInterface;

/**
 * Ranks every stock for a search sentence: the core of semantic search, shared by the Semantic
 * Search use case and by Search Stock (through {@link RelatedStockFinder}).
 *
 * <ol>
 *   <li>Split the sentence into a topic and a sort order ({@link SearchQueryParser}).
 *   <li>Score every profile against the topic: by meaning if possible, otherwise by keywords.
 *   <li>Keep the {@value #CANDIDATE_POOL} best matches.
 *   <li>Order them by relevance or by company size, and return the first few.
 * </ol>
 */
public class StockRanker implements RelatedStockFinder {

  /** How many of the closest matches are re-ordered by size ("biggest oil stocks"). */
  static final int CANDIDATE_POOL = 30;

  private final StockProfileDataAccessInterface profileDataAccess;
  private final ProfileMatcher primaryMatcher;
  private final ProfileMatcher fallbackMatcher;
  private final SearchQueryParser parser = new SearchQueryParser();

  /**
   * Creates the ranker.
   *
   * @param profileDataAccess provides every stock profile
   * @param primaryMatcher the preferred way to score profiles (by meaning)
   * @param fallbackMatcher used if the primary one can't run (by keywords)
   */
  public StockRanker(
      StockProfileDataAccessInterface profileDataAccess,
      ProfileMatcher primaryMatcher,
      ProfileMatcher fallbackMatcher) {
    this.profileDataAccess = profileDataAccess;
    this.primaryMatcher = primaryMatcher;
    this.fallbackMatcher = fallbackMatcher;
  }

  /**
   * Ranks the stocks for a sentence.
   *
   * @param query what the user typed, e.g. "best selling oil stocks"
   * @param limit the most stocks to return
   * @return the parsed query and the best matches in display order
   * @throws StockDataUnavailableException if the profiles can't be loaded
   */
  public Ranking rank(String query, int limit) throws StockDataUnavailableException {
    final ParsedQuery parsed = parser.parse(query);
    final List<StockProfile> profiles = profileDataAccess.getProfiles();

    double[] scores;
    String fallbackReason = null;
    try {
      scores = primaryMatcher.score(parsed.getTopic(), profiles);
    } catch (EmbeddingUnavailableException e) {
      fallbackReason = e.getMessage();
      try {
        scores = fallbackMatcher.score(parsed.getTopic(), profiles);
      } catch (EmbeddingUnavailableException again) {
        throw new StockDataUnavailableException("Search is unavailable: " + again.getMessage());
      }
    }

    final List<RankedStock> matches = new ArrayList<>();
    for (int i = 0; i < profiles.size(); i++) {
      if (scores[i] > 0) {
        matches.add(new RankedStock(profiles.get(i), scores[i]));
      }
    }
    matches.sort(Comparator.comparingDouble(RankedStock::getScore).reversed());
    final List<RankedStock> pool =
        new ArrayList<>(matches.subList(0, Math.min(CANDIDATE_POOL, matches.size())));
    if (parsed.getSortOrder() != SortOrder.RELEVANCE) {
      // List.sort is stable: stocks of the same size keep their relevance order.
      pool.sort(bySize(parsed.getSortOrder()));
    }
    return new Ranking(parsed, pool.subList(0, Math.min(limit, pool.size())), fallbackReason);
  }

  @Override
  public List<StockListing> findRelated(String query, int limit)
      throws StockDataUnavailableException {
    final List<StockListing> listings = new ArrayList<>();
    for (RankedStock stock : rank(query, limit).getStocks()) {
      final StockProfile profile = stock.getProfile();
      listings.add(new StockListing(profile.getSymbol(), profile.getName(), ""));
    }
    return listings;
  }

  /** Biggest (or smallest) first; stocks of unknown size always go last. */
  private static Comparator<RankedStock> bySize(SortOrder order) {
    return Comparator.comparingInt(
        stock -> {
          final MarketCapClass size = stock.getProfile().getMarketCap();
          if (size == MarketCapClass.UNKNOWN) {
            return Integer.MAX_VALUE;
          }
          return order == SortOrder.LARGEST ? size.ordinal() : -size.ordinal();
        });
  }
}
