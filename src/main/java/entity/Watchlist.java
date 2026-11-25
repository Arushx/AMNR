package entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * The ticker symbols one user has saved, in the order they were added.
 *
 * <p>Instances are immutable; {@link #withSymbol} and {@link #withoutSymbol} return new copies.
 * Symbols are stored trimmed and uppercase, and each appears at most once.
 */
public class Watchlist {

  /** One to five letters, optionally followed by a dot and one class letter, e.g. "BRK.B". */
  private static final Pattern SYMBOL_FORMAT = Pattern.compile("[A-Z]{1,5}(\\.[A-Z])?");

  private final String username;
  private final List<String> symbols;

  /**
   * Creates a watchlist.
   *
   * @param username the owner's username; must not be blank
   * @param symbols the saved symbols, in the order they were added; normalized on the way in
   * @throws IllegalArgumentException if the username is blank, the list is null, or a symbol is
   *     invalid or repeated
   */
  public Watchlist(String username, List<String> symbols) {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("username must not be blank");
    }
    if (symbols == null) {
      throw new IllegalArgumentException("symbols must not be null");
    }
    final List<String> normalized = new ArrayList<>();
    for (String symbol : symbols) {
      final String clean = normalizeSymbol(symbol);
      if (normalized.contains(clean)) {
        throw new IllegalArgumentException("duplicate symbol: " + clean);
      }
      normalized.add(clean);
    }
    this.username = username;
    this.symbols = List.copyOf(normalized);
  }

  /**
   * Checks whether text is a well-formed ticker symbol, ignoring case and surrounding spaces. Does
   * not check that the stock exists.
   *
   * @param raw the text to check; may be null
   * @return true if it is well-formed
   */
  public static boolean isValidSymbol(String raw) {
    return raw != null && SYMBOL_FORMAT.matcher(raw.trim().toUpperCase(Locale.ROOT)).matches();
  }

  /**
   * Returns the symbol in its stored form: trimmed and uppercase.
   *
   * @param raw the symbol as typed
   * @return the normalized symbol
   * @throws IllegalArgumentException if the symbol is not well-formed
   */
  public static String normalizeSymbol(String raw) {
    if (!isValidSymbol(raw)) {
      throw new IllegalArgumentException("invalid symbol: " + raw);
    }
    return raw.trim().toUpperCase(Locale.ROOT);
  }

  public String getUsername() {
    return username;
  }

  /**
   * Returns the saved symbols.
   *
   * @return the symbols in the order they were added; unmodifiable
   */
  public List<String> getSymbols() {
    return symbols;
  }

  /**
   * Checks whether a symbol is saved, ignoring case and surrounding spaces.
   *
   * @param symbol the symbol to look for; an invalid one is never contained
   * @return true if it is in the watchlist
   */
  public boolean contains(String symbol) {
    return isValidSymbol(symbol) && symbols.contains(normalizeSymbol(symbol));
  }

  /**
   * Returns a copy with a symbol added at the end.
   *
   * @param symbol the symbol to add
   * @return the new watchlist
   * @throws IllegalArgumentException if the symbol is invalid or already saved
   */
  public Watchlist withSymbol(String symbol) {
    final List<String> updated = new ArrayList<>(symbols);
    updated.add(symbol);
    return new Watchlist(username, updated);
  }

  /**
   * Returns a copy without a symbol.
   *
   * @param symbol the symbol to remove
   * @return the new watchlist
   * @throws IllegalArgumentException if the symbol is not saved
   */
  public Watchlist withoutSymbol(String symbol) {
    if (!contains(symbol)) {
      throw new IllegalArgumentException("symbol not in watchlist: " + symbol);
    }
    final List<String> updated = new ArrayList<>(symbols);
    updated.remove(normalizeSymbol(symbol));
    return new Watchlist(username, updated);
  }
}
