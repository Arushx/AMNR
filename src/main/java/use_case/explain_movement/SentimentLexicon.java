package use_case.explain_movement;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Two word lists: words that make news sound good and words that make it sound bad.
 *
 * <p>Words are stored in upper case. Instances are immutable. Where the lists come from (e.g. the
 * Loughran-McDonald dictionary) is not this class's concern; a data access class loads them.
 */
public class SentimentLexicon {

  private final Set<String> positiveWords;
  private final Set<String> negativeWords;

  /**
   * Creates a lexicon.
   *
   * @param positiveWords words with a positive meaning, in any case
   * @param negativeWords words with a negative meaning, in any case
   * @throws IllegalArgumentException if a set is null
   */
  public SentimentLexicon(Set<String> positiveWords, Set<String> negativeWords) {
    if (positiveWords == null || negativeWords == null) {
      throw new IllegalArgumentException("word sets must not be null");
    }
    this.positiveWords = upperCaseCopy(positiveWords);
    this.negativeWords = upperCaseCopy(negativeWords);
  }

  /**
   * Tells whether a word is in the positive list.
   *
   * @param word a word, in any case
   * @return true if it is a positive word
   */
  public boolean isPositive(String word) {
    return positiveWords.contains(word.toUpperCase());
  }

  /**
   * Tells whether a word is in the negative list.
   *
   * @param word a word, in any case
   * @return true if it is a negative word
   */
  public boolean isNegative(String word) {
    return negativeWords.contains(word.toUpperCase());
  }

  /**
   * Returns how many positive words there are.
   *
   * @return the size of the positive list
   */
  public int positiveCount() {
    return positiveWords.size();
  }

  /**
   * Returns how many negative words there are.
   *
   * @return the size of the negative list
   */
  public int negativeCount() {
    return negativeWords.size();
  }

  private static Set<String> upperCaseCopy(Set<String> words) {
    final Set<String> copy = new HashSet<>();
    for (String word : words) {
      copy.add(word.trim().toUpperCase());
    }
    return Collections.unmodifiableSet(copy);
  }
}
