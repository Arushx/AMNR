package use_case;

import entity.StockProfile;
import java.util.List;

/** Gives a use case the profile (sector, industry, size, description) of every known stock. */
public interface StockProfileDataAccessInterface {

  /**
   * Returns every stock profile.
   *
   * @return the profiles
   * @throws StockDataUnavailableException if the profiles can't be loaded
   */
  List<StockProfile> getProfiles() throws StockDataUnavailableException;
}
