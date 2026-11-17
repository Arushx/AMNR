package interface_adapter.search_stock;

/** Implemented by the search view so it hears when there is something new to display. */
public interface SearchStockViewModelListener {

  /** The search state was updated. */
  void stateChanged();
}
