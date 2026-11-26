package use_case.semantic_search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class SemanticSearchInteractorTest {

  /** Records what the interactor sends to the presenter. */
  private static class RecordingPresenter implements SemanticSearchOutputBoundary {
    SemanticSearchOutputData success;
    String error;

    @Override
    public void prepareSuccessView(SemanticSearchOutputData outputData) {
      success = outputData;
    }

    @Override
    public void prepareFailView(String errorMessage) {
      error = errorMessage;
    }
  }

  private final StockRankerTest.FakeProfiles profiles = new StockRankerTest.FakeProfiles();
  private final FakeEmbedder embedder = new FakeEmbedder();
  private final RecordingPresenter presenter = new RecordingPresenter();
  private final SemanticSearchInteractor interactor =
      new SemanticSearchInteractor(
          new StockRanker(
              profiles,
              new SemanticProfileMatcher(embedder, new InMemoryVectorStore()),
              new KeywordProfileMatcher()),
          presenter);

  private void search(String query) {
    interactor.execute(new SemanticSearchInputData(query));
  }

  @Test
  void sentenceShowsMatchesBiggestFirst() {
    search("best selling oil stocks");

    assertNull(presenter.error);
    SemanticSearchOutputData out = presenter.success;
    assertEquals("best selling oil stocks", out.getQuery());
    assertEquals("oil", out.getTopic());
    assertEquals(SortOrder.LARGEST, out.getSortOrder());
    assertEquals(List.of("XOM", "COP", "TINY", "MYST"), out.getSymbols());
    assertEquals("Exxon Mobil Corporation", out.getNames().get(0));
    assertEquals("Oil, Gas & Fuels", out.getIndustries().get(0));
    assertEquals("Mega Cap", out.getSizes().get(0));
    assertEquals("", out.getSizes().get(3));
    assertEquals(4, out.getScores().size());
    assertNull(out.getFallbackReason());
  }

  @Test
  void fallbackReasonIsPassedOn() {
    embedder.unavailable = true;

    search("banks");

    assertEquals(List.of("BAC"), presenter.success.getSymbols());
    assertEquals("model can't run here", presenter.success.getFallbackReason());
  }

  @Test
  void blankSearchFails() {
    search("   ");

    assertEquals("Please describe the stocks you are looking for.", presenter.error);
  }

  @Test
  void nullSearchFails() {
    search(null);

    assertEquals("Please describe the stocks you are looking for.", presenter.error);
  }

  @Test
  void nothingMatchingFails() {
    embedder.unavailable = true;

    search("spaceships");

    assertEquals("No stocks match \"spaceships\".", presenter.error);
  }

  @Test
  void missingProfilesReportTheError() {
    profiles.unavailable = true;

    search("oil");

    assertEquals("profiles missing", presenter.error);
  }
}
