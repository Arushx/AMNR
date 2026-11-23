package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import entity.User;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.UserStorageException;
import use_case.WatchlistStorageException;

/** Runs against a real SQLite file in a temporary folder, deleted after each test. */
class SqliteWatchlistDataAccessObjectTest {

  @TempDir Path tempDir;

  private String dbPath;
  private SqliteWatchlistDataAccessObject dao;

  @BeforeEach
  void setUp() throws UserStorageException, WatchlistStorageException {
    dbPath = tempDir.resolve("amnr.db").toString();
    SqliteUserDataAccessObject users = new SqliteUserDataAccessObject(dbPath);
    users.save(new User("andrew", "secret"));
    users.save(new User("bob", "secret"));
    dao = new SqliteWatchlistDataAccessObject(dbPath);
  }

  @Test
  void newUserHasEmptyWatchlist() throws WatchlistStorageException {
    assertEquals(List.of(), dao.findByUsername("andrew").getSymbols());
  }

  @Test
  void returnsSymbolsInOrderAdded() throws WatchlistStorageException {
    dao.addSymbol("andrew", "MSFT");
    dao.addSymbol("andrew", "AAPL");
    dao.addSymbol("andrew", "GOOGL");

    assertEquals(List.of("MSFT", "AAPL", "GOOGL"), dao.findByUsername("andrew").getSymbols());
  }

  @Test
  void keepsOrderAfterRemovingAndAddingAgain() throws WatchlistStorageException {
    dao.addSymbol("andrew", "MSFT");
    dao.addSymbol("andrew", "AAPL");
    dao.removeSymbol("andrew", "AAPL");
    dao.addSymbol("andrew", "GOOGL");

    assertEquals(List.of("MSFT", "GOOGL"), dao.findByUsername("andrew").getSymbols());
  }

  @Test
  void storesSymbolsNormalized() throws WatchlistStorageException {
    dao.addSymbol("andrew", " aapl ");

    assertEquals(List.of("AAPL"), dao.findByUsername("andrew").getSymbols());
  }

  @Test
  void keepsWatchlistAfterReopening() throws WatchlistStorageException {
    dao.addSymbol("andrew", "AAPL");

    SqliteWatchlistDataAccessObject reopened = new SqliteWatchlistDataAccessObject(dbPath);

    assertEquals(List.of("AAPL"), reopened.findByUsername("andrew").getSymbols());
  }

  @Test
  void keepsUsersWatchlistsSeparate() throws WatchlistStorageException {
    dao.addSymbol("andrew", "AAPL");
    dao.addSymbol("bob", "MSFT");

    assertEquals(List.of("AAPL"), dao.findByUsername("andrew").getSymbols());
    assertEquals(List.of("MSFT"), dao.findByUsername("bob").getSymbols());
  }

  @Test
  void usernameIgnoresCase() throws WatchlistStorageException {
    dao.addSymbol("ANDREW", "AAPL");

    assertEquals(List.of("AAPL"), dao.findByUsername("andrew").getSymbols());
  }

  @Test
  void rejectsDuplicateSymbol() throws WatchlistStorageException {
    dao.addSymbol("andrew", "AAPL");

    assertThrows(WatchlistStorageException.class, () -> dao.addSymbol("Andrew", "aapl"));
  }

  @Test
  void rejectsUnknownUser() {
    assertThrows(WatchlistStorageException.class, () -> dao.addSymbol("ghost", "AAPL"));
  }

  @Test
  void rejectsInvalidSymbol() {
    assertThrows(IllegalArgumentException.class, () -> dao.addSymbol("andrew", "NOT VALID"));
  }

  @Test
  void removesSymbol() throws WatchlistStorageException {
    dao.addSymbol("andrew", "AAPL");
    dao.addSymbol("andrew", "MSFT");

    dao.removeSymbol("andrew", "aapl");

    assertEquals(List.of("MSFT"), dao.findByUsername("andrew").getSymbols());
  }

  @Test
  void removingMissingSymbolDoesNothing() throws WatchlistStorageException {
    dao.addSymbol("andrew", "AAPL");

    dao.removeSymbol("andrew", "MSFT");

    assertEquals(List.of("AAPL"), dao.findByUsername("andrew").getSymbols());
  }

  @Test
  void failsWhenDatabaseCannotBeOpened() {
    String badPath = tempDir.resolve("missing-folder").resolve("amnr.db").toString();

    assertThrows(
        WatchlistStorageException.class, () -> new SqliteWatchlistDataAccessObject(badPath));
  }
}
