package interface_adapter.search_stock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import use_case.search_stock.SearchStockMatchesOutputData;
import use_case.search_stock.SearchStockOutputData;

class SearchStockPresenterTest {

  private static final LocalDate DAY = LocalDate.of(2026, 11, 13);

  private final SearchStockViewModel viewModel = new SearchStockViewModel();
  private final SearchStockPresenter presenter = new SearchStockPresenter(viewModel);
  private int notifications;

  SearchStockPresenterTest() {
    viewModel.addListener(() -> notifications++);
  }

  private static SearchStockOutputData output(double price, double change, double percent) {
    return new SearchStockOutputData(
        "IBM", price, change, percent, DAY, List.of(DAY), List.of(price));
  }

  @Test
  void successFormatsQuoteForDisplay() {
    presenter.prepareSuccessView(output(247.13, 2.46, 1.0054));

    SearchStockState state = viewModel.getState();
    assertEquals("IBM", state.getSymbol());
    assertEquals("$247.13", state.getPriceText());
    assertEquals("+2.46 (+1.01%)", state.getChangeText());
    assertEquals("As of Nov 13, 2026", state.getAsOfText());
    assertTrue(state.isUp());
    assertEquals(List.of(DAY), state.getChartDates());
    assertEquals(List.of(247.13), state.getChartCloses());
    assertNull(state.getError());
    assertEquals(1, notifications);
  }

  @Test
  void fallingPriceShowsMinusAndIsNotUp() {
    presenter.prepareSuccessView(output(1240.5, -4.67, -0.3751));

    SearchStockState state = viewModel.getState();
    assertEquals("$1,240.50", state.getPriceText());
    assertEquals("-4.67 (-0.38%)", state.getChangeText());
    assertFalse(state.isUp());
  }

  @Test
  void failureSetsErrorAndNotifies() {
    presenter.prepareFailView("No quote found for that symbol");

    assertEquals("No quote found for that symbol", viewModel.getState().getError());
    assertEquals(1, notifications);
  }

  @Test
  void successAfterFailureClearsTheError() {
    presenter.prepareFailView("oops");
    presenter.prepareSuccessView(output(247.13, 2.46, 1.0054));

    assertNull(viewModel.getState().getError());
  }

  @Test
  void matchesBecomeLabelsAndSymbols() {
    presenter.prepareMatchesView(
        new SearchStockMatchesOutputData(
            "apple",
            List.of("AAPL", "XYZ"),
            List.of("Apple Inc", "Apple Something"),
            List.of("NASDAQ", "")));

    SearchStockState state = viewModel.getState();
    assertEquals(
        List.of("AAPL  Apple Inc  (NASDAQ)", "XYZ  Apple Something"), state.getMatchLabels());
    assertEquals(List.of("AAPL", "XYZ"), state.getMatchSymbols());
    assertEquals("", state.getPriceText());
    assertNull(state.getError());
    assertEquals(1, notifications);
  }

  @Test
  void showingAStockClearsOldMatches() {
    presenter.prepareMatchesView(
        new SearchStockMatchesOutputData(
            "apple", List.of("AAPL"), List.of("Apple Inc"), List.of("NASDAQ")));
    presenter.prepareSuccessView(output(247.13, 2.46, 1.0054));

    assertTrue(viewModel.getState().getMatchLabels().isEmpty());
  }
}
