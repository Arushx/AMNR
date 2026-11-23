package use_case.watchlist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.InMemoryWatchlistDataAccessObject;
import use_case.WatchlistStorageException;

class WatchlistInteractorTest {

  /** Records which presenter method the interactor called. */
  private static class RecordingPresenter implements WatchlistOutputBoundary {
    WatchlistOutputData success;
    String error;

    @Override
    public void prepareSuccessView(WatchlistOutputData outputData) {
      success = outputData;
    }

    @Override
    public void prepareFailView(String errorMessage) {
      error = errorMessage;
    }
  }

  private InMemoryWatchlistDataAccessObject dao;
  private RecordingPresenter presenter;
  private WatchlistInteractor interactor;

  @BeforeEach
  void setUp() {
    dao = new InMemoryWatchlistDataAccessObject();
    presenter = new RecordingPresenter();
    interactor = new WatchlistInteractor(dao, presenter);
  }

  private void assertFailedWith(String message) {
    assertEquals(message, presenter.error);
    assertNull(presenter.success);
  }

  private void assertSucceededWith(List<String> symbols, String message) {
    assertNull(presenter.error);
    assertEquals(symbols, presenter.success.getSymbols());
    assertEquals(message, presenter.success.getMessage());
  }

  // add

  @Test
  void addSavesNormalizedSymbol() throws WatchlistStorageException {
    interactor.add(new WatchlistInputData("andrew", " aapl "));

    assertSucceededWith(List.of("AAPL"), "Added AAPL");
    assertEquals("andrew", presenter.success.getUsername());
    assertEquals(List.of("AAPL"), dao.findByUsername("andrew").getSymbols());
  }

  @Test
  void addAppendsToExistingSymbols() throws WatchlistStorageException {
    dao.addSymbol("andrew", "MSFT");

    interactor.add(new WatchlistInputData("andrew", "AAPL"));

    assertSucceededWith(List.of("MSFT", "AAPL"), "Added AAPL");
  }

  @Test
  void addRejectsDuplicate() throws WatchlistStorageException {
    dao.addSymbol("andrew", "AAPL");

    interactor.add(new WatchlistInputData("andrew", "aapl"));

    assertFailedWith("AAPL is already in your watchlist");
  }

  @Test
  void addRejectsBlankSymbol() {
    interactor.add(new WatchlistInputData("andrew", "  "));

    assertFailedWith("Ticker cannot be empty");
  }

  @Test
  void addRejectsNullSymbol() {
    interactor.add(new WatchlistInputData("andrew", null));

    assertFailedWith("Ticker cannot be empty");
  }

  @Test
  void addRejectsInvalidSymbol() {
    interactor.add(new WatchlistInputData("andrew", "AAPL1"));

    assertFailedWith("Invalid ticker: AAPL1");
  }

  @Test
  void addRejectsMissingUser() {
    interactor.add(new WatchlistInputData(" ", "AAPL"));

    assertFailedWith("You must be logged in to use the watchlist");
  }

  @Test
  void addReportsStorageError() {
    dao.setFailing(true);

    interactor.add(new WatchlistInputData("andrew", "AAPL"));

    assertFailedWith("Database unavailable");
  }

  // remove

  @Test
  void removeDeletesSymbol() throws WatchlistStorageException {
    dao.addSymbol("andrew", "AAPL");
    dao.addSymbol("andrew", "MSFT");

    interactor.remove(new WatchlistInputData("andrew", "aapl"));

    assertSucceededWith(List.of("MSFT"), "Removed AAPL");
    assertEquals(List.of("MSFT"), dao.findByUsername("andrew").getSymbols());
  }

  @Test
  void removeRejectsSymbolNotSaved() {
    interactor.remove(new WatchlistInputData("andrew", "AAPL"));

    assertFailedWith("AAPL is not in your watchlist");
  }

  @Test
  void removeRejectsInvalidSymbol() {
    interactor.remove(new WatchlistInputData("andrew", "not valid"));

    assertFailedWith("Invalid ticker: not valid");
  }

  @Test
  void removeRejectsMissingUser() {
    interactor.remove(new WatchlistInputData(null, "AAPL"));

    assertFailedWith("You must be logged in to use the watchlist");
  }

  @Test
  void removeReportsStorageError() {
    dao.setFailing(true);

    interactor.remove(new WatchlistInputData("andrew", "AAPL"));

    assertFailedWith("Database unavailable");
  }

  // view

  @Test
  void viewReturnsEmptyListForNewUser() {
    interactor.view(new WatchlistInputData("andrew"));

    assertSucceededWith(List.of(), null);
    assertEquals("andrew", presenter.success.getUsername());
  }

  @Test
  void viewReturnsSymbolsInOrderAdded() throws WatchlistStorageException {
    dao.addSymbol("andrew", "MSFT");
    dao.addSymbol("andrew", "AAPL");

    interactor.view(new WatchlistInputData("andrew"));

    assertSucceededWith(List.of("MSFT", "AAPL"), null);
  }

  @Test
  void viewRejectsMissingUser() {
    interactor.view(new WatchlistInputData(""));

    assertFailedWith("You must be logged in to use the watchlist");
  }

  @Test
  void viewReportsStorageError() {
    dao.setFailing(true);

    interactor.view(new WatchlistInputData("andrew"));

    assertFailedWith("Database unavailable");
  }
}
