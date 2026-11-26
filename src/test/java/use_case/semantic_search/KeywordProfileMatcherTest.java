package use_case.semantic_search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class KeywordProfileMatcherTest {

  private final KeywordProfileMatcher matcher = new KeywordProfileMatcher();

  @Test
  void industryWordsScoreMoreThanDescriptionWords() {
    double[] scores =
        matcher.score("oil", List.of(TestProfiles.EXXON, TestProfiles.APPLE));

    // Exxon: industry 3 + description 1; Apple: no "oil" anywhere.
    assertEquals(4.0, scores[0]);
    assertEquals(0.0, scores[1]);
  }

  @Test
  void wordStartsMatchLongerWords() {
    double[] scores = matcher.score("bank", List.of(TestProfiles.BANK));

    assertTrue(scores[0] > 0);
  }

  @Test
  void wordsInsideOtherWordsDoNotMatch() {
    // "hone" appears inside Apple's "smartphones", but not at the start of a word.
    double[] scores = matcher.score("hone", List.of(TestProfiles.APPLE));

    assertEquals(0.0, scores[0]);
  }

  @Test
  void shortWordsAreIgnored() {
    assertEquals(0.0, matcher.score("a", List.of(TestProfiles.APPLE))[0]);
  }
}
