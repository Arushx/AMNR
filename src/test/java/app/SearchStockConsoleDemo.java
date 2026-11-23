package app;

import data_access.AlphaVantageClient;
import data_access.AlphaVantageException;
import data_access.AlphaVantageParser;
import data_access.AlphaVantageStockDataAccessObject;
import data_access.FileStockListingDataAccessObject;
import interface_adapter.search_stock.SearchStockController;
import interface_adapter.search_stock.SearchStockPresenter;
import interface_adapter.search_stock.SearchStockState;
import interface_adapter.search_stock.SearchStockViewModel;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import org.json.JSONObject;
import use_case.search_stock.SearchStockInteractor;

/**
 * A developer tool for trying the Search Stock use case before the Swing view exists.
 *
 * <p>It builds the real engine (controller → interactor → DAO → presenter → view model) and
 * stands in for the view: it reads searches from the keyboard and prints the view model's state.
 * After a company-name search, type the number of a match to pick it.
 *
 * <ul>
 *   <li>With ALPHA_VANTAGE_API_KEY set: real data. Each search uses 2 of the 25 daily requests.
 *   <li>Without it: offline mode, answering every search with the saved IBM responses.
 * </ul>
 *
 * <p>Lives in src/test so it never ships with the app.
 */
public class SearchStockConsoleDemo {

  public static void main(String[] args) {
    final String apiKey = System.getenv("ALPHA_VANTAGE_API_KEY");
    final boolean online = apiKey != null && !apiKey.isBlank();
    final AlphaVantageClient client = online ? new AlphaVantageClient(apiKey) : new SavedDataClient();

    // Build the engine exactly like AppBuilder.addSearchStockUseCase() does.
    final AlphaVantageParser parser = new AlphaVantageParser();
    final AlphaVantageStockDataAccessObject dataAccess =
        new AlphaVantageStockDataAccessObject(client, parser);
    final FileStockListingDataAccessObject listings =
        new FileStockListingDataAccessObject(
            Path.of("data", "listing_status.csv"), client, parser);
    final SearchStockViewModel viewModel = new SearchStockViewModel();
    final SearchStockController controller =
        new SearchStockController(
            new SearchStockInteractor(
                listings, dataAccess, dataAccess, new SearchStockPresenter(viewModel)));

    // Stand-in for the view: when the view model changes, print what the screen would show.
    viewModel.addListener(() -> print(viewModel.getState()));

    System.out.println(online
        ? "ONLINE: real Alpha Vantage data (2 requests per search)."
        : "OFFLINE: no API key set, so every stock shown is the saved IBM data.");
    System.out.println("Type a ticker (AAPL) or company name (apple), a match number, or 'quit'.");

    final Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
    while (true) {
      System.out.print("\nsearch> ");
      if (!in.hasNextLine()) {
        return;
      }
      final String line = in.nextLine();
      if (line.trim().equalsIgnoreCase("quit")) {
        return;
      }
      // A number picks one of the matches shown by the last name search.
      final List<String> matchSymbols = viewModel.getState().getMatchSymbols();
      final String trimmed = line.trim();
      if (trimmed.matches("[0-9]+")) {
        final int choice = Integer.parseInt(trimmed);
        if (choice >= 1 && choice <= matchSymbols.size()) {
          controller.execute(matchSymbols.get(choice - 1));
          continue;
        }
      }
      controller.execute(line);
    }
  }

  private static void print(SearchStockState state) {
    if (state.getError() != null) {
      System.out.println("  ERROR: " + state.getError());
      return;
    }
    if (!state.getMatchLabels().isEmpty()) {
      System.out.println("  Did you mean:");
      for (int i = 0; i < state.getMatchLabels().size(); i++) {
        System.out.println("    " + (i + 1) + ". " + state.getMatchLabels().get(i));
      }
      System.out.println("  Type a number to pick one.");
      return;
    }
    System.out.println("  " + state.getSymbol() + "  " + state.getPriceText()
        + "  " + (state.isUp() ? "▲ " : "▼ ") + state.getChangeText()
        + "  (" + state.getAsOfText() + ")");

    final List<Double> closes = state.getChartCloses();
    final int n = closes.size();
    System.out.println("  Chart data: " + n + " days, "
        + state.getChartDates().get(0) + " to " + state.getChartDates().get(n - 1));
    final int from = Math.max(0, n - 5);
    for (int i = from; i < n; i++) {
      System.out.printf("    %s  close %.2f%n", state.getChartDates().get(i), closes.get(i));
    }
  }

  /** Answers every request with the saved IBM responses, so no requests are used. */
  private static class SavedDataClient extends AlphaVantageClient {

    SavedDataClient() {
      super("offline");
    }

    @Override
    public JSONObject fetchGlobalQuote(String symbol) throws AlphaVantageException {
      return load("global_quote_ibm.json");
    }

    @Override
    public JSONObject fetchDailyTimeSeries(String symbol) throws AlphaVantageException {
      return load("time_series_daily_ibm.json");
    }

    @Override
    public String fetchListingStatus() throws AlphaVantageException {
      return read("listing_status_sample.csv");
    }

    private static JSONObject load(String file) throws AlphaVantageException {
      return new JSONObject(read(file));
    }

    private static String read(String file) throws AlphaVantageException {
      try (InputStream stream =
          SearchStockConsoleDemo.class.getResourceAsStream("/alphavantage/" + file)) {
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new AlphaVantageException("Could not read saved data " + file, e);
      }
    }
  }
}
