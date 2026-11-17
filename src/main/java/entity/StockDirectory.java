package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Every stock we know about, searchable by exact ticker or by company name.
 *
 * <p>Name search ranks matches from best to worst:
 *
 * <ol>
 *   <li>the name is exactly what was typed ("apple inc");
 *   <li>the name starts with it ("apple" → "Apple Inc");
 *   <li>a word in the name starts with it ("apple" → "Kurv ... Apple ETF");
 *   <li>the name contains it anywhere ("apple" → "Pineapple Financial").
 * </ol>
 *
 * <p>Within the same rank, shorter names come first (the main company is usually the shortest),
 * then alphabetical by ticker. Upper/lower case is ignored.
 */
public class StockDirectory {

  private static final int NO_MATCH = Integer.MAX_VALUE;

  private final List<StockListing> listings;
  private final Map<String, StockListing> bySymbol = new HashMap<>();

  /**
   * Creates a directory.
   *
   * @param listings every known stock
   * @throws IllegalArgumentException if listings is null
   */
  public StockDirectory(List<StockListing> listings) {
    if (listings == null) {
      throw new IllegalArgumentException("listings must not be null");
    }
    this.listings = Collections.unmodifiableList(new ArrayList<>(listings));
    for (StockListing listing : this.listings) {
      bySymbol.put(listing.getSymbol(), listing);
    }
  }

  /**
   * Looks up a stock by its exact ticker symbol, ignoring case.
   *
   * @param symbol the ticker, e.g. "aapl"
   * @return the listing, or null if no stock has that ticker
   */
  public StockListing findBySymbol(String symbol) {
    if (symbol == null) {
      return null;
    }
    return bySymbol.get(symbol.trim().toUpperCase());
  }

  /**
   * Finds the stocks whose company name best matches the text, best first.
   *
   * @param query the text the user typed, e.g. "apple"
   * @param limit the most matches to return, e.g. 5
   * @return up to {@code limit} listings; empty if nothing matches or the query is blank
   */
  public List<StockListing> searchByName(String query, int limit) {
    final List<StockListing> matches = new ArrayList<>();
    if (query == null || query.isBlank()) {
      return matches;
    }
    final String text = query.trim().toLowerCase(Locale.ROOT);
    for (StockListing listing : listings) {
      if (rank(listing, text) != NO_MATCH) {
        matches.add(listing);
      }
    }
    matches.sort(
        Comparator.<StockListing>comparingInt(listing -> rank(listing, text))
            .thenComparingInt(listing -> listing.getName().length())
            .thenComparing(StockListing::getSymbol));
    return new ArrayList<>(matches.subList(0, Math.min(limit, matches.size())));
  }

  /** Returns how well a listing's name matches the text: 0 is best, NO_MATCH means no match. */
  private static int rank(StockListing listing, String text) {
    final String name = listing.getName().toLowerCase(Locale.ROOT);
    if (name.isEmpty() || !name.contains(text)) {
      return NO_MATCH;
    }
    if (name.equals(text)) {
      return 0;
    }
    if (name.startsWith(text)) {
      return 1;
    }
    for (String word : name.split("[^a-z0-9]+")) {
      if (word.startsWith(text)) {
        return 2;
      }
    }
    return 3;
  }
}
