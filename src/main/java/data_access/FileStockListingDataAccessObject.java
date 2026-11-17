package data_access;

import entity.StockListing;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import use_case.StockDataUnavailableException;
import use_case.StockListingDataAccessInterface;

/**
 * Provides the list of every stock from a local file, downloading it from Alpha Vantage only if
 * the file doesn't exist yet.
 *
 * <p>This is our data persistence for searching: the list (about 14,500 stocks) is downloaded
 * once, saved, and read from disk afterwards, so name searches cost no API requests. After the
 * first load it is also kept in memory.
 */
public class FileStockListingDataAccessObject implements StockListingDataAccessInterface {

  private final Path file;
  private final AlphaVantageClient client;
  private final AlphaVantageParser parser;
  private List<StockListing> cached;

  /**
   * Creates the data access object.
   *
   * @param file where the list is saved, e.g. data/listing_status.csv
   * @param client used to download the list if the file is missing
   * @param parser turns the CSV text into listings
   */
  public FileStockListingDataAccessObject(
      Path file, AlphaVantageClient client, AlphaVantageParser parser) {
    this.file = file;
    this.client = client;
    this.parser = parser;
  }

  @Override
  public List<StockListing> getListings() throws StockDataUnavailableException {
    if (cached == null) {
      try {
        cached = parser.parseListings(loadCsv());
      } catch (AlphaVantageException e) {
        throw new StockDataUnavailableException("Could not load the stock list: " + e.getMessage());
      } catch (IOException e) {
        throw new StockDataUnavailableException(
            "Could not read or save the stock list file " + file + ": " + e.getMessage());
      }
    }
    return cached;
  }

  /** Reads the saved file, or downloads the list and saves it first. */
  private String loadCsv() throws AlphaVantageException, IOException {
    if (Files.exists(file)) {
      return Files.readString(file, StandardCharsets.UTF_8);
    }
    final String csv = client.fetchListingStatus();
    if (file.getParent() != null) {
      Files.createDirectories(file.getParent());
    }
    Files.writeString(file, csv, StandardCharsets.UTF_8);
    return csv;
  }
}
