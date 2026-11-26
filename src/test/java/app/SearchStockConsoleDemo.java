package app;

import data_access.AlphaVantageClient;
import data_access.AlphaVantageException;
import data_access.AlphaVantageParser;
import data_access.AlphaVantageStockDataAccessObject;
import data_access.FileProfileVectorDataAccessObject;
import data_access.FileStockListingDataAccessObject;
import data_access.MiniLmModelFiles;
import data_access.OnnxTextEmbedder;
import data_access.ResourceStockProfileDataAccessObject;
import interface_adapter.search_stock.SearchStockController;
import interface_adapter.search_stock.SearchStockPresenter;
import interface_adapter.search_stock.SearchStockState;
import interface_adapter.search_stock.SearchStockViewModel;
import interface_adapter.semantic_search.SemanticSearchController;
import interface_adapter.semantic_search.SemanticSearchPresenter;
import interface_adapter.semantic_search.SemanticSearchState;
import interface_adapter.semantic_search.SemanticSearchViewModel;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.JSONObject;
import use_case.search_stock.SearchStockInteractor;
import use_case.semantic_search.KeywordProfileMatcher;
import use_case.semantic_search.SemanticProfileMatcher;
import use_case.semantic_search.SemanticSearchInteractor;
import use_case.semantic_search.StockRanker;

/**
 * A developer tool for trying Search Stock (Use Case 1) and Semantic Search (Use Case 7) before
 * their Swing views exist.
 *
 * <p>It builds the real engine (controller → interactor → DAO → presenter → view model) and
 * stands in for the view: it reads searches from the keyboard and prints the view model's state.
 * After a company-name search, type the number of a match to pick it. A sentence that matches no
 * name ("best selling oil stocks") gets semantic suggestions. Type {@code find <sentence>} for the
 * full Semantic Search results (10 stocks with industry and size); pick one by number to see it
 * through Search Stock.
 *
 * <ul>
 *   <li>With ALPHA_VANTAGE_API_KEY set: real data. Each search uses 2 of the 25 daily requests.
 *   <li>Without it: offline mode, answering every search with the saved IBM responses.
 * </ul>
 *
 * <p>Lives in src/test so it never ships with the app.
 */
public class SearchStockConsoleDemo {

  /** The tickers of whichever list was printed last, so a number can pick from it. */
  private static List<String> lastSymbols = new ArrayList<>();

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
    // Semantic search needs no API key: profiles ship with the app, the model runs locally.
    final StockRanker ranker =
        new StockRanker(
            new ResourceStockProfileDataAccessObject(
                ResourceStockProfileDataAccessObject.DEFAULT_RESOURCE),
            new SemanticProfileMatcher(
                new OnnxTextEmbedder(
                    new MiniLmModelFiles(Path.of("data", "models", "all-MiniLM-L6-v2"))),
                new FileProfileVectorDataAccessObject(Path.of("data", "profile_vectors.bin"))),
            new KeywordProfileMatcher());

    final SearchStockViewModel viewModel = new SearchStockViewModel();
    final SearchStockController controller =
        new SearchStockController(
            new SearchStockInteractor(
                listings, dataAccess, dataAccess, new SearchStockPresenter(viewModel), ranker));

    final SemanticSearchViewModel semanticViewModel = new SemanticSearchViewModel();
    final SemanticSearchController semanticController =
        new SemanticSearchController(
            new SemanticSearchInteractor(ranker, new SemanticSearchPresenter(semanticViewModel)));

    // Stand-ins for the views: when a view model changes, print what the screen would show.
    viewModel.addListener(() -> print(viewModel.getState()));
    semanticViewModel.addListener(() -> printSemantic(semanticViewModel.getState()));

    System.out.println(online
        ? "ONLINE: real Alpha Vantage data (2 requests per search)."
        : "OFFLINE: no API key set, so every stock shown is the saved IBM data.");
    System.out.println("Type a ticker (AAPL), a company name (apple), a sentence (oil stocks),");
    System.out.println("'find <sentence>' for full semantic results, a number to pick, or 'quit'.");
    System.out.println("(The first semantic search builds the search index: about 15 seconds.)");

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
      final String trimmed = line.trim();
      // A number picks from the last list shown; Search Stock then shows that stock.
      if (trimmed.matches("[0-9]+")) {
        final int choice = Integer.parseInt(trimmed);
        if (choice >= 1 && choice <= lastSymbols.size()) {
          controller.execute(lastSymbols.get(choice - 1));
          continue;
        }
      }
      if (trimmed.toLowerCase().startsWith("find ")) {
        semanticController.execute(trimmed.substring("find ".length()));
      } else {
        controller.execute(line);
      }
    }
  }

  private static void print(SearchStockState state) {
    if (state.getError() != null) {
      System.out.println("  ERROR: " + state.getError());
      return;
    }
    if (!state.getMatchLabels().isEmpty()) {
      lastSymbols = state.getMatchSymbols();
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

  private static void printSemantic(SemanticSearchState state) {
    if (state.getError() != null) {
      System.out.println("  ERROR: " + state.getError());
      return;
    }
    lastSymbols = state.getResultSymbols();
    System.out.println("  " + state.getSummary() + ":");
    for (int i = 0; i < state.getResultLabels().size(); i++) {
      System.out.println("    " + (i + 1) + ". " + state.getResultLabels().get(i));
    }
    System.out.println("  Type a number to see that stock.");
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
