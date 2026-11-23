package use_case;

import entity.StockListing;
import java.util.List;

/** Gives a use case the list of every searchable stock (ticker, company name, exchange). */
public interface StockListingDataAccessInterface {

  /**
   * Returns every known stock.
   *
   * @return the listings
   * @throws StockDataUnavailableException if the list can't be loaded or downloaded
   */
  List<StockListing> getListings() throws StockDataUnavailableException;
}
