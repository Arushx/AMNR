package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.NewsArticle;
import entity.Sentiment;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import use_case.explain_movement.LexiconSentimentClassifier;
import use_case.explain_movement.SentimentLexicon;

class SentimentLexiconLoaderTest {

  private static InputStream text(String s) {
    return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void readsOnlyMarkedWordsFromTheDictionary() throws IOException {
    String dictionary =
        "Word,Seq_num,Negative,Positive\n"
            + "LOSS,1,2009,0\n"
            + "GAIN,2,0,2009\n"
            + "TABLE,3,0,0\n";

    SentimentLexicon lexicon = new SentimentLexiconLoader().load(text(dictionary));

    assertTrue(lexicon.isNegative("loss"));
    assertTrue(lexicon.isPositive("gain"));
    assertFalse(lexicon.isPositive("table"));
    assertFalse(lexicon.isNegative("table"));
  }

  @Test
  void dictionaryWithoutSentimentColumnsIsRejected() {
    assertThrows(
        IOException.class,
        () -> new SentimentLexiconLoader().load(text("Word,Seq_num\nLOSS,1\n")));
  }

  @Test
  void loadsTheBundledDictionary() throws IOException {
    SentimentLexicon lexicon = new SentimentLexiconLoader().loadDefault();

    // Counts of marked words in the 1993-2025 dictionary.
    assertEquals(2355, lexicon.negativeCount());
    assertEquals(354, lexicon.positiveCount());
    assertTrue(lexicon.isNegative("downgrade"));
    assertTrue(lexicon.isPositive("gain"));
  }

  @Test
  void realLexiconLabelsTypicalHeadlines() throws IOException {
    LexiconSentimentClassifier classifier =
        new LexiconSentimentClassifier(new SentimentLexiconLoader().loadDefault());

    assertEquals(
        Sentiment.NEGATIVE, classifier.classify(article("ACME reports loss, analysts downgrade")));
    assertEquals(
        Sentiment.POSITIVE, classifier.classify(article("ACME posts strong gains")));
    assertEquals(Sentiment.NEUTRAL, classifier.classify(article("ACME to hold annual meeting")));
  }

  @Test
  void headlineVerbsWithoutASentimentMarkAreNeutral() throws IOException {
    // PLUNGE, PROBE and SURGE are in the dictionary, but with 0 in both the Negative and Positive
    // columns, so they carry no sentiment. Known limitation of using only this dictionary.
    LexiconSentimentClassifier classifier =
        new LexiconSentimentClassifier(new SentimentLexiconLoader().loadDefault());

    assertEquals(Sentiment.NEUTRAL, classifier.classify(article("ACME shares plunge after probe")));
    assertEquals(Sentiment.NEUTRAL, classifier.classify(article("ACME shares surge")));
  }

  private static NewsArticle article(String headline) {
    return new NewsArticle(
        headline, "", "Example Wire", "https://example.com/a",
        LocalDateTime.of(2026, 3, 3, 9, 0), List.of("ACME"));
  }
}
