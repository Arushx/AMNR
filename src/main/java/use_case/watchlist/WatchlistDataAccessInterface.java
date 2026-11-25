package use_case.watchlist;

import entity.Watchlist;
import use_case.WatchlistStorageException;

/** Lets the watchlist use case read and change saved watchlists, without saying where they are. */
public interface WatchlistDataAccessInterface {

  /**
   * Finds a user's watchlist, ignoring username case.
   *
   * @param username the owner's username
   * @return the watchlist; empty if the user has saved nothing
   * @throws WatchlistStorageException if the watchlist can't be read
   */
  Watchlist findByUsername(String username) throws WatchlistStorageException;

  /**
   * Saves a symbol at the end of a user's watchlist.
   *
   * @param username the owner's username; the account must exist
   * @param symbol a well-formed symbol not already saved; normalized before saving
   * @throws WatchlistStorageException if the symbol is already saved, the user doesn't exist, or
   *     the watchlist can't be written
   */
  void addSymbol(String username, String symbol) throws WatchlistStorageException;

  /**
   * Removes a symbol from a user's watchlist. Does nothing if it isn't saved.
   *
   * @param username the owner's username
   * @param symbol a well-formed symbol; normalized before matching
   * @throws WatchlistStorageException if the watchlist can't be written
   */
  void removeSymbol(String username, String symbol) throws WatchlistStorageException;
}
