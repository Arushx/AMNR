package data_access;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Turns text into the token ids a BERT model such as FinBERT expects, matching Hugging Face's
 * "uncased" BertTokenizer.
 *
 * <ol>
 *   <li>Clean: drop control characters, turn any whitespace into a space.
 *   <li>Lower-case and remove accents ("Café" becomes "cafe").
 *   <li>Split on spaces and around punctuation ("didn't" becomes "didn", "'", "t").
 *   <li>WordPiece: split each word into the longest pieces found in {@code vocab.txt}; later
 *       pieces start with "##" ("acme" becomes "ac", "##me"). A word that can't be split this way
 *       becomes [UNK].
 *   <li>Add [CLS] at the start and [SEP] at the end, and cut to a maximum length.
 * </ol>
 *
 * <p>It is plain Java with no native code, so it is easy to test against Python's output.
 */
public class BertTokenizer {

  static final String CLS = "[CLS]";
  static final String SEP = "[SEP]";
  static final String PAD = "[PAD]";
  static final String UNK = "[UNK]";
  private static final String CONTINUATION = "##";
  private static final int MAX_WORD_CHARS = 100;

  private final Map<String, Integer> vocab;
  private final int clsId;
  private final int sepId;
  private final int padId;
  private final int unkId;

  /**
   * Creates a tokenizer from a vocabulary (one token per line; the line number is the id).
   *
   * @param vocab the vocabulary, e.g. FinBERT's {@code vocab.txt}
   * @throws IllegalArgumentException if a special token is missing
   */
  public BertTokenizer(Map<String, Integer> vocab) {
    this.vocab = Map.copyOf(vocab);
    this.clsId = requireId(CLS);
    this.sepId = requireId(SEP);
    this.padId = requireId(PAD);
    this.unkId = requireId(UNK);
  }

  /**
   * Reads a {@code vocab.txt} file.
   *
   * @param in the file's contents
   * @return a tokenizer using it
   * @throws IOException if it can't be read
   */
  public static BertTokenizer fromVocab(InputStream in) throws IOException {
    final Map<String, Integer> vocab = new HashMap<>();
    final BufferedReader reader =
        new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    String line;
    int id = 0;
    while ((line = reader.readLine()) != null) {
      vocab.put(line, id);
      id++;
    }
    return new BertTokenizer(vocab);
  }

  public int getPadId() {
    return padId;
  }

  /**
   * Encodes text as token ids: [CLS], the word pieces, [SEP].
   *
   * @param text any text
   * @param maxLength the most ids to return, including [CLS] and [SEP]; at least 2
   * @return the ids; extra word pieces are cut off before [SEP]
   */
  public long[] encode(String text, int maxLength) {
    if (maxLength < 2) {
      throw new IllegalArgumentException("maxLength must be at least 2");
    }
    final List<String> pieces = tokenize(text);
    final int kept = Math.min(pieces.size(), maxLength - 2);
    final long[] ids = new long[kept + 2];
    ids[0] = clsId;
    for (int i = 0; i < kept; i++) {
      ids[i + 1] = vocab.getOrDefault(pieces.get(i), unkId);
    }
    ids[kept + 1] = sepId;
    return ids;
  }

  /**
   * Splits text into word pieces, without [CLS] and [SEP].
   *
   * @param text any text
   * @return the word pieces, e.g. ["ac", "##me", "shares"]
   */
  public List<String> tokenize(String text) {
    final List<String> pieces = new ArrayList<>();
    for (String word : splitWords(normalize(text))) {
      pieces.addAll(wordPieces(word));
    }
    return pieces;
  }

  /** Cleans, separates Chinese characters, lower-cases and removes accents. */
  static String normalize(String text) {
    final StringBuilder cleaned = new StringBuilder();
    text.codePoints().forEach(cp -> {
      if (cp == 0 || cp == 0xFFFD || isControl(cp)) {
        return;
      }
      if (isWhitespace(cp)) {
        cleaned.append(' ');
      } else if (isChineseChar(cp)) {
        cleaned.append(' ').appendCodePoint(cp).append(' ');
      } else {
        cleaned.appendCodePoint(cp);
      }
    });
    final String lower = cleaned.toString().toLowerCase(Locale.ROOT);
    final String decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD);
    final StringBuilder noAccents = new StringBuilder();
    decomposed.codePoints().forEach(cp -> {
      if (Character.getType(cp) != Character.NON_SPACING_MARK) {
        noAccents.appendCodePoint(cp);
      }
    });
    return noAccents.toString();
  }

  /** Splits on spaces, and makes every punctuation character its own word. */
  static List<String> splitWords(String text) {
    final List<String> words = new ArrayList<>();
    final StringBuilder current = new StringBuilder();
    text.codePoints().forEach(cp -> {
      if (cp == ' ') {
        flush(current, words);
      } else if (isPunctuation(cp)) {
        flush(current, words);
        words.add(new String(Character.toChars(cp)));
      } else {
        current.appendCodePoint(cp);
      }
    });
    flush(current, words);
    return words;
  }

  /** Greedy longest-match WordPiece, as in BERT. */
  List<String> wordPieces(String word) {
    if (word.codePointCount(0, word.length()) > MAX_WORD_CHARS) {
      return List.of(UNK);
    }
    final List<String> pieces = new ArrayList<>();
    int start = 0;
    while (start < word.length()) {
      int end = word.length();
      String match = null;
      while (start < end) {
        final String candidate =
            (start > 0 ? CONTINUATION : "") + word.substring(start, end);
        if (vocab.containsKey(candidate)) {
          match = candidate;
          break;
        }
        end = word.offsetByCodePoints(end, -1);
      }
      if (match == null) {
        return List.of(UNK);
      }
      pieces.add(match);
      start = end;
    }
    return pieces;
  }

  private static void flush(StringBuilder current, List<String> words) {
    if (current.length() > 0) {
      words.add(current.toString());
      current.setLength(0);
    }
  }

  private int requireId(String token) {
    final Integer id = vocab.get(token);
    if (id == null) {
      throw new IllegalArgumentException("vocabulary has no " + token + " token");
    }
    return id;
  }

  private static boolean isWhitespace(int cp) {
    return cp == ' ' || cp == '\t' || cp == '\n' || cp == '\r'
        || Character.getType(cp) == Character.SPACE_SEPARATOR;
  }

  private static boolean isControl(int cp) {
    if (cp == '\t' || cp == '\n' || cp == '\r') {
      return false;
    }
    final int type = Character.getType(cp);
    return type == Character.CONTROL || type == Character.FORMAT;
  }

  private static boolean isPunctuation(int cp) {
    if (cp >= 33 && cp <= 47 || cp >= 58 && cp <= 64 || cp >= 91 && cp <= 96
        || cp >= 123 && cp <= 126) {
      return true;
    }
    switch (Character.getType(cp)) {
      case Character.CONNECTOR_PUNCTUATION:
      case Character.DASH_PUNCTUATION:
      case Character.START_PUNCTUATION:
      case Character.END_PUNCTUATION:
      case Character.INITIAL_QUOTE_PUNCTUATION:
      case Character.FINAL_QUOTE_PUNCTUATION:
      case Character.OTHER_PUNCTUATION:
        return true;
      default:
        return false;
    }
  }

  /** CJK ideographs, which BERT treats as separate words. */
  private static boolean isChineseChar(int cp) {
    return cp >= 0x4E00 && cp <= 0x9FFF
        || cp >= 0x3400 && cp <= 0x4DBF
        || cp >= 0x20000 && cp <= 0x2A6DF
        || cp >= 0x2A700 && cp <= 0x2B73F
        || cp >= 0x2B740 && cp <= 0x2B81F
        || cp >= 0x2B820 && cp <= 0x2CEAF
        || cp >= 0xF900 && cp <= 0xFAFF
        || cp >= 0x2F800 && cp <= 0x2FA1F;
  }
}
