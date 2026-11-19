package data_access;

import java.io.IOException;
import java.time.LocalDate;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Sends requests to the Finnhub web API and returns the raw JSON.
 *
 * <p>This class only knows about HTTP and Finnhub's error format. Turning the JSON into entities is
 * {@link FinnhubNewsParser}'s job. The API key is sent in the {@code X-Finnhub-Token} header, not
 * in the URL, so it doesn't end up in logs.
 */
public class FinnhubClient {

  private static final String BASE_URL = "https://finnhub.io/api/v1";
  private static final String TOKEN_HEADER = "X-Finnhub-Token";
  private static final int HTTP_UNAUTHORIZED = 401;
  private static final int HTTP_FORBIDDEN = 403;
  private static final int HTTP_TOO_MANY_REQUESTS = 429;

  private final String apiKey;
  private final OkHttpClient httpClient = new OkHttpClient();

  /**
   * Creates a client that authenticates with the given key.
   *
   * @param apiKey the Finnhub API key, read from the environment by the caller
   * @throws IllegalArgumentException if the key is missing or blank
   */
  public FinnhubClient(String apiKey) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalArgumentException(
          "Finnhub API key is not set (environment variable FINNHUB_API_KEY)");
    }
    this.apiKey = apiKey;
  }

  /**
   * Fetches the news about a company published from {@code from} to {@code to} (the company-news
   * endpoint). The free tier covers about one year back.
   *
   * @param symbol the ticker symbol, e.g. "NVDA"
   * @param from the first day to include
   * @param to the last day to include
   * @return the response, a JSON array of articles
   * @throws FinnhubException if the request fails or Finnhub reports an error
   */
  public JSONArray fetchCompanyNews(String symbol, LocalDate from, LocalDate to)
      throws FinnhubException {
    final HttpUrl url =
        HttpUrl.get(BASE_URL + "/company-news")
            .newBuilder()
            .addQueryParameter("symbol", symbol)
            .addQueryParameter("from", from.toString())
            .addQueryParameter("to", to.toString())
            .build();
    final Request request = new Request.Builder().url(url).header(TOKEN_HEADER, apiKey).build();

    try (Response response = httpClient.newCall(request).execute()) {
      final ResponseBody body = response.body();
      final String text = body == null ? "" : body.string();
      checkForError(response.code(), text);
      final Object json = new JSONTokener(text).nextValue();
      if (!(json instanceof JSONArray)) {
        throw new FinnhubException("Finnhub returned an unexpected response for company news");
      }
      return (JSONArray) json;
    } catch (IOException e) {
      throw new FinnhubException("Could not reach Finnhub: " + e.getMessage(), e);
    } catch (JSONException e) {
      throw new FinnhubException("Finnhub returned a response that isn't JSON", e);
    }
  }

  /**
   * Throws if a response is one of Finnhub's error replies.
   *
   * <p>Finnhub reports errors with an HTTP error status and a body like {@code {"error": "Invalid
   * API key."}}. Some errors also come with status 200, so the body is checked too.
   *
   * @param status the HTTP status code
   * @param body the response body; may be empty
   * @throws FinnhubException if the response is an error reply
   */
  static void checkForError(int status, String body) throws FinnhubException {
    final String message = errorMessageIn(body);
    if (status == HTTP_TOO_MANY_REQUESTS) {
      throw new FinnhubException("Finnhub rate limit reached, try again in a minute");
    }
    if (status == HTTP_UNAUTHORIZED || status == HTTP_FORBIDDEN) {
      throw new FinnhubException(
          message != null ? message : "Finnhub refused the API key (HTTP " + status + ")");
    }
    if (status < 200 || status >= 300) {
      throw new FinnhubException(
          message != null ? message : "Finnhub request failed with HTTP status " + status);
    }
    if (message != null) {
      throw new FinnhubException(message);
    }
  }

  private static String errorMessageIn(String body) {
    if (body == null || !body.trim().startsWith("{")) {
      return null;
    }
    try {
      final JSONObject json = new JSONObject(body);
      return json.has("error") ? json.getString("error") : null;
    } catch (JSONException e) {
      return null;
    }
  }
}
