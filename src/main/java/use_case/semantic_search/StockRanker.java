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
 *   <li>Order the matches by relevance, or (for "biggest ..." or "small cap ...") take every
 *       match that is nearly as good as the best one and order those by company size.
 *   <li>Return the first few.
 * </ol>
 */
public class StockRanker implements RelatedStockFinder {

  /**
   * For a size order, matches scoring within this much of the best match are re-ordered by size.
   * A broad topic ("tech") has many near-equal matches, so its pool is big and includes the giant
   * companies; a narrow one ("oil") has a smaller pool, so size can't pull in unrelated stocks.
   */
  static final double SIZE_SORT_SCORE_GAP = 0.12;

  /** The size-sorted pool always has at least this many matches (if there are that many). */
  static final int MIN_SIZE_SORT_POOL = 30;

  /** ... and at most this many. */
  static final int MAX_SIZE_SORT_POOL = 300;

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
    List<RankedStock> ordered = matches;
    if (parsed.getSortOrder() != SortOrder.RELEVANCE) {
      ordered = new ArrayList<>(matches.subList(0, sizeSortPool(matches)));
      // List.sort is stable: stocks of the same size keep their relevance order.
      ordered.sort(bySize(parsed.getSortOrder()));
    }
    return new Ranking(
        parsed, new ArrayList<>(ordered.subList(0, Math.min(limit, ordered.size()))),
        fallbackReason);
  }

  /** How many of the best matches (sorted best first) to re-order by size. */
  private static int sizeSortPool(List<RankedStock> matches) {
    if (matches.isEmpty()) {
      return 0;
    }
    final double cutoff = matches.get(0).getScore() - SIZE_SORT_SCORE_GAP;
    int pool = 0;
    while (pool < matches.size() && matches.get(pool).getScore() >= cutoff) {
      pool++;
    }
    pool = Math.max(MIN_SIZE_SORT_POOL, Math.min(MAX_SIZE_SORT_POOL, pool));
    return Math.min(pool, matches.size());
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
