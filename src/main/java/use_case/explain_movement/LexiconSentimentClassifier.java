package use_case.explain_movement;

import entity.NewsArticle;
import entity.Sentiment;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Labels articles by counting positive and negative words from a {@link SentimentLexicon} in the
 * headline and summary.
 *
 * <p>Rules:
 *
 * <ul>
 *   <li>score = positive words &minus; negative words; above 0 is POSITIVE, below 0 is NEGATIVE,
 *       0 is NEUTRAL.
 *   <li>Headline words count twice, because the headline says what the article is about.
 *   <li>A positive word right after a negation ("not", "no", "never", ... within 3 words) is not
 *       counted, as in Loughran and McDonald's method ("not strong" is not good news).
 *   <li>Hyphenated words are checked whole ("sell-off") and part by part.
 * </ul>
 *
 * <p>It is deterministic and needs no network, so it is used in tests and as the default.
 */
public class LexiconSentimentClassifier implements SentimentClassifier {

  static final int HEADLINE_WEIGHT = 2;
  static final int NEGATION_WINDOW = 3;

  private static final Pattern WORD = Pattern.compile("[A-Za-z]+(?:-[A-Za-z]+)*");
  private static final Set<String> NEGATIONS =
      Set.of(
          "NO", "NOT", "NONE", "NEITHER", "NEVER", "NOBODY", "ISN", "AREN", "WASN", "WEREN",
          "DOESN", "DIDN", "HASN", "HAVEN");

  private final SentimentLexicon lexicon;

  /**
   * Creates a classifier.
   *
   * @param lexicon the word lists to use
   */
  public LexiconSentimentClassifier(SentimentLexicon lexicon) {
    this.lexicon = lexicon;
  }

  @Override
  public List<Sentiment> classify(List<NewsArticle> articles) {
    final List<Sentiment> labels = new ArrayList<>();
    for (NewsArticle article : articles) {
      labels.add(classify(article));
    }
    return labels;
  }

  /**
   * Labels one article.
   *
   * @param article the article
   * @return its label
   */
  public Sentiment classify(NewsArticle article) {
    final int score =
        HEADLINE_WEIGHT * score(article.getHeadline()) + score(article.getSummary());
    if (score > 0) {
      return Sentiment.POSITIVE;
    }
    if (score < 0) {
      return Sentiment.NEGATIVE;
    }
    return Sentiment.NEUTRAL;
  }

  /**
   * Scores a piece of text: positive words minus negative words.
   *
   * @param text any text
   * @return the score; 0 for empty text
   */
  int score(String text) {
    final List<String> tokens = tokenize(text);
    int score = 0;
    for (int i = 0; i < tokens.size(); i++) {
      final int value = valueOf(tokens.get(i));
      if (value > 0 && isNegated(tokens, i)) {
        continue;
      }
      score += value;
    }
    return score;
  }

  /** Returns +1, -1 or 0 for one token, checking hyphenated words whole first, then by part. */
  private int valueOf(String token) {
    final int whole = valueOfWord(token);
    if (whole != 0 || !token.contains("-")) {
      return whole;
    }
    int sum = 0;
    for (String part : token.split("-")) {
      sum += valueOfWord(part);
    }
    return Integer.signum(sum);
  }

  private int valueOfWord(String word) {
    if (lexicon.isNegative(word)) {
      return -1;
    }
    if (lexicon.isPositive(word)) {
      return 1;
    }
    return 0;
  }

  private static boolean isNegated(List<String> tokens, int index) {
    for (int i = Math.max(0, index - NEGATION_WINDOW); i < index; i++) {
      if (NEGATIONS.contains(tokens.get(i))) {
        return true;
      }
    }
    return false;
  }

  private static List<String> tokenize(String text) {
    final List<String> tokens = new ArrayList<>();
    final Matcher matcher = WORD.matcher(text);
    while (matcher.find()) {
      tokens.add(matcher.group().toUpperCase(Locale.ROOT));
    }
    return tokens;
  }
}
