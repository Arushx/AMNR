package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StockProfileTest {

  @Test
  void keepsAllFieldsAndNormalisesSymbol() {
    StockProfile profile =
        new StockProfile(
            " xom ",
            " Exxon Mobil Corporation ",
            "Energy",
            "Oil, Gas & Fuels",
            MarketCapClass.MEGA,
            "Exxon Mobil Corporation explores for and produces crude oil and natural gas.");

    assertEquals("XOM", profile.getSymbol());
    assertEquals("Exxon Mobil Corporation", profile.getName());
    assertEquals("Energy", profile.getSector());
    assertEquals("Oil, Gas & Fuels", profile.getIndustry());
    assertEquals(MarketCapClass.MEGA, profile.getMarketCap());
    assertTrue(profile.hasDescription());
  }

  @Test
  void missingValuesBecomeEmptyOrUnknown() {
    StockProfile profile = new StockProfile("AACB", null, null, null, null, null);

    assertEquals("", profile.getName());
    assertEquals("", profile.getSector());
    assertEquals("", profile.getIndustry());
    assertEquals(MarketCapClass.UNKNOWN, profile.getMarketCap());
    assertFalse(profile.hasDescription());
  }

  @Test
  void rejectsBlankSymbol() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new StockProfile(" ", "x", "y", "z", MarketCapClass.MID, "d"));
  }
}
