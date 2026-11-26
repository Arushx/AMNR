package use_case.semantic_search;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Splits a search sentence into its topic and a sort order, using simple word rules.
 *
 * <p>Embeddings understand topics ("oil" is close to "crude"), but not ranking words: "best
 * selling" would match a company literally named "Best". So ranking phrases are taken out and
 * turned into a {@link SortOrder}, and filler words ("stocks", "companies") are dropped, leaving
 * only the topic to embed.
 *
 * <p>"Expensive" and "cheap" are read as company size (market cap), because share prices would
 * cost one API request per stock; a company's total value is a fair stand-in.
 */
public class SearchQueryParser {

  /** Longer phrases come first so "best selling" is removed before "best". */
  private static final List<String> LARGEST_PHRASES =
      List.of(
          "best selling", "best-selling", "most valuable", "most expensive", "most popular",
          "blue chip", "blue-chip", "biggest", "largest", "leading", "major", "giant", "priciest",
          "expensive", "popular", "top", "best");

  private static final List<String> SMALLEST_PHRASES =
      List.of(
          "small cap", "small-cap", "micro cap", "micro-cap", "smallest", "cheapest", "penny",
          "cheap", "tiny");

  private static final Set<String> FILLER_WORDS =
      Set.of(
          "stock", "stocks", "share", "shares", "company", "companies", "firm", "firms",
          "business", "businesses", "corporation", "corporations", "equities", "the", "a", "an",
          "of", "in", "that", "which", "who", "me", "show", "find", "list", "some", "any", "good",
          "for", "to", "and", "most", "make", "makes", "making", "maker", "makers");

  /**
   * Parses a search sentence.
   *
   * @param query what the user typed, e.g. "best selling oil stocks"
   * @return the topic and sort order; if every word was a ranking or filler word, the topic is the
   *     whole query, so there is always something to search for
   */
  public ParsedQuery parse(String query) {
    String text = " " + query.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9&\\- ]", " ") + " ";
    SortOrder order = SortOrder.RELEVANCE;
    for (String phrase : SMALLEST_PHRASES) {
      if (text.contains(" " + phrase + " ")) {
        order = SortOrder.SMALLEST;
        text = text.replace(" " + phrase + " ", " ");
      }
    }
    for (String phrase : LARGEST_PHRASES) {
      if (text.contains(" " + phrase + " ")) {
        if (order == SortOrder.RELEVANCE) {
          order = SortOrder.LARGEST;
        }
        text = text.replace(" " + phrase + " ", " ");
      }
    }
    final StringBuilder topic = new StringBuilder();
    for (String word : text.trim().split("\\s+")) {
      if (!word.isEmpty() && !FILLER_WORDS.contains(word)) {
        topic.append(topic.length() == 0 ? "" : " ").append(word);
      }
    }
    return new ParsedQuery(topic.length() == 0 ? query.trim() : topic.toString(), order);
  }
}
