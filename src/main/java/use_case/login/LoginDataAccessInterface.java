package use_case.login;

import entity.User;
import java.util.Optional;
import use_case.UserStorageException;

/** Lets the login use case look up user accounts, without saying where they are stored. */
public interface LoginDataAccessInterface {

  /**
   * Finds the account with this username, ignoring case.
   *
   * @param username the username to look for
   * @return the account, or empty if there is none
   * @throws UserStorageException if the accounts can't be read
   */
  Optional<User> findByUsername(String username) throws UserStorageException;
}
