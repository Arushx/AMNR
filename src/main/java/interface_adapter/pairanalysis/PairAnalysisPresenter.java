package interface_adapter.pairanalysis;

import java.util.Locale;
import use_case.pairanalysis.PairAnalysisOutputBoundary;
import use_case.pairanalysis.PairAnalysisOutputData;

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
    viewModel.setFirstTicker(outputData.getFirstTicker());
    viewModel.setSecondTicker(outputData.getSecondTicker());
    viewModel.setCorrelationText(String.format(Locale.US, "%.4f", outputData.getCorrelation()));
    viewModel.setDetailText(
        "Based on "
            + outputData.getSharedReturnCount()
            + " daily returns on trading days both stocks share");
    viewModel.setError(null);
    viewModel.firePropertyChanged();
  }

  @Override
  public void prepareFailView(String errorMessage) {
    // Clear the previous result so it isn't mistaken for the answer to the request that failed.
    viewModel.setCorrelationText("");
    viewModel.setDetailText("");
    viewModel.setError(errorMessage);
    viewModel.firePropertyChanged();
  }
}
