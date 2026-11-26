package use_case.semantic_search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SearchQueryParserTest {

  private final SearchQueryParser parser = new SearchQueryParser();

  @Test
  void rankingWordsBecomeLargestFirst() {
    ParsedQuery parsed = parser.parse("Best selling oil stocks");

    assertEquals("oil", parsed.getTopic());
    assertEquals(SortOrder.LARGEST, parsed.getSortOrder());
  }

  @Test
  void smallCapBecomesSmallestFirst() {
    ParsedQuery parsed = parser.parse("small cap biotech");

    assertEquals("biotech", parsed.getTopic());
    assertEquals(SortOrder.SMALLEST, parsed.getSortOrder());
  }

  @Test
  void mostExpensiveMeansBiggestCompanies() {
    ParsedQuery parsed = parser.parse("most expensive tech stocks");

    assertEquals("tech", parsed.getTopic());
    assertEquals(SortOrder.LARGEST, parsed.getSortOrder());
  }

  @Test
  void cheapMeansSmallestCompanies() {
    ParsedQuery parsed = parser.parse("cheap gold miners");

    assertEquals("gold miners", parsed.getTopic());
    assertEquals(SortOrder.SMALLEST, parsed.getSortOrder());
  }

  @Test
  void fillerWordsAreDropped() {
    ParsedQuery parsed = parser.parse("show me companies that make computer chips");

    assertEquals("computer chips", parsed.getTopic());
    assertEquals(SortOrder.RELEVANCE, parsed.getSortOrder());
  }

  @Test
  void punctuationIsIgnored() {
    assertEquals("airlines", parser.parse("airlines?!").getTopic());
  }

  @Test
  void wordsInsideOtherWordsAreNotRankingWords() {
    // "top" is a ranking word, but "laptop" is not.
    ParsedQuery parsed = parser.parse("laptop makers");

    assertEquals("laptop", parsed.getTopic());
    assertEquals(SortOrder.RELEVANCE, parsed.getSortOrder());
  }

  @Test
  void onlyRankingWordsKeepsTheWholeQuery() {
    ParsedQuery parsed = parser.parse("biggest stocks");

    assertEquals("biggest stocks", parsed.getTopic());
    assertEquals(SortOrder.LARGEST, parsed.getSortOrder());
  }
}
