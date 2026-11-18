package use_case.explain_movement;

import entity.Direction;
import entity.NewsArticle;
import entity.PriceMovement;
import entity.Sentiment;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Decides which news articles are about the company, and which of those best explain a price move.
 *
 * <p>Relevant: the article is tagged with the ticker, or its headline names the company or the
 * ticker. Each relevant article then gets a score:
 *
 * <ul>
 *   <li>+{@value #HEADLINE_POINTS} if the headline names the company or ticker (it is about the
 *       company, not a list of "10 stocks to watch").
 *   <li>+{@value #SENTIMENT_POINTS} if its sentiment matches the move (negative for a drop,
 *       positive for a rise).
 *   <li>+{@value #NEAR_BIG_DAY_POINTS} if it was published on one of the biggest-move days or up
 *       to {@value #DAYS_BEFORE_BIG_DAY} days before it (news from Friday evening moves the price
 *       on Monday).
 * </ul>
 *
 * <p>The highest scores are kept, at most {@value #MAX_SELECTED}; ties go to the earlier article,
 * since news comes before the price reacts.
 */
public class EvidenceSelector {

  static final int HEADLINE_POINTS = 3;
  static final int SENTIMENT_POINTS = 2;
  static final int NEAR_BIG_DAY_POINTS = 2;
  static final int DAYS_BEFORE_BIG_DAY = 3;
  static final int MAX_SELECTED = 15;

  private static final Set<String> COMPANY_SUFFIXES =
      Set.of(
          "INC", "INCORPORATED", "CORP", "CORPORATION", "CO", "COMPANY", "LTD", "LIMITED",
          "PLC", "HOLDINGS", "GROUP", "SA", "AG", "NV", "LLC", "THE");

  /**
   * Keeps only the articles about the company.
   *
   * @param articles all fetched articles
   * @param symbol the ticker symbol, e.g. "NVDA"
   * @param companyName the company's name, e.g. "NVIDIA Corporation"; may be blank
   * @return the relevant articles, in the same order
   */
  public List<NewsArticle> filterRelevant(
      List<NewsArticle> articles, String symbol, String companyName) {
    final List<NewsArticle> relevant = new ArrayList<>();
    for (NewsArticle article : articles) {
      if (article.isTaggedWith(symbol) || headlineNames(article, symbol, companyName)) {
        relevant.add(article);
      }
    }
    return relevant;
  }

  /**
   * Picks the articles that best explain the move.
   *
   * @param relevant the relevant articles with their labels
   * @param movement the price move to explain
   * @param companyName the company's name; may be blank
   * @return the chosen articles plus sentiment counts over all relevant articles
   */
  public NewsEvidence select(
      List<LabeledArticle> relevant, PriceMovement movement, String companyName) {
    final List<LabeledArticle> ranked = new ArrayList<>(relevant);
    ranked.sort(
        Comparator.comparingInt(
                (LabeledArticle labeled) -> score(labeled, movement, companyName))
            .reversed()
            .thenComparing(labeled -> labeled.getArticle().getPublishedAt()));
    return new NewsEvidence(ranked.subList(0, Math.min(MAX_SELECTED, ranked.size())), relevant);
  }

  /**
   * Scores one article; higher means a better explanation of the move.
   *
   * @param labeled the article and its label
   * @param movement the price move
   * @param companyName the company's name; may be blank
   * @return the score
   */
  int score(LabeledArticle labeled, PriceMovement movement, String companyName) {
    final NewsArticle article = labeled.getArticle();
    int score = 0;
    if (headlineNames(article, movement.getSymbol(), companyName)) {
      score += HEADLINE_POINTS;
    }
    if (matchesDirection(labeled.getSentiment(), movement.getDirection())) {
      score += SENTIMENT_POINTS;
    }
    if (isNearBigDay(article.getPublishedDate(), movement.getBiggestMoveDays())) {
      score += NEAR_BIG_DAY_POINTS;
    }
    return score;
  }

  /**
   * Tells whether a sentiment fits a direction: negative news for a drop, positive for a rise.
   *
   * @param sentiment the article's label
   * @param direction the price move's direction
   * @return true if they fit; always false for a FLAT move
   */
  public static boolean matchesDirection(Sentiment sentiment, Direction direction) {
    return direction == Direction.DOWN && sentiment == Sentiment.NEGATIVE
        || direction == Direction.UP && sentiment == Sentiment.POSITIVE;
  }

  private static boolean isNearBigDay(LocalDate published, List<LocalDate> bigDays) {
    for (LocalDate bigDay : bigDays) {
      final long daysBefore = ChronoUnit.DAYS.between(published, bigDay);
      if (daysBefore >= 0 && daysBefore <= DAYS_BEFORE_BIG_DAY) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tells whether a headline names the company.
   *
   * <ul>
   *   <li>The ticker must appear in capitals as a whole word ("NVDA"), because some tickers are
   *       ordinary words ("ON", "NOW").
   *   <li>The company name is matched ignoring case and suffixes ("NVIDIA Corporation" matches
   *       "Nvidia"). A two-word name also matches on its first word ("Meta Platforms" matches
   *       "Meta"); longer names must match whole ("Bank of America" doesn't match "bank").
   * </ul>
   */
  static boolean headlineNames(NewsArticle article, String symbol, String companyName) {
    final String rawHeadline = " " + article.getHeadline().replaceAll("[^A-Za-z0-9]+", " ") + " ";
    if (symbol != null
        && !symbol.isBlank()
        && rawHeadline.contains(" " + symbol.trim().toUpperCase(Locale.ROOT) + " ")) {
      return true;
    }
    final String headline = " " + normalize(article.getHeadline()) + " ";
    final String name = shortName(companyName);
    if (name.isEmpty()) {
      return false;
    }
    if (headline.contains(" " + name + " ")) {
      return true;
    }
    final String[] words = name.split(" ");
    return words.length == 2 && headline.contains(" " + words[0] + " ");
  }

  /**
   * Returns the company name without suffixes like "Inc." or "Corporation", in normalized form,
   * e.g. "NVIDIA Corporation" gives "NVIDIA".
   */
  static String shortName(String companyName) {
    if (companyName == null) {
      return "";
    }
    final List<String> words = new ArrayList<>(List.of(normalize(companyName).split(" ")));
    words.removeIf(word -> word.isEmpty() || COMPANY_SUFFIXES.contains(word));
    return String.join(" ", words);
  }

  /** Upper case, with everything except letters and digits turned into single spaces. */
  private static String normalize(String text) {
    return text.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", " ").trim();
  }
}
