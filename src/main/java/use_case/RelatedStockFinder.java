package use_case;

import entity.StockListing;
import java.util.List;

/**
 * Finds stocks related to a sentence such as "best selling oil stocks", for use cases that want
 * to offer them (e.g. Search Stock, when no ticker or company name matches).
 */
public interface RelatedStockFinder {

  /**
   * Finds the stocks that best match a sentence.
   *
   * @param query what the user typed
   * @param limit the most stocks to return
   * @return the matches, best first; empty if nothing matches
   * @throws StockDataUnavailableException if the stock data can't be loaded
   */
  List<StockListing> findRelated(String query, int limit) throws StockDataUnavailableException;
}
