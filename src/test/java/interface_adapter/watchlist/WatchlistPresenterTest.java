package interface_adapter.watchlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.watchlist.WatchlistOutputData;

class WatchlistPresenterTest {

  private WatchlistViewModel viewModel;
  private WatchlistPresenter presenter;
  private List<PropertyChangeEvent> events;

  @BeforeEach
  void setUp() {
    viewModel = new WatchlistViewModel();
    presenter = new WatchlistPresenter(viewModel);
    events = new ArrayList<>();
    viewModel.addPropertyChangeListener(events::add);
  }

  @Test
  void startsEmpty() {
    WatchlistState state = viewModel.getState();

    assertEquals("", state.getUsername());
    assertEquals(List.of(), state.getSymbols());
    assertNull(state.getMessage());
    assertNull(state.getError());
  }

  @Test
  void successShowsSymbolsAndMessage() {
    presenter.prepareSuccessView(
        new WatchlistOutputData("andrew", List.of("AAPL", "MSFT"), "Added MSFT"));

    WatchlistState state = viewModel.getState();
    assertEquals("andrew", state.getUsername());
    assertEquals(List.of("AAPL", "MSFT"), state.getSymbols());
    assertEquals("Added MSFT", state.getMessage());
    assertNull(state.getError());
    assertEquals(1, events.size());
  }

  @Test
  void successClearsPreviousError() {
    presenter.prepareFailView("Invalid ticker: AAPL1");

    presenter.prepareSuccessView(new WatchlistOutputData("andrew", List.of("AAPL"), "Added AAPL"));

    assertNull(viewModel.getState().getError());
  }

  @Test
  void viewOfAnotherUserReplacesPreviousList() {
    presenter.prepareSuccessView(new WatchlistOutputData("andrew", List.of("AAPL"), null));

    presenter.prepareSuccessView(new WatchlistOutputData("bob", List.of(), null));

    assertEquals("bob", viewModel.getState().getUsername());
    assertEquals(List.of(), viewModel.getState().getSymbols());
  }

  @Test
  void failureKeepsSymbolsAndClearsMessage() {
    presenter.prepareSuccessView(new WatchlistOutputData("andrew", List.of("AAPL"), "Added AAPL"));

    presenter.prepareFailView("AAPL is already in your watchlist");

    WatchlistState state = viewModel.getState();
    assertEquals(List.of("AAPL"), state.getSymbols());
    assertEquals("AAPL is already in your watchlist", state.getError());
    assertNull(state.getMessage());
  }

  @Test
  void sameErrorTwiceFiresTwice() {
    presenter.prepareFailView("Ticker cannot be empty");
    presenter.prepareFailView("Ticker cannot be empty");

    assertEquals(2, events.size());
  }

  @Test
  void stateSymbolsCannotBeModified() {
    presenter.prepareSuccessView(new WatchlistOutputData("andrew", List.of("AAPL"), null));

    assertThrows(
        UnsupportedOperationException.class, () -> viewModel.getState().getSymbols().add("MSFT"));
  }
}
