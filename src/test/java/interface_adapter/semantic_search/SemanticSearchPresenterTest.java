package interface_adapter.semantic_search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;
import use_case.semantic_search.SemanticSearchOutputData;
import use_case.semantic_search.SortOrder;

class SemanticSearchPresenterTest {

  private final SemanticSearchViewModel viewModel = new SemanticSearchViewModel();
  private final SemanticSearchPresenter presenter = new SemanticSearchPresenter(viewModel);
  private int notifications;

  SemanticSearchPresenterTest() {
    viewModel.addListener(() -> notifications++);
  }

  private static SemanticSearchOutputData output(SortOrder order, String fallbackReason) {
    return new SemanticSearchOutputData(
        "best selling oil stocks",
        "oil",
        order,
        List.of("XOM", "MYST"),
        List.of("Exxon Mobil Corporation", "Mystery Oil Co"),
        List.of("Oil, Gas & Fuels", ""),
        List.of("Mega Cap", ""),
        List.of(0.61, 0.40),
        fallbackReason);
  }

  @Test
  void successFormatsOneLabelPerStock() {
    presenter.prepareSuccessView(output(SortOrder.LARGEST, null));

    SemanticSearchState state = viewModel.getState();
    assertEquals(
        List.of("XOM  Exxon Mobil Corporation  ·  Oil, Gas & Fuels  ·  Mega Cap",
            "MYST  Mystery Oil Co"),
        state.getResultLabels());
    assertEquals(List.of("XOM", "MYST"), state.getResultSymbols());
    assertEquals("Stocks about \"oil\", biggest companies first", state.getSummary());
    assertNull(state.getError());
    assertEquals(1, notifications);
  }

  @Test
  void summaryMentionsSmallestFirstAndFallback() {
    presenter.prepareSuccessView(output(SortOrder.SMALLEST, "model can't run here"));

    assertEquals(
        "Stocks about \"oil\", smallest companies first (keyword matching: model can't run here)",
        viewModel.getState().getSummary());
  }

  @Test
  void relevanceOrderHasNoSortNote() {
    presenter.prepareSuccessView(output(SortOrder.RELEVANCE, null));

    assertEquals("Stocks about \"oil\"", viewModel.getState().getSummary());
  }

  @Test
  void failureKeepsTheLastResultsAndAddsTheError() {
    presenter.prepareSuccessView(output(SortOrder.LARGEST, null));

    presenter.prepareFailView("No stocks match \"spaceships\".");

    assertEquals("No stocks match \"spaceships\".", viewModel.getState().getError());
    assertEquals(2, viewModel.getState().getResultSymbols().size());
    assertEquals(2, notifications);
  }
}
