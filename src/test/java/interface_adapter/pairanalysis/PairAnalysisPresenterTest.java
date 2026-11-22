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

  @Test
  void successFormatsResultAndAlertsView() {
    presenter.prepareSuccessView(new PairAnalysisOutputData("KO", "PEP", 0.873149, 99));

    assertEquals("KO", viewModel.getFirstTicker());
    assertEquals("PEP", viewModel.getSecondTicker());
    assertEquals("0.8731", viewModel.getCorrelationText());
    assertEquals(
        "Based on 99 daily returns on trading days both stocks share",
        viewModel.getDetailText());
    assertNull(viewModel.getError());

    assertEquals(1, events.size());
    assertEquals(PairAnalysisViewModel.VIEW_NAME, events.get(0).getPropertyName());
    assertSame(viewModel, events.get(0).getNewValue());
  }

  @Test
  void negativeCorrelationKeepsSign() {
    presenter.prepareSuccessView(new PairAnalysisOutputData("A", "B", -0.5, 10));

    assertEquals("-0.5000", viewModel.getCorrelationText());
  }

  @Test
  void failureShowsErrorAndClearsPreviousResult() {
    presenter.prepareSuccessView(new PairAnalysisOutputData("KO", "PEP", 0.9, 99));

    presenter.prepareFailView("Could not get price history for XYZ");

    assertEquals("Could not get price history for XYZ", viewModel.getError());
    assertEquals("", viewModel.getCorrelationText());
    assertEquals("", viewModel.getDetailText());
    assertEquals(2, events.size());
  }

  @Test
  void successAfterFailureClearsError() {
    presenter.prepareFailView("Enter two ticker symbols.");

    presenter.prepareSuccessView(new PairAnalysisOutputData("KO", "PEP", 0.9, 99));

    assertNull(viewModel.getError());
  }

  @Test
  void sameErrorTwiceStillAlertsView() {
    presenter.prepareFailView("Enter two ticker symbols.");
    presenter.prepareFailView("Enter two ticker symbols.");

    assertEquals(2, events.size());
  }
}
