package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MarketCapClassTest {

  @Test
  void readsLabelsFromTheData() {
    assertEquals(MarketCapClass.MEGA, MarketCapClass.fromLabel("Mega Cap"));
    assertEquals(MarketCapClass.NANO, MarketCapClass.fromLabel("Nano Cap"));
  }

  @Test
  void ignoresCaseAndSpaces() {
    assertEquals(MarketCapClass.SMALL, MarketCapClass.fromLabel("  small cap "));
  }

  @Test
  void unknownLabelsBecomeUnknown() {
    assertEquals(MarketCapClass.UNKNOWN, MarketCapClass.fromLabel(""));
    assertEquals(MarketCapClass.UNKNOWN, MarketCapClass.fromLabel(null));
    assertEquals(MarketCapClass.UNKNOWN, MarketCapClass.fromLabel("Giant Cap"));
  }

  @Test
  void biggerClassesSortFirst() {
    assertTrue(MarketCapClass.MEGA.compareTo(MarketCapClass.LARGE) < 0);
    assertTrue(MarketCapClass.NANO.compareTo(MarketCapClass.UNKNOWN) < 0);
  }
}
