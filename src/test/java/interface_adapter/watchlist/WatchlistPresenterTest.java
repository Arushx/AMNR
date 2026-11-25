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

    assertEquals("", viewModel.getUsername());
    assertEquals(List.of(), viewModel.getSymbols());
    assertNull(viewModel.getMessage());
    assertNull(viewModel.getError());
  }

  @Test
  void successShowsSymbolsAndMessage() {
    presenter.prepareSuccessView(
        new WatchlistOutputData("andrew", List.of("AAPL", "MSFT"), "Added MSFT"));

    assertEquals("andrew", viewModel.getUsername());
    assertEquals(List.of("AAPL", "MSFT"), viewModel.getSymbols());
    assertEquals("Added MSFT", viewModel.getMessage());
    assertNull(viewModel.getError());
    assertEquals(1, events.size());
  }

  @Test
  void successClearsPreviousError() {
    presenter.prepareFailView("Invalid ticker: AAPL1");

    presenter.prepareSuccessView(new WatchlistOutputData("andrew", List.of("AAPL"), "Added AAPL"));

    assertNull(viewModel.getError());
  }

  @Test
  void viewOfAnotherUserReplacesPreviousList() {
    presenter.prepareSuccessView(new WatchlistOutputData("andrew", List.of("AAPL"), null));

    presenter.prepareSuccessView(new WatchlistOutputData("bob", List.of(), null));

    assertEquals("bob", viewModel.getUsername());
    assertEquals(List.of(), viewModel.getSymbols());
  }

  @Test
  void failureKeepsSymbolsAndClearsMessage() {
    presenter.prepareSuccessView(new WatchlistOutputData("andrew", List.of("AAPL"), "Added AAPL"));

    presenter.prepareFailView("AAPL is already in your watchlist");

    assertEquals(List.of("AAPL"), viewModel.getSymbols());
    assertEquals("AAPL is already in your watchlist", viewModel.getError());
    assertNull(viewModel.getMessage());
  }

  @Test
  void sameErrorTwiceFiresTwice() {
    presenter.prepareFailView("Ticker cannot be empty");
    presenter.prepareFailView("Ticker cannot be empty");

    assertEquals(2, events.size());
  }

  @Test
  void symbolsCannotBeModified() {
    presenter.prepareSuccessView(new WatchlistOutputData("andrew", List.of("AAPL"), null));

    assertThrows(
        UnsupportedOperationException.class, () -> viewModel.getSymbols().add("MSFT"));
  }
}
