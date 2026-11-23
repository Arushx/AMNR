package interface_adapter.search_stock;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the search view's current state and tells listening views when it changes (Observer).
 */
public class SearchStockViewModel {

  public static final String VIEW_NAME = "search stock";

  private final List<SearchStockViewModelListener> listeners = new ArrayList<>();
  private SearchStockState state = new SearchStockState();

  public SearchStockState getState() {
    return state;
  }

  public void setState(SearchStockState state) {
    this.state = state;
  }

  /**
   * Registers a view to be told about state changes.
   *
   * @param listener the view to notify
   */
  public void addListener(SearchStockViewModelListener listener) {
    listeners.add(listener);
  }

  /** Tells every listening view that the state changed, so it redraws. */
  public void notifyStateChanged() {
    for (SearchStockViewModelListener listener : listeners) {
      listener.stateChanged();
    }
  }
}
