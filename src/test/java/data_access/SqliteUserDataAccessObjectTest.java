package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.User;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.UserStorageException;

/** Runs against a real SQLite file in a temporary folder, deleted after each test. */
class SqliteUserDataAccessObjectTest {

  @TempDir Path tempDir;

  private String dbPath;
  private SqliteUserDataAccessObject dao;

  @BeforeEach
  void setUp() throws UserStorageException {
    dbPath = tempDir.resolve("users.db").toString();
    dao = new SqliteUserDataAccessObject(dbPath);
  }

  @Test
  void createsDatabaseFile() {
    assertTrue(Files.exists(Path.of(dbPath)));
  }

  @Test
  void findsSavedUser() throws UserStorageException {
    dao.save(new User("andrew", "secret"));

    Optional<User> found = dao.findByUsername("andrew");

    assertTrue(found.isPresent());
    assertEquals("andrew", found.get().getUsername());
    assertEquals("secret", found.get().getPassword());
  }

  @Test
  void returnsEmptyForUnknownUser() throws UserStorageException {
    assertTrue(dao.findByUsername("nobody").isEmpty());
  }

  @Test
  void existsByUsernameReflectsSavedUsers() throws UserStorageException {
    assertFalse(dao.existsByUsername("andrew"));

    dao.save(new User("andrew", "secret"));

    assertTrue(dao.existsByUsername("andrew"));
  }

  @Test
  void usernamesIgnoreCase() throws UserStorageException {
    dao.save(new User("Andrew", "secret"));

    assertTrue(dao.existsByUsername("ANDREW"));
    assertEquals("Andrew", dao.findByUsername("andrew").get().getUsername());
  }

  @Test
  void rejectsDuplicateUsername() throws UserStorageException {
    dao.save(new User("andrew", "secret"));

    assertThrows(UserStorageException.class, () -> dao.save(new User("ANDREW", "other")));
  }

  @Test
  void keepsUsersAfterReopening() throws UserStorageException {
    dao.save(new User("andrew", "secret"));

    SqliteUserDataAccessObject reopened = new SqliteUserDataAccessObject(dbPath);

    assertEquals("secret", reopened.findByUsername("andrew").get().getPassword());
  }

  @Test
  void handlesQuotesInUsername() throws UserStorageException {
    dao.save(new User("o'brien", "secret"));

    assertTrue(dao.existsByUsername("o'brien"));
  }

  @Test
  void failsWhenDatabaseCannotBeOpened() {
    String badPath = tempDir.resolve("missing-folder").resolve("users.db").toString();

    assertThrows(UserStorageException.class, () -> new SqliteUserDataAccessObject(badPath));
  }
}
