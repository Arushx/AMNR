package interface_adapter.search_stock;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import use_case.search_stock.SearchStockMatchesOutputData;
import use_case.search_stock.SearchStockOutputBoundary;
import use_case.search_stock.SearchStockOutputData;

/** Formats the search result for display and puts it in the view model. */
public class SearchStockPresenter implements SearchStockOutputBoundary {

  private static final DateTimeFormatter AS_OF_FORMAT =
      DateTimeFormatter.ofPattern("MMM d, uuuu", Locale.US);

  private final SearchStockViewModel viewModel;

  /**
   * Creates the presenter.
   *
   * @param viewModel where the formatted result goes
   */
  public SearchStockPresenter(SearchStockViewModel viewModel) {
    this.viewModel = viewModel;
  }

  @Override
  public void prepareSuccessView(SearchStockOutputData outputData) {
    final SearchStockState state = new SearchStockState();
    state.setSymbol(outputData.getSymbol());
    state.setPriceText(String.format(Locale.US, "$%,.2f", outputData.getPrice()));
    state.setChangeText(
        String.format(
            Locale.US,
            "%+.2f (%+.2f%%)",
            outputData.getChange(),
            outputData.getChangePercent()));
    state.setAsOfText("As of " + AS_OF_FORMAT.format(outputData.getLatestTradingDay()));
    state.setUp(outputData.getChange() >= 0);
    state.setChartDates(new ArrayList<>(outputData.getDates()));
    state.setChartCloses(new ArrayList<>(outputData.getCloses()));
    state.setError(null);

    viewModel.setState(state);
    viewModel.notifyStateChanged();
  }

  @Override
  public void prepareMatchesView(SearchStockMatchesOutputData outputData) {
    final List<String> labels = new ArrayList<>();
    for (int i = 0; i < outputData.getSymbols().size(); i++) {
      String label = outputData.getSymbols().get(i) + "  " + outputData.getNames().get(i);
      if (!outputData.getExchanges().get(i).isEmpty()) {
        label += "  (" + outputData.getExchanges().get(i) + ")";
      }
      labels.add(label);
    }
    final SearchStockState state = new SearchStockState();
    state.setMatchLabels(labels);
    state.setMatchSymbols(new ArrayList<>(outputData.getSymbols()));
    state.setError(null);

    viewModel.setState(state);
    viewModel.notifyStateChanged();
  }

  @Override
  public void prepareFailView(String errorMessage) {
    final SearchStockState state = viewModel.getState();
    state.setError(errorMessage);
    viewModel.notifyStateChanged();
  }
}
