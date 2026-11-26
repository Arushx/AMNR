package data_access;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import entity.NewsArticle;
import entity.Sentiment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import use_case.explain_movement.SentimentUnavailableException;

class LocalFinbertSentimentClassifierTest {

  private static final List<Sentiment> FINBERT_LABELS =
      List.of(Sentiment.POSITIVE, Sentiment.NEGATIVE, Sentiment.NEUTRAL);

  private static NewsArticle article(String headline, String summary) {
    return new NewsArticle(
        headline, summary, "Example Wire", "https://example.com/" + headline.hashCode(),
        LocalDateTime.of(2026, 3, 3, 9, 0), List.of("ACME"));
  }

  @Test
  void readsTheLabelOrderFromConfig() throws IOException {
    String config =
        new String(
            getClass().getResourceAsStream("/finbert/config.json").readAllBytes(),
            java.nio.charset.StandardCharsets.UTF_8);

    assertEquals(FINBERT_LABELS, LocalFinbertSentimentClassifier.readLabels(config));
  }

  @Test
  void rejectsConfigWithoutUsableLabels() {
    assertThrows(IOException.class, () -> LocalFinbertSentimentClassifier.readLabels("{}"));
    assertThrows(
        IOException.class,
        () -> LocalFinbertSentimentClassifier.readLabels("{\"id2label\": {\"0\": \"happy\"}}"));
    assertThrows(
        IOException.class,
        () -> LocalFinbertSentimentClassifier.readLabels(
            "{\"id2label\": {\"0\": \"positive\", \"2\": \"neutral\"}}"));
  }

  @Test
  void highestScoreWins() {
    assertEquals(Sentiment.NEGATIVE,
        LocalFinbertSentimentClassifier.labelOf(new float[] {-1.7f, 2.6f, -0.1f}, FINBERT_LABELS));
    assertEquals(Sentiment.POSITIVE,
        LocalFinbertSentimentClassifier.labelOf(new float[] {1f, 1f, 0f}, FINBERT_LABELS));
  }

  @Test
  void padsRowsAndMasksThePadding() {
    List<long[]> ids = List.of(new long[] {101, 5, 102}, new long[] {101, 102});

    assertArrayEquals(
        new long[][] {{101, 5, 102}, {101, 102, 0}}, LocalFinbertSentimentClassifier.pad(ids, 0));
    assertArrayEquals(
        new long[][] {{1, 1, 1}, {1, 1, 0}},
        LocalFinbertSentimentClassifier.attentionMask(ids, 3));
  }

  @Test
  void textIsHeadlinePlusSummary() {
    assertEquals("ACME falls Sales dropped.",
        LocalFinbertSentimentClassifier.textOf(article("ACME falls", "Sales dropped.")));
    assertEquals("ACME falls", LocalFinbertSentimentClassifier.textOf(article("ACME falls", "")));
  }

  /**
   * Runs the real model and compares with the labels Python's ONNX Runtime gave for the same file.
   *
   * <p>Runs only when FINBERT_MODEL_DIR is set or FINBERT_DOWNLOAD is "true". The model is looked
   * for in FINBERT_MODEL_DIR if set, otherwise in {@link FinbertModelFiles#defaultDirectory()}
   * (e.g. {@code C:\Users\you\.amnr\models\finbert-8f269ab}). With FINBERT_DOWNLOAD=true it is
   * downloaded there first, the way the app will (about 111 MB, once); a model already there with
   * the right size is not downloaded again.
   */
  @Test
  void matchesPythonOnRealHeadlines() throws IOException, SentimentUnavailableException {
    final String configured = System.getenv("FINBERT_MODEL_DIR");
    final boolean download =
        "true".equalsIgnoreCase(String.valueOf(System.getenv("FINBERT_DOWNLOAD")).strip());
    assumeTrue(download || configured != null && !configured.isBlank(),
        "set FINBERT_DOWNLOAD=true or FINBERT_MODEL_DIR to run the real model");

    FinbertModelFiles files = new FinbertModelFiles(FinbertModelFiles.directoryOrDefault(configured));
    System.out.println("FinBERT folder: " + files.getDirectory());
    if (download) {
      long start = System.nanoTime();
      files.downloadMissing();
      System.out.printf("Download step took %.1f s%n", (System.nanoTime() - start) / 1e9);
    }
    assertTrue(files.isComplete(),
        "model_quantized.onnx should be in " + files.getDirectory()
            + " (set FINBERT_DOWNLOAD=true to download it there)");

    JSONArray cases = BertTokenizerTest.reference().getJSONArray("model_cases");
    List<NewsArticle> articles = new ArrayList<>();
    List<Integer> caseIndexes = new ArrayList<>();
    for (int i = 0; i < cases.length(); i++) {
      if (!cases.getJSONObject(i).getString("text").isBlank()) {
        articles.add(article(cases.getJSONObject(i).getString("text"), ""));
        caseIndexes.add(i);
      }
    }

    try (LocalFinbertSentimentClassifier classifier = LocalFinbertSentimentClassifier.load(files)) {
      // All texts go through in one padded batch, unlike Python's one-at-a-time reference, so
      // this also checks that padding doesn't change the answers.
      List<Sentiment> labels = classifier.classify(articles);

      for (int i = 0; i < articles.size(); i++) {
        JSONObject expected = cases.getJSONObject(caseIndexes.get(i));
        assertEquals(
            Sentiment.valueOf(expected.getString("label").toUpperCase()), labels.get(i),
            "label for: " + expected.getString("text"));
      }
    }
  }

  @Test
  void loadingWithoutFilesFails(@org.junit.jupiter.api.io.TempDir Path empty) {
    IOException e =
        assertThrows(
            IOException.class,
            () -> LocalFinbertSentimentClassifier.load(new FinbertModelFiles(empty)));
    assertEquals("FinBERT model file is missing in " + empty, e.getMessage());
    assertEquals(0, empty.toFile().list().length);
    assertEquals(false, Files.exists(empty.resolve("model_quantized.onnx")));
  }
}
