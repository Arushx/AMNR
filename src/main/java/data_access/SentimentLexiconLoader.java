package data_access;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import use_case.explain_movement.SentimentLexicon;

/**
 * Builds a {@link SentimentLexicon} from the Loughran-McDonald Master Dictionary in {@code
 * src/main/resources/sentiment/}.
 *
 * <p>A word is in a list when its "Positive" or "Negative" column is not 0 (the column holds the
 * year the word was added). Words with 0 in both columns are in the file but carry no sentiment.
 */
public class SentimentLexiconLoader {

  static final String DICTIONARY = "/sentiment/Loughran-McDonald_MasterDictionary_1993-2025.csv";

  /**
   * Loads the lexicon from the bundled dictionary file.
   *
   * @return the lexicon
   * @throws IOException if the file is missing or can't be read
   */
  public SentimentLexicon loadDefault() throws IOException {
    try (InputStream dictionary = open(DICTIONARY)) {
      return load(dictionary);
    }
  }

  /**
   * Loads the lexicon from a Loughran-McDonald Master Dictionary CSV.
   *
   * @param dictionary the Master Dictionary CSV
   * @return the lexicon
   * @throws IOException if the file can't be read or lacks the needed columns
   */
  public SentimentLexicon load(InputStream dictionary) throws IOException {
    final Set<String> positive = new HashSet<>();
    final Set<String> negative = new HashSet<>();
    readDictionary(dictionary, positive, negative);
    return new SentimentLexicon(positive, negative);
  }

  private static void readDictionary(InputStream in, Set<String> positive, Set<String> negative)
      throws IOException {
    final BufferedReader reader = reader(in);
    final String header = reader.readLine();
    if (header == null) {
      throw new IOException("sentiment dictionary is empty");
    }
    final List<String> columns = List.of(stripBom(header).split(","));
    final int word = columns.indexOf("Word");
    final int neg = columns.indexOf("Negative");
    final int pos = columns.indexOf("Positive");
    if (word < 0 || neg < 0 || pos < 0) {
      throw new IOException("sentiment dictionary needs Word, Negative and Positive columns");
    }
    String line;
    while ((line = reader.readLine()) != null) {
      final String[] cells = line.split(",", -1);
      if (cells.length <= Math.max(word, Math.max(neg, pos)) || cells[word].isBlank()) {
        continue;
      }
      if (isMarked(cells[neg])) {
        negative.add(cells[word]);
      }
      if (isMarked(cells[pos])) {
        positive.add(cells[word]);
      }
    }
  }

  private static boolean isMarked(String cell) {
    final String value = cell.trim();
    return !value.isEmpty() && !"0".equals(value);
  }

  private static String stripBom(String text) {
    return text.startsWith("﻿") ? text.substring(1) : text;
  }

  private static BufferedReader reader(InputStream in) {
    return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
  }

  private static InputStream open(String resource) throws IOException {
    final InputStream in = SentimentLexiconLoader.class.getResourceAsStream(resource);
    if (in == null) {
      throw new IOException("missing resource " + resource);
    }
    return in;
  }
}
