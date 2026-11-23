package interface_adapter.watchlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.watchlist.WatchlistInputBoundary;
import use_case.watchlist.WatchlistInputData;

class WatchlistControllerTest {

  /** Records which use case method the controller called, and with what. */
  private static class RecordingUseCase implements WatchlistInputBoundary {
    String action;
    WatchlistInputData received;

    @Override
    public void add(WatchlistInputData inputData) {
      action = "add";
      received = inputData;
    }

    @Override
    public void remove(WatchlistInputData inputData) {
      action = "remove";
      received = inputData;
    }

    @Override
    public void view(WatchlistInputData inputData) {
      action = "view";
      received = inputData;
    }
  }

  private RecordingUseCase useCase;
  private WatchlistController controller;

  @BeforeEach
  void setUp() {
    useCase = new RecordingUseCase();
    controller = new WatchlistController(useCase);
  }

  @Test
  void addPassesUsernameAndTicker() {
    controller.add("andrew", "aapl");

    assertEquals("add", useCase.action);
    assertEquals("andrew", useCase.received.getUsername());
    assertEquals("aapl", useCase.received.getSymbol());
  }

  @Test
  void removePassesUsernameAndTicker() {
    controller.remove("andrew", "AAPL");

    assertEquals("remove", useCase.action);
    assertEquals("andrew", useCase.received.getUsername());
    assertEquals("AAPL", useCase.received.getSymbol());
  }

  @Test
  void viewPassesUsername() {
    controller.view("andrew");

    assertEquals("view", useCase.action);
    assertEquals("andrew", useCase.received.getUsername());
    assertNull(useCase.received.getSymbol());
  }
}
