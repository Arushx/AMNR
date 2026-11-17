package data_access;

import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Sends requests to the Alpha Vantage web API and returns the raw JSON.
 *
 * <p>This class only knows about HTTP and Alpha Vantage's error format. Turning the JSON into
 * entities is {@link AlphaVantageParser}'s job.
 */
public class AlphaVantageClient {

  private static final String BASE_URL = "https://www.alphavantage.co/query";

  private final String apiKey;
  private final OkHttpClient httpClient = new OkHttpClient();

  /**
   * Creates a client that authenticates with the given key.
   *
   * @param apiKey the Alpha Vantage API key, read from the environment by the caller
   * @throws IllegalArgumentException if the key is missing or blank
   */
  public AlphaVantageClient(String apiKey) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalArgumentException(
          "Alpha Vantage API key is not set (environment variable ALPHA_VANTAGE_API_KEY)");
    }
    this.apiKey = apiKey;
  }

  /**
   * Fetches the latest quote for a symbol (the GLOBAL_QUOTE function).
   *
   * @param symbol the ticker symbol, e.g. "IBM"
   * @return the response JSON
   * @throws AlphaVantageException if the request fails or Alpha Vantage reports an error
   */
  public JSONObject fetchGlobalQuote(String symbol) throws AlphaVantageException {
    return query("GLOBAL_QUOTE", "symbol", symbol);
  }

  /**
   * Fetches the last 100 trading days of prices for a symbol (TIME_SERIES_DAILY, compact).
   *
   * @param symbol the ticker symbol, e.g. "IBM"
   * @return the response JSON
   * @throws AlphaVantageException if the request fails or Alpha Vantage reports an error
   */
  public JSONObject fetchDailyTimeSeries(String symbol) throws AlphaVantageException {
    return query("TIME_SERIES_DAILY", "symbol", symbol);
  }

  /**
   * Fetches every actively traded US stock and ETF (the LISTING_STATUS function).
   *
   * <p>Unlike the other functions, this one answers with CSV text: a header line
   * {@code symbol,name,exchange,assetType,ipoDate,delistingDate,status}, then one line per stock.
   *
   * @return the CSV text
   * @throws AlphaVantageException if the request fails or Alpha Vantage reports an error
   */
  public String fetchListingStatus() throws AlphaVantageException {
    final String body = send(buildUrl("LISTING_STATUS", null, null));
    // Errors still come back as JSON, even from this CSV endpoint.
    if (body.trim().startsWith("{")) {
      parseAndCheck(body);
      throw new AlphaVantageException("Alpha Vantage returned JSON instead of the stock list");
    }
    return body;
  }

  /**
   * Sends one request and returns its JSON, after checking for every kind of failure.
   *
   * @param function the Alpha Vantage function name
   * @param paramName the name of the function's main parameter
   * @param paramValue the value of that parameter
   * @return the response JSON
   * @throws AlphaVantageException if the request fails or Alpha Vantage reports an error
   */
  private JSONObject query(String function, String paramName, String paramValue)
      throws AlphaVantageException {
    return parseAndCheck(send(buildUrl(function, paramName, paramValue)));
  }

  /** Builds the request URL; {@code paramName} may be null for functions with no parameter. */
  private HttpUrl buildUrl(String function, String paramName, String paramValue) {
    final HttpUrl.Builder url =
        HttpUrl.get(BASE_URL).newBuilder().addQueryParameter("function", function);
    if (paramName != null) {
      url.addQueryParameter(paramName, paramValue);
    }
    return url.addQueryParameter("apikey", apiKey).build();
  }

  /**
   * Sends a GET request and returns the response body as text.
   *
   * @param url the full request URL
   * @return the response body
   * @throws AlphaVantageException if the network fails, the status isn't 2xx, or there's no body
   */
  private String send(HttpUrl url) throws AlphaVantageException {
    final Request request = new Request.Builder().url(url).build();
    try (Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new AlphaVantageException(
            "Alpha Vantage request failed with HTTP status " + response.code());
      }
      final ResponseBody body = response.body();
      if (body == null) {
        throw new AlphaVantageException("Alpha Vantage returned an empty response");
      }
      return body.string();
    } catch (IOException e) {
      throw new AlphaVantageException("Could not reach Alpha Vantage: " + e.getMessage(), e);
    }
  }

  /**
   * Parses response text as JSON and checks it for Alpha Vantage's error replies.
   *
   * @param text the response body
   * @return the parsed JSON
   * @throws AlphaVantageException if it isn't JSON or is an error reply
   */
  private static JSONObject parseAndCheck(String text) throws AlphaVantageException {
    final JSONObject json;
    try {
      json = new JSONObject(text);
    } catch (JSONException e) {
      throw new AlphaVantageException("Alpha Vantage returned a response that isn't JSON", e);
    }
    checkForApiError(json);
    return json;
  }

  /**
   * Throws if the JSON is one of Alpha Vantage's error replies.
   *
   * <p>Alpha Vantage sends errors with HTTP 200, so a successful status isn't enough: the error is
   * a JSON object with an "Error Message", "Information" or "Note" key instead of data.
   *
   * @param json a parsed response
   * @throws AlphaVantageException if the response is an error reply
   */
  static void checkForApiError(JSONObject json) throws AlphaVantageException {
    for (String key : new String[] {"Error Message", "Information", "Note"}) {
      if (json.has(key)) {
        throw new AlphaVantageException(json.getString(key));
      }
    }
  }
}
