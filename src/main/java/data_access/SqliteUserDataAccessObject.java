package data_access;

import entity.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import use_case.UserStorageException;
import use_case.login.LoginDataAccessInterface;
import use_case.signup.SignupDataAccessInterface;

/**
 * Stores user accounts in a local SQLite file.
 *
 * <p>The users table is created on construction if missing. Usernames are unique ignoring case
 * ({@code COLLATE NOCASE}), so "Andrew" and "andrew" are the same account. A new connection is
 * opened per call, so the file is never held open between operations.
 */
public class SqliteUserDataAccessObject
    implements SignupDataAccessInterface, LoginDataAccessInterface {

  private static final String CREATE_TABLE =
      "CREATE TABLE IF NOT EXISTS users ("
          + "username TEXT PRIMARY KEY COLLATE NOCASE, "
          + "password TEXT NOT NULL)";
  private static final String INSERT_USER = "INSERT INTO users (username, password) VALUES (?, ?)";
  private static final String SELECT_USER =
      "SELECT username, password FROM users WHERE username = ?";
  private static final String COUNT_USER = "SELECT COUNT(*) FROM users WHERE username = ?";

  private final String url;

  /**
   * Opens (or creates) the database file and makes sure the users table exists.
   *
   * @param dbPath path to the SQLite file, e.g. "amnr.db"; its folder must already exist
   * @throws UserStorageException if the database can't be opened
   */
  public SqliteUserDataAccessObject(String dbPath) throws UserStorageException {
    this.url = "jdbc:sqlite:" + dbPath;
    try (Connection connection = connect();
        Statement statement = connection.createStatement()) {
      statement.execute(CREATE_TABLE);
    } catch (SQLException e) {
      throw new UserStorageException("Could not open the user database at " + dbPath, e);
    }
  }

  @Override
  public boolean existsByUsername(String username) throws UserStorageException {
    try (Connection connection = connect();
        PreparedStatement statement = connection.prepareStatement(COUNT_USER)) {
      statement.setString(1, username);
      try (ResultSet result = statement.executeQuery()) {
        return result.next() && result.getInt(1) > 0;
      }
    } catch (SQLException e) {
      throw new UserStorageException("Could not check username " + username, e);
    }
  }

  @Override
  public void save(User user) throws UserStorageException {
    try (Connection connection = connect();
        PreparedStatement statement = connection.prepareStatement(INSERT_USER)) {
      statement.setString(1, user.getUsername());
      statement.setString(2, user.getPassword());
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new UserStorageException("Could not save user " + user.getUsername(), e);
    }
  }

  @Override
  public Optional<User> findByUsername(String username) throws UserStorageException {
    try (Connection connection = connect();
        PreparedStatement statement = connection.prepareStatement(SELECT_USER)) {
      statement.setString(1, username);
      try (ResultSet result = statement.executeQuery()) {
        if (!result.next()) {
          return Optional.empty();
        }
        return Optional.of(new User(result.getString("username"), result.getString("password")));
      }
    } catch (SQLException e) {
      throw new UserStorageException("Could not look up user " + username, e);
    }
  }

  private Connection connect() throws SQLException {
    return DriverManager.getConnection(url);
  }
}
