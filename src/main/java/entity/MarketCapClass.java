package entity;

/**
 * How big a company is, by the total value of its shares (market capitalisation).
 *
 * <p>The constants are declared from largest to smallest, so sorting by this enum's natural order
 * puts the biggest companies first. {@link #UNKNOWN} comes last.
 */
public enum MarketCapClass {
  MEGA("Mega Cap"),
  LARGE("Large Cap"),
  MID("Mid Cap"),
  SMALL("Small Cap"),
  MICRO("Micro Cap"),
  NANO("Nano Cap"),
  UNKNOWN("");

  private final String label;

  MarketCapClass(String label) {
    this.label = label;
  }

  /**
   * Returns the label used in the stock profiles data, e.g. "Mega Cap".
   *
   * @return the label, or an empty string for {@link #UNKNOWN}
   */
  public String getLabel() {
    return label;
  }

  /**
   * Finds the class for a label such as "Mega Cap" (case and extra spaces are ignored).
   *
   * @param label the label from the data; may be null or empty
   * @return the matching class, or {@link #UNKNOWN} if there is none
   */
  public static MarketCapClass fromLabel(String label) {
    if (label != null) {
      final String cleaned = label.trim();
      for (MarketCapClass value : values()) {
        if (value != UNKNOWN && value.label.equalsIgnoreCase(cleaned)) {
          return value;
        }
      }
    }
    return UNKNOWN;
  }
}
