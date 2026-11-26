package interface_adapter.semantic_search;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the semantic search view's current state and tells listening views when it changes
 * (Observer).
 */
public class SemanticSearchViewModel {

  public static final String VIEW_NAME = "semantic search";

  private final List<SemanticSearchViewModelListener> listeners = new ArrayList<>();
  private SemanticSearchState state = new SemanticSearchState();

  public SemanticSearchState getState() {
    return state;
  }

  public void setState(SemanticSearchState state) {
    this.state = state;
  }

  /**
   * Registers a view to be told about state changes.
   *
   * @param listener the view to notify
   */
  public void addListener(SemanticSearchViewModelListener listener) {
    listeners.add(listener);
  }

  /** Tells every listening view that the state changed, so it redraws. */
  public void notifyStateChanged() {
    for (SemanticSearchViewModelListener listener : listeners) {
      listener.stateChanged();
    }
  }
}
