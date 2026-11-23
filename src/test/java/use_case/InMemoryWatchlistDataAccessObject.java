package use_case;

import entity.Watchlist;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import use_case.watchlist.WatchlistDataAccessInterface;

/**
 * Keeps watchlists in a map, ignoring username case like the SQLite DAO. Can be switched to fail
 * every call to test how the interactor handles storage errors.
 */
public class InMemoryWatchlistDataAccessObject implements WatchlistDataAccessInterface {

  private final Map<String, List<String>> watchlists = new HashMap<>();
  private boolean failing;

  public void setFailing(boolean failing) {
    this.failing = failing;
  }

  @Override
  public Watchlist findByUsername(String username) throws WatchlistStorageException {
    checkFailing();
    return new Watchlist(username, watchlists.getOrDefault(key(username), List.of()));
  }

  @Override
  public void addSymbol(String username, String symbol) throws WatchlistStorageException {
    checkFailing();
    final String normalized = Watchlist.normalizeSymbol(symbol);
    final List<String> symbols = watchlists.computeIfAbsent(key(username), k -> new ArrayList<>());
    if (symbols.contains(normalized)) {
      throw new WatchlistStorageException(normalized + " is already saved");
    }
    symbols.add(normalized);
  }

  @Override
  public void removeSymbol(String username, String symbol) throws WatchlistStorageException {
    checkFailing();
    final List<String> symbols = watchlists.get(key(username));
    if (symbols != null) {
      symbols.remove(Watchlist.normalizeSymbol(symbol));
    }
  }

  private void checkFailing() throws WatchlistStorageException {
    if (failing) {
      throw new WatchlistStorageException("Database unavailable");
    }
  }

  private static String key(String username) {
    return username.toLowerCase(Locale.ROOT);
  }
}
