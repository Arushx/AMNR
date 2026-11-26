package data_access;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Splits text into the word pieces a BERT-style model understands, and looks up their numbers.
 *
 * <p>A model can't read letters; it reads token ids from a fixed vocabulary (about 30,000 entries
 * in {@code vocab.txt}). Common words are one token ("oil"); rarer words are built from pieces
 * ("petrochemicals" becomes "petro", "##chemical", "##s"). This is the "uncased" BERT scheme used
 * by all-MiniLM-L6-v2:
 *
 * <ol>
 *   <li>Lowercase, remove accents and control characters.
 *   <li>Split on spaces, and make every punctuation mark its own token.
 *   <li>Split each word into the longest pieces found in the vocabulary.
 *   <li>Wrap the result in the special [CLS] ... [SEP] tokens.
 * </ol>
 */
public class WordPieceTokenizer {

  static final String CLS = "[CLS]";
  static final String SEP = "[SEP]";
  static final String UNK = "[UNK]";

  /** Words longer than this are not split; they become [UNK] (same limit as BERT). */
  private static final int MAX_WORD_CHARS = 100;

  private final Map<String, Integer> vocab;

  /**
   * Creates a tokenizer from a vocabulary.
   *
   * @param vocab each token mapped to its id; must contain [CLS], [SEP] and [UNK]
   * @throws IllegalArgumentException if a special token is missing
   */
  public WordPieceTokenizer(Map<String, Integer> vocab) {
    for (String special : new String[] {CLS, SEP, UNK}) {
      if (!vocab.containsKey(special)) {
        throw new IllegalArgumentException("vocabulary has no " + special + " token");
      }
    }
    this.vocab = Map.copyOf(vocab);
  }

  /**
   * Loads a {@code vocab.txt} file, where line N (counting from 0) is the token with id N.
   *
   * @param file the vocabulary file
   * @return the tokenizer
   * @throws IOException if the file can't be read
   */
  public static WordPieceTokenizer fromVocabFile(Path file) throws IOException {
    final List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
    final Map<String, Integer> vocab = new HashMap<>();
    for (int id = 0; id < lines.size(); id++) {
      vocab.put(lines.get(id), id);
    }
    return new WordPieceTokenizer(vocab);
  }

  /**
   * Turns text into token ids, ready for the model.
   *
   * @param text the text
   * @param maxTokens the most ids to return, counting [CLS] and [SEP]; longer text is cut off
   * @return the ids, starting with [CLS] and ending with [SEP]
   */
  public long[] encode(String text, int maxTokens) {
    final List<String> pieces = tokenize(text);
    final int kept = Math.min(pieces.size(), maxTokens - 2);
    final long[] ids = new long[kept + 2];
    ids[0] = vocab.get(CLS);
    for (int i = 0; i < kept; i++) {
      ids[i + 1] = vocab.get(pieces.get(i));
    }
    ids[kept + 1] = vocab.get(SEP);
    return ids;
  }

  /**
   * Splits text into word pieces (without [CLS] and [SEP]).
   *
   * @param text the text
   * @return the pieces, each one in the vocabulary
   */
  public List<String> tokenize(String text) {
    final List<String> pieces = new ArrayList<>();
    for (String word : splitWords(text)) {
      pieces.addAll(wordPieces(word));
    }
    return pieces;
  }

  /** Steps 1 and 2: clean the text and split it into words and punctuation marks. */
  private static List<String> splitWords(String text) {
    final String lower = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD);
    final List<String> words = new ArrayList<>();
    final StringBuilder word = new StringBuilder();
    for (int i = 0; i < lower.length(); i++) {
      final char c = lower.charAt(i);
      if (Character.getType(c) == Character.NON_SPACING_MARK
          || (Character.isISOControl(c) && !Character.isWhitespace(c))) {
        continue; // accents (after NFD, "é" is "e" + accent) and control characters are dropped
      }
      if (Character.isWhitespace(c) || isPunctuation(c)) {
        if (word.length() > 0) {
          words.add(word.toString());
          word.setLength(0);
        }
        if (isPunctuation(c)) {
          words.add(String.valueOf(c));
        }
      } else {
        word.append(c);
      }
    }
    if (word.length() > 0) {
      words.add(word.toString());
    }
    return words;
  }

  /** BERT treats every ASCII symbol (such as '$') and all Unicode punctuation as punctuation. */
  private static boolean isPunctuation(char c) {
    if (c >= 33 && c <= 47 || c >= 58 && c <= 64 || c >= 91 && c <= 96 || c >= 123 && c <= 126) {
      return true;
    }
    final int type = Character.getType(c);
    return type == Character.CONNECTOR_PUNCTUATION
        || type == Character.DASH_PUNCTUATION
        || type == Character.START_PUNCTUATION
        || type == Character.END_PUNCTUATION
        || type == Character.INITIAL_QUOTE_PUNCTUATION
        || type == Character.FINAL_QUOTE_PUNCTUATION
        || type == Character.OTHER_PUNCTUATION;
  }

  /**
   * Step 3: splits one word into the longest pieces in the vocabulary, left to right. Pieces after
   * the first are marked with "##". If any part can't be matched, the whole word is [UNK].
   */
  private List<String> wordPieces(String word) {
    if (word.length() > MAX_WORD_CHARS) {
      return List.of(UNK);
    }
    final List<String> pieces = new ArrayList<>();
    int start = 0;
    while (start < word.length()) {
      String match = null;
      int end = word.length();
      while (end > start) {
        final String candidate = (start > 0 ? "##" : "") + word.substring(start, end);
        if (vocab.containsKey(candidate)) {
          match = candidate;
          break;
        }
        end--;
      }
      if (match == null) {
        return List.of(UNK);
      }
      pieces.add(match);
      start = end;
    }
    return pieces;
  }
}
