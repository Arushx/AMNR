package use_case.semantic_search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.MarketCapClass;
import entity.StockListing;
import entity.StockProfile;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import use_case.StockDataUnavailableException;
import use_case.StockProfileDataAccessInterface;

class StockRankerTest {

  /** Provides the shared test profiles, or fails. */
  static class FakeProfiles implements StockProfileDataAccessInterface {
    boolean unavailable;

    @Override
    public List<StockProfile> getProfiles() throws StockDataUnavailableException {
      if (unavailable) {
        throw new StockDataUnavailableException("profiles missing");
      }
      return TestProfiles.ALL;
    }
  }

  private final FakeProfiles profiles = new FakeProfiles();
  private final FakeEmbedder embedder = new FakeEmbedder();
  private final StockRanker ranker =
      new StockRanker(
          profiles,
          new SemanticProfileMatcher(embedder, new InMemoryVectorStore()),
          new KeywordProfileMatcher());

  private static List<String> symbols(Ranking ranking) {
    List<String> symbols = new ArrayList<>();
    for (RankedStock stock : ranking.getStocks()) {
      symbols.add(stock.getProfile().getSymbol());
    }
    return symbols;
  }

  @Test
  void relevanceOrderKeepsOnlyMatches() throws StockDataUnavailableException {
    Ranking ranking = ranker.rank("crude oil", 10);

    // Apple and the bank share no meaning with oil in the fake model, so they are left out.
    assertFalse(symbols(ranking).contains("AAPL"));
    assertFalse(symbols(ranking).contains("BAC"));
    assertFalse(ranking.usedFallback());
  }

  @Test
  void rankingWordsSortBiggestFirstWithUnknownLast() throws StockDataUnavailableException {
    Ranking ranking = ranker.rank("best selling oil stocks", 10);

    assertEquals(SortOrder.LARGEST, ranking.getQuery().getSortOrder());
    assertEquals(List.of("XOM", "COP", "TINY", "MYST"), symbols(ranking));
  }

  @Test
  void smallCapSortsSmallestFirstWithUnknownLast() throws StockDataUnavailableException {
    Ranking ranking = ranker.rank("small cap oil", 10);

    assertEquals(List.of("TINY", "COP", "XOM", "MYST"), symbols(ranking));
  }

  @Test
  void limitCutsTheList() throws StockDataUnavailableException {
    assertEquals(List.of("XOM", "COP"), symbols(ranker.rank("biggest oil", 2)));
  }

  @Test
  void fallsBackToKeywordsWhenTheModelCannotRun() throws StockDataUnavailableException {
    embedder.unavailable = true;

    Ranking ranking = ranker.rank("banks", 10);

    assertTrue(ranking.usedFallback());
    assertEquals("model can't run here", ranking.getFallbackReason());
    assertEquals(List.of("BAC"), symbols(ranking));
  }

  @Test
  void missingProfilesAreReported() {
    profiles.unavailable = true;

    assertThrows(StockDataUnavailableException.class, () -> ranker.rank("oil", 10));
  }

  @Test
  void findRelatedReturnsListingsForSearchStock() throws StockDataUnavailableException {
    List<StockListing> related = ranker.findRelated("biggest oil", 2);

    assertEquals("XOM", related.get(0).getSymbol());
    assertEquals("Exxon Mobil Corporation", related.get(0).getName());
    assertEquals(2, related.size());
  }

  /** Gives each profile a fixed score, in the order of TestProfiles.ALL. */
  private static class FixedScores implements ProfileMatcher {
    private final double[] scores;

    FixedScores(double... scores) {
      this.scores = scores;
    }

    @Override
    public double[] score(String topic, List<StockProfile> profiles) {
      return scores.clone();
    }
  }

  @Test
  void sameSizeStocksKeepTheirRelevanceOrder() throws StockDataUnavailableException {
    // Order of ALL: XOM, TINY, COP, MYST, AAPL, BAC. The three Mega Caps (XOM, AAPL, BAC) come
    // first, in score order; then Large, Nano, and unknown size last.
    StockRanker fixed =
        new StockRanker(profiles, new FixedScores(0.5, 0.9, 0.4, 0.3, 0.2, 0.1),
            new KeywordProfileMatcher());

    assertEquals(List.of("XOM", "AAPL", "BAC", "COP", "TINY", "MYST"),
        symbols(fixed.rank("biggest anything", 10)));
  }

  @Test
  void giantsThatMatchFarWorseAreNotPulledInBySize() throws StockDataUnavailableException {
    // 35 small companies match well (within the score gap); 5 giants match much worse.
    List<StockProfile> many = new ArrayList<>();
    double[] scores = new double[40];
    for (int i = 0; i < 40; i++) {
      boolean giant = i >= 35;
      many.add(new StockProfile("S" + i, "Company " + i, "Energy", "Oil, Gas & Fuels",
          giant ? MarketCapClass.MEGA : MarketCapClass.SMALL, ""));
      scores[i] = giant ? 0.5 : 0.9 - i * 0.001;
    }
    StockRanker fixed =
        new StockRanker(() -> many, new FixedScores(scores), new KeywordProfileMatcher());

    List<String> top = symbols(fixed.rank("biggest oil", 5));

    assertEquals(List.of("S0", "S1", "S2", "S3", "S4"), top);
  }

  @Test
  void relevanceOrderFollowsScores() throws StockDataUnavailableException {
    StockRanker fixed =
        new StockRanker(profiles, new FixedScores(0.5, 0.9, 0.4, 0.3, 0.2, 0.1),
            new KeywordProfileMatcher());

    assertEquals(List.of("TINY", "XOM", "COP"), symbols(fixed.rank("anything", 3)));
  }
}
