package use_case.semantic_search;

import entity.StockProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Scores profiles by counting the search words they contain: the fallback when the embedding model
 * can't run. It needs no model, so it works everywhere, but it only finds exact words ("oil"
 * won't find "crude").
 *
 * <p>A word in the industry counts most, then the sector or name, then the description.
 */
public class KeywordProfileMatcher implements ProfileMatcher {

  private static final int INDUSTRY_POINTS = 3;
  private static final int SECTOR_OR_NAME_POINTS = 2;
  private static final int DESCRIPTION_POINTS = 1;

  @Override
  public double[] score(String topic, List<StockProfile> profiles) {
    final List<Pattern> words = new ArrayList<>();
    for (String word : topic.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
      if (word.length() >= 2) {
        // Match whole words or their start, so "bank" also finds "banks" and "banking".
        words.add(Pattern.compile("\\b" + Pattern.quote(word)));
      }
    }
    final double[] scores = new double[profiles.size()];
    for (int i = 0; i < profiles.size(); i++) {
      final StockProfile profile = profiles.get(i);
      for (Pattern word : words) {
        scores[i] += points(word, profile.getIndustry(), INDUSTRY_POINTS)
            + points(word, profile.getSector(), SECTOR_OR_NAME_POINTS)
            + points(word, profile.getName(), SECTOR_OR_NAME_POINTS)
            + points(word, profile.getDescription(), DESCRIPTION_POINTS);
      }
    }
    return scores;
  }

  private static int points(Pattern word, String field, int points) {
    return word.matcher(field.toLowerCase(Locale.ROOT)).find() ? points : 0;
  }
}
