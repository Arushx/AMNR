package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.MarketCapClass;
import entity.StockProfile;
import java.util.List;
import org.junit.jupiter.api.Test;
import use_case.StockDataUnavailableException;

class ResourceStockProfileDataAccessObjectTest {

  private static final String SAMPLE = "/profiles/stock_profiles_sample.csv";

  @Test
  void readsEveryProfileFromTheSample() throws StockDataUnavailableException {
    List<StockProfile> profiles = new ResourceStockProfileDataAccessObject(SAMPLE).getProfiles();

    assertEquals(4, profiles.size());
    StockProfile xom = profiles.get(3);
    assertEquals("XOM", xom.getSymbol());
    assertEquals("Energy", xom.getSector());
    assertEquals("Oil, Gas & Fuels", xom.getIndustry());
    assertEquals(MarketCapClass.MEGA, xom.getMarketCap());
    assertTrue(xom.getDescription().startsWith("Exxon Mobil Corporation explores"));
  }

  @Test
  void handlesQuotesAndEmptyFields() throws StockDataUnavailableException {
    List<StockProfile> profiles = new ResourceStockProfileDataAccessObject(SAMPLE).getProfiles();

    assertFalse(profiles.get(0).hasDescription());
    StockProfile quoted = profiles.get(2);
    assertEquals("Quote \"Test\" Co", quoted.getName());
    assertEquals("It sells \"smart\" things.", quoted.getDescription());
    assertEquals(MarketCapClass.UNKNOWN, quoted.getMarketCap());
  }

  @Test
  void keepsProfilesInMemoryAfterTheFirstLoad() throws StockDataUnavailableException {
    ResourceStockProfileDataAccessObject dataAccess =
        new ResourceStockProfileDataAccessObject(SAMPLE);

    assertSame(dataAccess.getProfiles(), dataAccess.getProfiles());
  }

  @Test
  void missingFileIsReported() {
    ResourceStockProfileDataAccessObject dataAccess =
        new ResourceStockProfileDataAccessObject("/profiles/does_not_exist.csv");

    assertThrows(StockDataUnavailableException.class, dataAccess::getProfiles);
  }

  @Test
  void wrongHeaderIsReported() {
    assertThrows(
        StockDataUnavailableException.class,
        () -> ResourceStockProfileDataAccessObject.parse("symbol,name\nXOM,Exxon\n"));
  }

  @Test
  void wrongNumberOfFieldsIsReported() {
    String csv = "symbol,name,sector,industry,market_cap,description\nXOM,Exxon,Energy\n";

    StockDataUnavailableException e =
        assertThrows(
            StockDataUnavailableException.class,
            () -> ResourceStockProfileDataAccessObject.parse(csv));
    assertTrue(e.getMessage().contains("line 2"));
  }

  @Test
  void theRealBundledFileLoads() throws StockDataUnavailableException {
    List<StockProfile> profiles =
        new ResourceStockProfileDataAccessObject(
                ResourceStockProfileDataAccessObject.DEFAULT_RESOURCE)
            .getProfiles();

    assertTrue(profiles.size() > 5000);
    assertTrue(profiles.stream().anyMatch(p -> p.getSymbol().equals("XOM")));
  }
}
