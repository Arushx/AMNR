package interface_adapter;

/**
 * Tracks which view is on screen. The active view's name matches a {@link
 * ViewModel#getViewName()}; presenters set it to switch views, and the view manager listens and
 * flips the card layout.
 */
public class ViewManagerModel extends ViewModel {

  private String activeView = "";

  /** Creates the model with no active view. */
  public ViewManagerModel() {
    super("view manager");
  }

  /**
   * Returns the view on screen.
   *
   * @return the active view's name, or "" before any view is shown
   */
  public String getActiveView() {
    return activeView;
  }

  public void setActiveView(String activeView) {
    this.activeView = activeView;
  }
}
