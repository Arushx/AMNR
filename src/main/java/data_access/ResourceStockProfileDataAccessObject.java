package data_access;

import entity.MarketCapClass;
import entity.StockProfile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import use_case.StockDataUnavailableException;
import use_case.StockProfileDataAccessInterface;

/**
 * Provides stock profiles from a CSV file bundled with the app (on the classpath).
 *
 * <p>The file ({@code stock_profiles.csv}, about 6,800 US stocks) ships inside the app, so it costs
 * no API requests and works offline. It is read once and then kept in memory.
 *
 * <p>Expected header: {@code symbol,name,sector,industry,market_cap,description}.
 */
public class ResourceStockProfileDataAccessObject implements StockProfileDataAccessInterface {

  /** Where the real data lives on the classpath. */
  public static final String DEFAULT_RESOURCE = "/stock_profiles.csv";

  static final List<String> HEADER =
      List.of("symbol", "name", "sector", "industry", "market_cap", "description");

  private final String resourcePath;
  private List<StockProfile> cached;

  /**
   * Creates the data access object.
   *
   * @param resourcePath the classpath location of the CSV, e.g. {@link #DEFAULT_RESOURCE}
   */
  public ResourceStockProfileDataAccessObject(String resourcePath) {
    this.resourcePath = resourcePath;
  }

  @Override
  public List<StockProfile> getProfiles() throws StockDataUnavailableException {
    if (cached == null) {
      cached = Collections.unmodifiableList(parse(readResource()));
    }
    return cached;
  }

  /** Reads the whole resource as text. */
  private String readResource() throws StockDataUnavailableException {
    try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
      if (stream == null) {
        throw new StockDataUnavailableException("Stock profiles file not found: " + resourcePath);
      }
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new StockDataUnavailableException(
          "Could not read the stock profiles file: " + e.getMessage());
    }
  }

  /**
   * Turns the CSV text into profiles.
   *
   * @param csv the whole file
   * @return one profile per data line; blank lines are skipped
   * @throws StockDataUnavailableException if the header or a line is malformed
   */
  static List<StockProfile> parse(String csv) throws StockDataUnavailableException {
    final String[] lines = csv.split("\\R");
    if (lines.length == 0 || !CsvLineParser.parse(lines[0].trim()).equals(HEADER)) {
      throw new StockDataUnavailableException("Stock profiles file has an unexpected header");
    }
    final List<StockProfile> profiles = new ArrayList<>();
    for (int i = 1; i < lines.length; i++) {
      if (lines[i].isBlank()) {
        continue;
      }
      try {
        profiles.add(toProfile(CsvLineParser.parse(lines[i])));
      } catch (IllegalArgumentException e) {
        throw new StockDataUnavailableException(
            "Stock profiles file is malformed at line " + (i + 1) + ": " + e.getMessage());
      }
    }
    return profiles;
  }

  /** Builds a profile from one line's fields, in {@link #HEADER} order. */
  private static StockProfile toProfile(List<String> fields) {
    if (fields.size() != HEADER.size()) {
      throw new IllegalArgumentException(
          "expected " + HEADER.size() + " fields but found " + fields.size());
    }
    return new StockProfile(
        fields.get(0),
        fields.get(1),
        fields.get(2),
        fields.get(3),
        MarketCapClass.fromLabel(fields.get(4)),
        fields.get(5));
  }
}
