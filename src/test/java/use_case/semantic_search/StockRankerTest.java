package use_case.semantic_search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
