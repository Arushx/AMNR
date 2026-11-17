package data_access;

import entity.PriceHistory;
import entity.PricePoint;
import entity.Quote;
import entity.StockListing;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Turns Alpha Vantage JSON responses into entities.
 *
 * <p>All of Alpha Vantage's key names live here, so if the API's format changes, only this class
 * changes. It does no network access, which makes it easy to test with saved responses.
 */
public class AlphaVantageParser {

  private static final String GLOBAL_QUOTE = "Global Quote";
  private static final String QUOTE_SYMBOL = "01. symbol";
  private static final String QUOTE_PRICE = "05. price";
  private static final String QUOTE_DAY = "07. latest trading day";
  private static final String QUOTE_CHANGE = "09. change";
  private static final String QUOTE_CHANGE_PERCENT = "10. change percent";

  private static final String META_DATA = "Meta Data";
  private static final String META_SYMBOL = "2. Symbol";
  private static final String DAILY_SERIES = "Time Series (Daily)";
  private static final String OPEN = "1. open";
  private static final String HIGH = "2. high";
  private static final String LOW = "3. low";
  private static final String CLOSE = "4. close";
  private static final String VOLUME = "5. volume";

  private static final String LISTING_HEADER = "symbol,name,exchange";

  /**
   * Parses a GLOBAL_QUOTE response.
   *
   * @param json the response from {@link AlphaVantageClient#fetchGlobalQuote(String)}
   * @return the quote
   * @throws AlphaVantageException if no quote was found or the format is unexpected
   */
  public Quote parseQuote(JSONObject json) throws AlphaVantageException {
    final JSONObject quote = json.optJSONObject(GLOBAL_QUOTE);
    // Alpha Vantage answers an unknown symbol with an empty "Global Quote" object.
    if (quote == null || quote.isEmpty()) {
      throw new AlphaVantageException("No quote found for that symbol");
    }
    try {
      final String changePercent = quote.getString(QUOTE_CHANGE_PERCENT).replace("%", "");
      return new Quote(
          quote.getString(QUOTE_SYMBOL),
          Double.parseDouble(quote.getString(QUOTE_PRICE)),
          Double.parseDouble(quote.getString(QUOTE_CHANGE)),
          Double.parseDouble(changePercent),
          LocalDate.parse(quote.getString(QUOTE_DAY)));
    } catch (JSONException | IllegalArgumentException | DateTimeParseException e) {
      throw new AlphaVantageException("Unexpected quote format from Alpha Vantage", e);
    }
  }

  /**
   * Parses a TIME_SERIES_DAILY response.
   *
   * @param json the response from {@link AlphaVantageClient#fetchDailyTimeSeries(String)}
   * @return the price history, oldest day first
   * @throws AlphaVantageException if the format is unexpected
   */
  public PriceHistory parseDailyHistory(JSONObject json) throws AlphaVantageException {
    try {
      final String symbol = json.getJSONObject(META_DATA).getString(META_SYMBOL);
      final JSONObject series = json.getJSONObject(DAILY_SERIES);

      final List<PricePoint> points = new ArrayList<>();
      for (String date : series.keySet()) {
        final JSONObject day = series.getJSONObject(date);
        points.add(
            new PricePoint(
                LocalDate.parse(date),
                Double.parseDouble(day.getString(OPEN)),
                Double.parseDouble(day.getString(HIGH)),
                Double.parseDouble(day.getString(LOW)),
                Double.parseDouble(day.getString(CLOSE)),
                Long.parseLong(day.getString(VOLUME))));
      }
      // PriceHistory sorts the points, since JSON objects don't keep the dates in order.
      return new PriceHistory(symbol, points);
    } catch (JSONException | IllegalArgumentException | DateTimeParseException e) {
      throw new AlphaVantageException("Unexpected time series format from Alpha Vantage", e);
    }
  }

  /**
   * Parses the LISTING_STATUS CSV into listings.
   *
   * <p>Each line after the header is {@code symbol,name,exchange,assetType,...}. Lines without a
   * symbol, or with too few columns, are skipped.
   *
   * @param csv the text from {@link AlphaVantageClient#fetchListingStatus()}
   * @return every listing in the file
   * @throws AlphaVantageException if the text doesn't start with the expected header
   */
  public List<StockListing> parseListings(String csv) throws AlphaVantageException {
    final String[] lines = csv.split("\\R");
    if (lines.length == 0 || !lines[0].startsWith(LISTING_HEADER)) {
      throw new AlphaVantageException("Unexpected stock list format from Alpha Vantage");
    }
    final List<StockListing> listings = new ArrayList<>();
    for (int i = 1; i < lines.length; i++) {
      final String[] columns = lines[i].split(",", -1);
      if (columns.length >= 3 && !columns[0].isBlank()) {
        listings.add(new StockListing(columns[0], columns[1], columns[2]));
      }
    }
    return listings;
  }
}
