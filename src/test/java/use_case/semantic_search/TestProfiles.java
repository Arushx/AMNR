package use_case.semantic_search;

import entity.MarketCapClass;
import entity.StockProfile;
import java.util.List;

/** A few hand-made profiles shared by the semantic search tests. */
final class TestProfiles {

  static final StockProfile EXXON =
      new StockProfile("XOM", "Exxon Mobil Corporation", "Energy", "Oil, Gas & Fuels",
          MarketCapClass.MEGA, "Explores for and produces crude oil and natural gas.");
  static final StockProfile DRILLER =
      new StockProfile("TINY", "Tiny Drilling Inc", "Energy", "Oil, Gas & Fuels",
          MarketCapClass.NANO, "Drills small oil wells in Texas.");
  static final StockProfile CONOCO =
      new StockProfile("COP", "ConocoPhillips", "Energy", "Oil, Gas & Fuels",
          MarketCapClass.LARGE, "Explores for and produces crude oil.");
  static final StockProfile MYSTERY =
      new StockProfile("MYST", "Mystery Oil Co", "Energy", "Oil, Gas & Fuels",
          MarketCapClass.UNKNOWN, "");
  static final StockProfile APPLE =
      new StockProfile("AAPL", "Apple Inc.", "Information Technology", "Computers & Hardware",
          MarketCapClass.MEGA, "Designs smartphones and personal computers.");
  static final StockProfile BANK =
      new StockProfile("BAC", "Bank of America Corporation", "Financials", "Banks",
          MarketCapClass.MEGA, "Provides banking and financial products.");

  static final List<StockProfile> ALL = List.of(EXXON, DRILLER, CONOCO, MYSTERY, APPLE, BANK);

  private TestProfiles() {
  }
}
