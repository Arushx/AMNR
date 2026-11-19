package interface_adapter;

/**
 * Tracks which view is on screen. Its state is the active view's name, matching a {@link
 * ViewModel#getViewName()}; presenters set it to switch views, and the view manager listens and
 * flips the card layout.
 */
public class ViewManagerModel extends ViewModel<String> {

  /** Creates the model with no active view. */
  public ViewManagerModel() {
    super("view manager", "");
  }
}
