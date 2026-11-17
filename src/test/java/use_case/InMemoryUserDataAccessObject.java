package use_case;

import entity.User;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import use_case.login.LoginDataAccessInterface;
import use_case.signup.SignupDataAccessInterface;

/**
 * Keeps users in a map, ignoring username case like the SQLite DAO. Can be switched to fail every
 * call to test how interactors handle storage errors.
 */
public class InMemoryUserDataAccessObject
    implements SignupDataAccessInterface, LoginDataAccessInterface {

  private final Map<String, User> users = new HashMap<>();
  private boolean failing;

  public void setFailing(boolean failing) {
    this.failing = failing;
  }

  @Override
  public boolean existsByUsername(String username) throws UserStorageException {
    checkFailing();
    return users.containsKey(key(username));
  }

  @Override
  public void save(User user) throws UserStorageException {
    checkFailing();
    if (users.containsKey(key(user.getUsername()))) {
      throw new UserStorageException("Username already exists");
    }
    users.put(key(user.getUsername()), user);
  }

  @Override
  public Optional<User> findByUsername(String username) throws UserStorageException {
    checkFailing();
    return Optional.ofNullable(users.get(key(username)));
  }

  private void checkFailing() throws UserStorageException {
    if (failing) {
      throw new UserStorageException("Database unavailable");
    }
  }

  private static String key(String username) {
    return username.toLowerCase(Locale.ROOT);
  }
}
