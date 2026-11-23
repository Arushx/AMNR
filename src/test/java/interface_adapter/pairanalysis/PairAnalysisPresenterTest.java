package interface_adapter.pairanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.pairanalysis.PairAnalysisOutputData;
import use_case.pairanalysis.PairAnalysisUnknownSymbolOutputData;

class PairAnalysisPresenterTest {

  private PairAnalysisViewModel viewModel;
  private PairAnalysisPresenter presenter;
  private List<PropertyChangeEvent> events;

  @BeforeEach
  void setUp() {
    viewModel = new PairAnalysisViewModel();
    presenter = new PairAnalysisPresenter(viewModel);
    events = new ArrayList<>();
    viewModel.addPropertyChangeListener(events::add);
  }

  private static PairAnalysisOutputData koPep(double correlation) {
    return new PairAnalysisOutputData("KO", "Coca-Cola Co", "PEP", "PepsiCo Inc", correlation, 99);
  }

  @Test
  void successFormatsResultAndAlertsView() {
    presenter.prepareSuccessView(koPep(0.873149));

    assertEquals("KO (Coca-Cola Co) & PEP (PepsiCo Inc)", viewModel.getPairText());
    assertEquals("0.8731", viewModel.getCorrelationText());
    assertEquals(
        "Based on 99 daily returns on trading days both stocks share",
        viewModel.getDetailText());
    assertNull(viewModel.getError());
    assertNull(viewModel.getFirstSymbolError());
    assertNull(viewModel.getSecondSymbolError());

    assertEquals(1, events.size());
    assertEquals(PairAnalysisViewModel.VIEW_NAME, events.get(0).getPropertyName());
    assertSame(viewModel, events.get(0).getNewValue());
  }

  @Test
  void negativeCorrelationKeepsSign() {
    presenter.prepareSuccessView(koPep(-0.5));

    assertEquals("-0.5000", viewModel.getCorrelationText());
  }

  @Test
  void missingNameShowsTickerOnly() {
    presenter.prepareSuccessView(new PairAnalysisOutputData("KO", "", "PEP", "PepsiCo Inc", 0.5, 9));

    assertEquals("KO & PEP (PepsiCo Inc)", viewModel.getPairText());
  }

  @Test
  void unknownSymbolMarksOnlyThatField() {
    presenter.prepareSuccessView(koPep(0.9));

    presenter.prepareUnknownSymbolView(
        new PairAnalysisUnknownSymbolOutputData("KO", "ZZZZ", false, true));

    assertNull(viewModel.getFirstSymbolError());
    assertEquals("No stock with symbol \"ZZZZ\"", viewModel.getSecondSymbolError());
    assertNull(viewModel.getError());
    assertEquals("", viewModel.getCorrelationText());
    assertEquals("", viewModel.getPairText());
    assertEquals(2, events.size());
  }

  @Test
  void bothUnknownMarksBothFields() {
    presenter.prepareUnknownSymbolView(
        new PairAnalysisUnknownSymbolOutputData("AAAA", "BBBB", true, true));

    assertEquals("No stock with symbol \"AAAA\"", viewModel.getFirstSymbolError());
    assertEquals("No stock with symbol \"BBBB\"", viewModel.getSecondSymbolError());
  }

  @Test
  void failureShowsErrorAndClearsResultAndFieldErrors() {
    presenter.prepareUnknownSymbolView(
        new PairAnalysisUnknownSymbolOutputData("ZZZZ", "KO", true, false));

    presenter.prepareFailView("Could not get price history for IBM");

    assertEquals("Could not get price history for IBM", viewModel.getError());
    assertNull(viewModel.getFirstSymbolError());
    assertEquals("", viewModel.getCorrelationText());
    assertEquals("", viewModel.getDetailText());
  }

  @Test
  void successClearsEarlierErrors() {
    presenter.prepareUnknownSymbolView(
        new PairAnalysisUnknownSymbolOutputData("ZZZZ", "KO", true, false));
    presenter.prepareFailView("Enter two ticker symbols.");

    presenter.prepareSuccessView(koPep(0.9));

    assertNull(viewModel.getError());
    assertNull(viewModel.getFirstSymbolError());
  }

  @Test
  void sameErrorTwiceStillAlertsView() {
    presenter.prepareFailView("Enter two ticker symbols.");
    presenter.prepareFailView("Enter two ticker symbols.");

    assertEquals(2, events.size());
  }
}
