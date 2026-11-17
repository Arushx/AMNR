package use_case.signup;

import entity.User;
import use_case.UserStorageException;

/** Lets the signup use case check and save user accounts, without saying where they are stored. */
public interface SignupDataAccessInterface {

  /**
   * Returns whether an account with this username exists, ignoring case.
   *
   * @param username the username to look for
   * @return true if the username is taken
   * @throws UserStorageException if the accounts can't be read
   */
  boolean existsByUsername(String username) throws UserStorageException;

  /**
   * Saves a new account.
   *
   * @param user the account to save
   * @throws UserStorageException if the username is taken or the account can't be saved
   */
  void save(User user) throws UserStorageException;
}
