package entity;

/**
 * What a company does: its sector, industry, size and a short description.
 *
 * <p>Semantic search compares a user's sentence (e.g. "best selling oil stocks") with these
 * profiles, so a stock can be found by what it does, not only by its ticker or name.
 *
 * <p>Instances are immutable.
 */
public class StockProfile {

  private final String symbol;
  private final String name;
  private final String sector;
  private final String industry;
  private final MarketCapClass marketCap;
  private final String description;

  /**
   * Creates a profile. Missing text fields become empty strings.
   *
   * @param symbol the ticker symbol, e.g. "XOM"
   * @param name the company name, e.g. "Exxon Mobil Corporation"
   * @param sector the broad sector, e.g. "Energy"
   * @param industry the narrower industry, e.g. "Oil, Gas &amp; Fuels"
   * @param marketCap the size class; null means {@link MarketCapClass#UNKNOWN}
   * @param description a short description of the business; may be empty
   * @throws IllegalArgumentException if the symbol is blank
   */
  public StockProfile(
      String symbol,
      String name,
      String sector,
      String industry,
      MarketCapClass marketCap,
      String description) {
    if (symbol == null || symbol.isBlank()) {
      throw new IllegalArgumentException("symbol must not be blank");
    }
    this.symbol = symbol.trim().toUpperCase();
    this.name = clean(name);
    this.sector = clean(sector);
    this.industry = clean(industry);
    this.marketCap = marketCap == null ? MarketCapClass.UNKNOWN : marketCap;
    this.description = clean(description);
  }

  private static String clean(String text) {
    return text == null ? "" : text.trim();
  }

  public String getSymbol() {
    return symbol;
  }

  public String getName() {
    return name;
  }

  public String getSector() {
    return sector;
  }

  public String getIndustry() {
    return industry;
  }

  public MarketCapClass getMarketCap() {
    return marketCap;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Says whether the profile has a written description, not just a sector and industry.
   *
   * @return true if the description is not empty
   */
  public boolean hasDescription() {
    return !description.isEmpty();
  }
}
