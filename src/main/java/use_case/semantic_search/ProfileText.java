package use_case.semantic_search;

import entity.StockProfile;

/** Builds the text that represents a stock when it is compared with a search. */
final class ProfileText {

  private ProfileText() {
  }

  /**
   * Joins a profile's name, sector, industry and description into one text, e.g. "Exxon Mobil
   * Corporation. Energy, Oil, Gas &amp; Fuels. Exxon Mobil Corporation explores for ...".
   *
   * @param profile the stock's profile
   * @return the text to embed or search in
   */
  static String of(StockProfile profile) {
    return profile.getName() + ". " + profile.getSector() + ", " + profile.getIndustry() + ". "
        + profile.getDescription();
  }
}
