package interface_adapter.semantic_search;

/** Implemented by the semantic search view so it hears when there is something new to display. */
public interface SemanticSearchViewModelListener {

  /** The semantic search state was updated. */
  void stateChanged();
}
