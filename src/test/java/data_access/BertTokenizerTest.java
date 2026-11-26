package data_access;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

/**
 * Checks the Java tokenizer against Hugging Face's own tokenizer.
 *
 * <p>{@code src/test/resources/finbert/reference.json} holds the tokens and ids that Hugging
 * Face's Python {@code tokenizers} library produced for each text, using FinBERT's vocab.txt (see
 * the README in that folder). The Java output must match exactly.
 */
class BertTokenizerTest {

  static BertTokenizer finbertTokenizer() throws IOException {
    try (InputStream in = BertTokenizerTest.class.getResourceAsStream("/finbert/vocab.txt")) {
      return BertTokenizer.fromVocab(in);
    }
  }

  static JSONObject reference() throws IOException {
    try (InputStream in = BertTokenizerTest.class.getResourceAsStream("/finbert/reference.json")) {
      return new JSONObject(new String(in.readAllBytes(), StandardCharsets.UTF_8));
    }
  }

  private static void assertMatches(BertTokenizer tokenizer, JSONObject testCase) {
    final String text = testCase.getString("text");
    final List<String> expectedTokens = new ArrayList<>();
    final JSONArray tokens = testCase.getJSONArray("tokens");
    for (int i = 1; i < tokens.length() - 1; i++) {
      expectedTokens.add(tokens.getString(i));
    }
    final JSONArray ids = testCase.getJSONArray("ids");
    final long[] expectedIds = new long[ids.length()];
    for (int i = 0; i < ids.length(); i++) {
      expectedIds[i] = ids.getLong(i);
    }

    assertEquals(expectedTokens, tokenizer.tokenize(text), "tokens for: " + text);
    assertArrayEquals(expectedIds, tokenizer.encode(text, 512), "ids for: " + text);
  }

  @Test
  void matchesHuggingFaceOnHeadlines() throws IOException {
    BertTokenizer tokenizer = finbertTokenizer();
    JSONArray cases = reference().getJSONArray("model_cases");
    for (int i = 0; i < cases.length(); i++) {
      assertMatches(tokenizer, cases.getJSONObject(i));
    }
  }

  @Test
  void matchesHuggingFaceOnAwkwardText() throws IOException {
    // Accents, curly quotes, Chinese characters, emoji, tabs, a 120-letter word, full-width
    // letters, zero-width spaces and soft hyphens.
    BertTokenizer tokenizer = finbertTokenizer();
    JSONArray cases = reference().getJSONArray("tokenizer_cases");
    for (int i = 0; i < cases.length(); i++) {
      assertMatches(tokenizer, cases.getJSONObject(i));
    }
  }

  @Test
  void cutsLongTextButKeepsClsAndSep() throws IOException {
    BertTokenizer tokenizer = finbertTokenizer();

    long[] ids = tokenizer.encode("one two three four five", 4);

    assertEquals(4, ids.length);
    assertEquals(101, ids[0]);
    assertEquals(102, ids[3]);
  }

  @Test
  void wordPiecesUseLongestMatchFirst() {
    BertTokenizer tokenizer =
        new BertTokenizer(
            Map.of("[PAD]", 0, "[UNK]", 1, "[CLS]", 2, "[SEP]", 3,
                "un", 4, "unhappy", 5, "##ness", 6, "##happi", 7));

    assertEquals(List.of("unhappy"), tokenizer.tokenize("unhappy"));
    assertEquals(List.of("un", "##happi", "##ness"), tokenizer.tokenize("unhappiness"));
    assertEquals(List.of("[UNK]"), tokenizer.tokenize("happy"));
  }

  @Test
  void vocabularyNeedsSpecialTokens() {
    assertThrows(IllegalArgumentException.class, () -> new BertTokenizer(Map.of("a", 0)));
    assertThrows(IllegalArgumentException.class, () -> finbertTokenizer().encode("x", 1));
  }
}
