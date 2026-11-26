package data_access;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WordPieceTokenizerTest {

  // tiny_vocab.txt ids: [PAD]=0 [UNK]=1 [CLS]=2 [SEP]=3 oil=4 stocks=5 best=6 selling=7
  // petro=8 ##chemical=9 ##s=10 ,=11 &=12 cafe=13 $=14
  private WordPieceTokenizer tokenizer;

  @BeforeEach
  void setUp() throws IOException, URISyntaxException {
    Path vocab = Path.of(getClass().getResource("/embedding/tiny_vocab.txt").toURI());
    tokenizer = WordPieceTokenizer.fromVocabFile(vocab);
  }

  @Test
  void lowercasesAndSplitsOnSpaces() {
    assertEquals(List.of("best", "selling", "oil", "stocks"),
        tokenizer.tokenize("Best  SELLING oil\tstocks"));
  }

  @Test
  void splitsRareWordsIntoPieces() {
    assertEquals(List.of("petro", "##chemical", "##s"), tokenizer.tokenize("petrochemicals"));
  }

  @Test
  void punctuationBecomesItsOwnToken() {
    assertEquals(List.of("oil", ",", "stocks", "&", "$"), tokenizer.tokenize("oil,stocks&$"));
  }

  @Test
  void removesAccents() {
    assertEquals(List.of("cafe"), tokenizer.tokenize("Café"));
  }

  @Test
  void unknownWordsBecomeUnk() {
    assertEquals(List.of("oil", "[UNK]"), tokenizer.tokenize("oil xyz"));
  }

  @Test
  void encodeWrapsIdsInClsAndSep() {
    assertArrayEquals(new long[] {2, 6, 4, 5, 3}, tokenizer.encode("best oil stocks", 16));
  }

  @Test
  void encodeCutsOffLongText() {
    assertArrayEquals(new long[] {2, 6, 4, 3}, tokenizer.encode("best oil stocks", 4));
  }

  @Test
  void rejectsVocabularyWithoutSpecialTokens() {
    assertThrows(IllegalArgumentException.class, () -> new WordPieceTokenizer(Map.of("oil", 0)));
  }
}
