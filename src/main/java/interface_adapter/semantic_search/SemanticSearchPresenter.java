package interface_adapter.semantic_search;

import java.util.ArrayList;
import java.util.List;
import use_case.semantic_search.SemanticSearchOutputBoundary;
import use_case.semantic_search.SemanticSearchOutputData;
import use_case.semantic_search.SortOrder;

/** Formats semantic search results as text for the view and updates the view model. */
public class SemanticSearchPresenter implements SemanticSearchOutputBoundary {

  private static final String SEPARATOR = "  ·  ";

  private final SemanticSearchViewModel viewModel;

  /**
   * Creates the presenter.
   *
   * @param viewModel the view model to update
   */
  public SemanticSearchPresenter(SemanticSearchViewModel viewModel) {
    this.viewModel = viewModel;
  }

  @Override
  public void prepareSuccessView(SemanticSearchOutputData outputData) {
    final List<String> labels = new ArrayList<>();
    for (int i = 0; i < outputData.getSymbols().size(); i++) {
      final StringBuilder label =
          new StringBuilder(outputData.getSymbols().get(i) + "  " + outputData.getNames().get(i));
      if (!outputData.getIndustries().get(i).isEmpty()) {
        label.append(SEPARATOR).append(outputData.getIndustries().get(i));
      }
      if (!outputData.getSizes().get(i).isEmpty()) {
        label.append(SEPARATOR).append(outputData.getSizes().get(i));
      }
      labels.add(label.toString());
    }

    final SemanticSearchState state = new SemanticSearchState();
    state.setSummary(summary(outputData));
    state.setResultLabels(labels);
    state.setResultSymbols(new ArrayList<>(outputData.getSymbols()));
    state.setError(null);
    viewModel.setState(state);
    viewModel.notifyStateChanged();
  }

  @Override
  public void prepareFailView(String errorMessage) {
    final SemanticSearchState state = viewModel.getState();
    state.setError(errorMessage);
    viewModel.notifyStateChanged();
  }

  /** E.g. "Stocks about \"oil\", biggest companies first". */
  private static String summary(SemanticSearchOutputData outputData) {
    String text = "Stocks about \"" + outputData.getTopic() + "\"";
    if (outputData.getSortOrder() == SortOrder.LARGEST) {
      text += ", biggest companies first";
    } else if (outputData.getSortOrder() == SortOrder.SMALLEST) {
      text += ", smallest companies first";
    }
    if (outputData.getFallbackReason() != null) {
      text += " (keyword matching: " + outputData.getFallbackReason() + ")";
    }
    return text;
  }
}
