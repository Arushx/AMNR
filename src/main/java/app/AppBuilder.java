package app;

import data_access.AlphaVantageClient;
import data_access.AlphaVantageParser;
import data_access.AlphaVantageStockDataAccessObject;
import interface_adapter.search_stock.SearchStockController;
import interface_adapter.search_stock.SearchStockPresenter;
import interface_adapter.search_stock.SearchStockViewModel;
import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import use_case.search_stock.SearchStockInputBoundary;
import use_case.search_stock.SearchStockInteractor;

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

  private final CardLayout cardLayout = new CardLayout();
  private final JPanel cardPanel = new JPanel(cardLayout);

  private final SearchStockViewModel searchStockViewModel = new SearchStockViewModel();
  private SearchStockController searchStockController;

  /**
   * Builds the Search Stock engine: data access, presenter, interactor and controller.
   *
   * <p>Reads the Alpha Vantage key from the {@code ALPHA_VANTAGE_API_KEY} environment variable.
   * The search view (added separately) uses {@link #searchStockViewModel} to display results and
   * {@link #searchStockController} to start a search.
   *
   * @return this builder, so calls can be chained
   * @throws IllegalArgumentException if the API key environment variable is not set
   */
  public AppBuilder addSearchStockUseCase() {
    final AlphaVantageClient client =
        new AlphaVantageClient(System.getenv("ALPHA_VANTAGE_API_KEY"));
    final AlphaVantageStockDataAccessObject stockDataAccess =
        new AlphaVantageStockDataAccessObject(client, new AlphaVantageParser());

    final SearchStockPresenter presenter = new SearchStockPresenter(searchStockViewModel);
    final SearchStockInputBoundary interactor =
        new SearchStockInteractor(stockDataAccess, stockDataAccess, presenter);
    searchStockController = new SearchStockController(interactor);
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
