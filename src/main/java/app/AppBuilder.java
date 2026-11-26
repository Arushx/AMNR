package app;

import data_access.AlphaVantageClient;
import data_access.AlphaVantageParser;
import data_access.AlphaVantageStockDataAccessObject;
import data_access.FileProfileVectorDataAccessObject;
import data_access.FileStockListingDataAccessObject;
import data_access.MiniLmModelFiles;
import data_access.OnnxTextEmbedder;
import data_access.ResourceStockProfileDataAccessObject;
import interface_adapter.pairanalysis.PairAnalysisController;
import interface_adapter.pairanalysis.PairAnalysisPresenter;
import interface_adapter.pairanalysis.PairAnalysisViewModel;
import interface_adapter.search_stock.SearchStockController;
import interface_adapter.search_stock.SearchStockPresenter;
import interface_adapter.search_stock.SearchStockViewModel;
import interface_adapter.semantic_search.SemanticSearchController;
import interface_adapter.semantic_search.SemanticSearchPresenter;
import interface_adapter.semantic_search.SemanticSearchViewModel;
import java.awt.CardLayout;
import java.nio.file.Path;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import use_case.pairanalysis.PairAnalysisInputBoundary;
import use_case.pairanalysis.PairAnalysisInteractor;
import use_case.search_stock.SearchStockInputBoundary;
import use_case.search_stock.SearchStockInteractor;
import use_case.semantic_search.KeywordProfileMatcher;
import use_case.semantic_search.SemanticProfileMatcher;
import use_case.semantic_search.SemanticSearchInputBoundary;
import use_case.semantic_search.SemanticSearchInteractor;
import use_case.semantic_search.StockRanker;
import view.PairAnalysisView;

/**
 * Assembles the Clean Architecture engine for each use case and wires it into one window.
 *
 * <p>This is the "Main component": the only place that knows about concrete classes from every
 * layer. Each use case lead adds an {@code addXView()} method that registers their view in
 * {@link #cardPanel}, and an {@code addXUseCase()} method that creates the data access object,
 * presenter, interactor and controller and connects them. Calls are chained in {@link Main}.
 */
public class AppBuilder {

  private static final String TITLE = "AMNR"; // TODO Decide if this is our final
  private static final Path LISTING_FILE = Path.of("data", "listing_status.csv");
  private static final Path MODEL_FOLDER = Path.of("data", "models", "all-MiniLM-L6-v2");
  private static final Path VECTOR_FILE = Path.of("data", "profile_vectors.bin");

  private final CardLayout cardLayout = new CardLayout();
  private final JPanel cardPanel = new JPanel(cardLayout);

  private final SearchStockViewModel searchStockViewModel = new SearchStockViewModel();
  private SearchStockController searchStockController;

  private final SemanticSearchViewModel semanticSearchViewModel = new SemanticSearchViewModel();
  private SemanticSearchController semanticSearchController;
  private StockRanker stockRanker;

  private final PairAnalysisViewModel pairAnalysisViewModel = new PairAnalysisViewModel();
  private PairAnalysisController pairAnalysisController;

  /**
   * Builds the Search Stock engine: data access, presenter, interactor and controller.
   *
   * <p>Reads the Alpha Vantage key from the {@code ALPHA_VANTAGE_API_KEY} environment variable. The
   * list of every stock is kept in {@code data/listing_status.csv}, downloaded on first use.
   * The search view (added separately) uses {@link #searchStockViewModel} to display results and
   * {@link #searchStockController} to start a search.
   *
   * @return this builder, so calls can be chained
   * @throws IllegalArgumentException if the API key environment variable is not set
   */
  public AppBuilder addSearchStockUseCase() {
    final AlphaVantageClient client =
        new AlphaVantageClient(System.getenv("ALPHA_VANTAGE_API_KEY"));
    final AlphaVantageParser parser = new AlphaVantageParser();
    final AlphaVantageStockDataAccessObject stockDataAccess =
        new AlphaVantageStockDataAccessObject(client, parser);
    final FileStockListingDataAccessObject listingDataAccess =
        new FileStockListingDataAccessObject(LISTING_FILE, client, parser);

    final SearchStockPresenter presenter = new SearchStockPresenter(searchStockViewModel);
    final SearchStockInputBoundary interactor =
        new SearchStockInteractor(
            listingDataAccess, stockDataAccess, stockDataAccess, presenter, stockRanker());
    searchStockController = new SearchStockController(interactor);
    return this;
  }

  /**
   * Builds the Semantic Search engine: find stocks from a sentence like "best selling oil stocks".
   *
   * <p>Needs no API key. Stock profiles ship with the app; the embedding model is downloaded once
   * into {@code data/models/}, and profile embeddings are saved in
   * {@code data/profile_vectors.bin}.
   * The same ranker also gives Search Stock its suggestions when nothing matches by name.
   *
   * @return this builder, so calls can be chained
   */
  public AppBuilder addSemanticSearchUseCase() {
    final SemanticSearchPresenter presenter = new SemanticSearchPresenter(semanticSearchViewModel);
    final SemanticSearchInputBoundary interactor =
        new SemanticSearchInteractor(stockRanker(), presenter);
    semanticSearchController = new SemanticSearchController(interactor);
    return this;
  }

  /** Creates the shared semantic ranker on first use, so both use cases use one copy. */
  private StockRanker stockRanker() {
    if (stockRanker == null) {
      final OnnxTextEmbedder embedder = new OnnxTextEmbedder(new MiniLmModelFiles(MODEL_FOLDER));
      stockRanker =
          new StockRanker(
              new ResourceStockProfileDataAccessObject(
                  ResourceStockProfileDataAccessObject.DEFAULT_RESOURCE),
              new SemanticProfileMatcher(
                  embedder, new FileProfileVectorDataAccessObject(VECTOR_FILE)),
              new KeywordProfileMatcher());
    }
    return stockRanker;
  }

  /**
   * Builds the Pair Analysis engine: data access, presenter, interactor and controller.
   *
   * <p>Reads the Alpha Vantage key from the {@code ALPHA_VANTAGE_API_KEY} environment variable.
   * Symbols are checked against the stock list in {@code data/listing_status.csv} (downloaded on
   * first use) before any prices are requested.
   *
   * @return this builder, so calls can be chained
   * @throws IllegalArgumentException if the API key environment variable is not set
   */
  public AppBuilder addPairAnalysisUseCase() {
    final AlphaVantageClient client =
        new AlphaVantageClient(System.getenv("ALPHA_VANTAGE_API_KEY"));
    final AlphaVantageParser parser = new AlphaVantageParser();
    final AlphaVantageStockDataAccessObject stockDataAccess =
        new AlphaVantageStockDataAccessObject(client, parser);
    final FileStockListingDataAccessObject listingDataAccess =
        new FileStockListingDataAccessObject(LISTING_FILE, client, parser);

    final PairAnalysisPresenter presenter = new PairAnalysisPresenter(pairAnalysisViewModel);
    final PairAnalysisInputBoundary interactor =
        new PairAnalysisInteractor(listingDataAccess, stockDataAccess, presenter);
    pairAnalysisController = new PairAnalysisController(interactor);
    return this;
  }

  /**
   * Adds the Pair Analysis view to the window. Call {@link #addPairAnalysisUseCase()} first, since
   * the view needs its controller.
   *
   * @return this builder, so calls can be chained
   * @throws IllegalStateException if the use case has not been added yet
   */
  public AppBuilder addPairAnalysisView() {
    if (pairAnalysisController == null) {
      throw new IllegalStateException("call addPairAnalysisUseCase() before addPairAnalysisView()");
    }
    final PairAnalysisView view =
        new PairAnalysisView(pairAnalysisController, pairAnalysisViewModel);
    cardPanel.add(view, view.getViewName());
    return this;
  }

  /**
   * Creates the application window containing every view added so far.
   *
   * @return the configured, not yet visible, main window
   */
  public JFrame build() {
    if (cardPanel.getComponentCount() == 0) {
      // Placeholder until the first view is added; remove once a real view exists.
      cardPanel.add(new JLabel(TITLE + " - no views yet", SwingConstants.CENTER), "placeholder");
    }
    final JFrame application = new JFrame(TITLE);
    application.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    application.add(cardPanel);
    return application;
  }
}
