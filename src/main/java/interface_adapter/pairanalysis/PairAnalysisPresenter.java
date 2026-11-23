package interface_adapter.pairanalysis;

import java.util.Locale;
import use_case.pairanalysis.PairAnalysisOutputBoundary;
import use_case.pairanalysis.PairAnalysisOutputData;
import use_case.pairanalysis.PairAnalysisUnknownSymbolOutputData;

/**
 * Turns the pair analysis output data into strings to display and updates the view model with
 * them.
 */
public class PairAnalysisPresenter implements PairAnalysisOutputBoundary {

  private final PairAnalysisViewModel viewModel;

  /**
   * Creates the presenter.
   *
   * @param viewModel where the display information goes
   */
  public PairAnalysisPresenter(PairAnalysisViewModel viewModel) {
    this.viewModel = viewModel;
  }

  @Override
  public void prepareSuccessView(PairAnalysisOutputData outputData) {
    viewModel.setPairText(
        describe(outputData.getFirstTicker(), outputData.getFirstName())
            + " & "
            + describe(outputData.getSecondTicker(), outputData.getSecondName()));
    viewModel.setCorrelationText(String.format(Locale.US, "%.4f", outputData.getCorrelation()));
    viewModel.setDetailText(
        "Based on "
            + outputData.getSharedReturnCount()
            + " daily returns on trading days both stocks share");
    viewModel.setError(null);
    viewModel.setFirstSymbolError(null);
    viewModel.setSecondSymbolError(null);
    viewModel.firePropertyChanged();
  }

  @Override
  public void prepareUnknownSymbolView(PairAnalysisUnknownSymbolOutputData outputData) {
    clearResult();
    viewModel.setError(null);
    viewModel.setFirstSymbolError(
        outputData.isFirstUnknown() ? unknownSymbolMessage(outputData.getFirstSymbol()) : null);
    viewModel.setSecondSymbolError(
        outputData.isSecondUnknown() ? unknownSymbolMessage(outputData.getSecondSymbol()) : null);
    viewModel.firePropertyChanged();
  }

  @Override
  public void prepareFailView(String errorMessage) {
    clearResult();
    viewModel.setError(errorMessage);
    viewModel.setFirstSymbolError(null);
    viewModel.setSecondSymbolError(null);
    viewModel.firePropertyChanged();
  }

  /** Clears the previous result so it isn't mistaken for the answer to a request that failed. */
  private void clearResult() {
    viewModel.setPairText("");
    viewModel.setCorrelationText("");
    viewModel.setDetailText("");
  }

  /** Formats one stock as "KO (Coca-Cola Co)", or just "KO" if the name is unknown. */
  private static String describe(String ticker, String name) {
    if (name == null || name.isEmpty()) {
      return ticker;
    }
    return ticker + " (" + name + ")";
  }

  private static String unknownSymbolMessage(String symbol) {
    return "No stock with symbol \"" + symbol + "\"";
  }
}
