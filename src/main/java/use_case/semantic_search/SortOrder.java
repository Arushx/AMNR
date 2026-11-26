package use_case.semantic_search;

/** How search results are ordered after the best matches are found. */
public enum SortOrder {
  /** Closest in meaning first. */
  RELEVANCE,
  /** Biggest companies first ("biggest", "best selling", "top"). */
  LARGEST,
  /** Smallest companies first ("small cap", "penny"). */
  SMALLEST
}
