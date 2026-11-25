package data_access;

import entity.Watchlist;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.sqlite.SQLiteConfig;
import use_case.WatchlistStorageException;
import use_case.watchlist.WatchlistDataAccessInterface;

/**
 * Stores watchlists in the same SQLite file as the user accounts, one row per saved symbol.
 *
 * <p>Each row references {@code users(username)}, so the users table must exist before a symbol
 * is added: construct {@link SqliteUserDataAccessObject} on the same file first. Usernames match
 * ignoring case, like the users table. Symbols are stored normalized (see {@link Watchlist}).
 */
public class SqliteWatchlistDataAccessObject implements WatchlistDataAccessInterface {

  private static final String CREATE_TABLE =
      "CREATE TABLE IF NOT EXISTS watchlist ("
          + "username TEXT NOT NULL COLLATE NOCASE REFERENCES users(username), "
          + "symbol TEXT NOT NULL, "
          + "PRIMARY KEY (username, symbol))";
  // A new row's rowid is one more than the largest existing rowid, so ordering by rowid lists
  // the current symbols in the order they were added.
  private static final String SELECT_SYMBOLS =
      "SELECT symbol FROM watchlist WHERE username = ? ORDER BY rowid";
  private static final String INSERT_SYMBOL =
      "INSERT INTO watchlist (username, symbol) VALUES (?, ?)";
  private static final String DELETE_SYMBOL =
      "DELETE FROM watchlist WHERE username = ? AND symbol = ?";

  private final String url;
  private final SQLiteConfig config = new SQLiteConfig();

  /**
   * Opens (or creates) the database file and makes sure the watchlist table exists.
   *
   * @param dbPath path to the SQLite file, e.g. "amnr.db"; its folder must already exist
   * @throws WatchlistStorageException if the database can't be opened
   */
  public SqliteWatchlistDataAccessObject(String dbPath) throws WatchlistStorageException {
    this.url = "jdbc:sqlite:" + dbPath;
    // SQLite ignores REFERENCES unless foreign keys are switched on for each connection.
    config.enforceForeignKeys(true);
    try (Connection connection = connect();
        Statement statement = connection.createStatement()) {
      statement.execute(CREATE_TABLE);
    } catch (SQLException e) {
      throw new WatchlistStorageException("Could not open the watchlist database at " + dbPath, e);
    }
  }

  @Override
  public Watchlist findByUsername(String username) throws WatchlistStorageException {
    try (Connection connection = connect();
        PreparedStatement statement = connection.prepareStatement(SELECT_SYMBOLS)) {
      statement.setString(1, username);
      final List<String> symbols = new ArrayList<>();
      try (ResultSet result = statement.executeQuery()) {
        while (result.next()) {
          symbols.add(result.getString("symbol"));
        }
      }
      return new Watchlist(username, symbols);
    } catch (SQLException e) {
      throw new WatchlistStorageException("Could not load the watchlist for " + username, e);
    }
  }

  @Override
  public void addSymbol(String username, String symbol) throws WatchlistStorageException {
    final String normalized = Watchlist.normalizeSymbol(symbol);
    try (Connection connection = connect();
        PreparedStatement statement = connection.prepareStatement(INSERT_SYMBOL)) {
      statement.setString(1, username);
      statement.setString(2, normalized);
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new WatchlistStorageException(
          "Could not add " + normalized + " to the watchlist for " + username, e);
    }
  }

  @Override
  public void removeSymbol(String username, String symbol) throws WatchlistStorageException {
    final String normalized = Watchlist.normalizeSymbol(symbol);
    try (Connection connection = connect();
        PreparedStatement statement = connection.prepareStatement(DELETE_SYMBOL)) {
      statement.setString(1, username);
      statement.setString(2, normalized);
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new WatchlistStorageException(
          "Could not remove " + normalized + " from the watchlist for " + username, e);
    }
  }

  private Connection connect() throws SQLException {
    return DriverManager.getConnection(url, config.toProperties());
  }
}
