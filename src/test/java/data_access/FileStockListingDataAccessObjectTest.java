package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.StockListing;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.StockDataUnavailableException;

/** Uses a temporary folder and a fake client, so no real files or network are touched. */
class FileStockListingDataAccessObjectTest {

  private static final String CSV =
      "symbol,name,exchange,assetType,ipoDate,delistingDate,status\n"
          + "AAPL,Apple Inc,NASDAQ,Stock,1980-12-12,null,Active\n"
          + "IBM,International Business Machines Corp,NYSE,Stock,1962-01-02,null,Active\n";

  @TempDir Path tempDir;

  /** Counts downloads; returns the CSV above, or fails. */
  private static class FakeClient extends AlphaVantageClient {
    int downloads;
    boolean fail;

    FakeClient() {
      super("fake-key");
    }

    @Override
    public String fetchListingStatus() throws AlphaVantageException {
      downloads++;
      if (fail) {
        throw new AlphaVantageException("daily limit reached");
      }
      return CSV;
    }
  }

  @Test
  void downloadsAndSavesWhenFileIsMissing() throws Exception {
    Path file = tempDir.resolve("data").resolve("listing_status.csv");
    FakeClient client = new FakeClient();
    FileStockListingDataAccessObject dao =
        new FileStockListingDataAccessObject(file, client, new AlphaVantageParser());

    List<StockListing> listings = dao.getListings();

    assertEquals(2, listings.size());
    assertEquals(1, client.downloads);
    assertTrue(Files.exists(file));
    assertEquals(CSV, Files.readString(file, StandardCharsets.UTF_8));
  }

  @Test
  void readsSavedFileWithoutDownloading() throws Exception {
    Path file = tempDir.resolve("listing_status.csv");
    Files.writeString(file, CSV, StandardCharsets.UTF_8);
    FakeClient client = new FakeClient();
    FileStockListingDataAccessObject dao =
        new FileStockListingDataAccessObject(file, client, new AlphaVantageParser());

    assertEquals("AAPL", dao.getListings().get(0).getSymbol());
    assertEquals(0, client.downloads);
  }

  @Test
  void keepsListInMemoryAfterFirstLoad() throws Exception {
    Path file = tempDir.resolve("listing_status.csv");
    FakeClient client = new FakeClient();
    FileStockListingDataAccessObject dao =
        new FileStockListingDataAccessObject(file, client, new AlphaVantageParser());

    dao.getListings();
    Files.delete(file);
    dao.getListings();

    assertEquals(1, client.downloads);
  }

  @Test
  void downloadFailureBecomesStockDataUnavailable() {
    FakeClient client = new FakeClient();
    client.fail = true;
    FileStockListingDataAccessObject dao =
        new FileStockListingDataAccessObject(
            tempDir.resolve("listing_status.csv"), client, new AlphaVantageParser());

    assertThrows(StockDataUnavailableException.class, dao::getListings);
  }
}
