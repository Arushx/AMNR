package interface_adapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Holds what one view displays and notifies the view when it changes.
 *
 * <p>Subclasses add the displayed fields. Presenters set those fields and then call {@link
 * #firePropertyChange()}; views register with {@link #addPropertyChangeListener} and redraw from
 * the view model's getters. Events carry the view model as the new value and a null old value, so
 * they fire even when nothing visibly changed (e.g. the same error twice in a row).
 */
public abstract class ViewModel {

  /** Property name used by {@link #firePropertyChange()}. */
  public static final String STATE_PROPERTY = "state";

  private final String viewName;
  private final PropertyChangeSupport support = new PropertyChangeSupport(this);

  /**
   * Creates the view model.
   *
   * @param viewName the name the view is registered under in the card layout
   */
  protected ViewModel(String viewName) {
    this.viewName = viewName;
  }

  public String getViewName() {
    return viewName;
  }

  /** Tells listeners the fields changed, under the property name {@value #STATE_PROPERTY}. */
  public void firePropertyChange() {
    firePropertyChange(STATE_PROPERTY);
  }

  /**
   * Tells listeners the fields changed, under a custom property name.
   *
   * @param propertyName lets a view react differently to different kinds of change
   */
  public void firePropertyChange(String propertyName) {
    support.firePropertyChange(propertyName, null, this);
  }

  /**
   * Registers a listener, usually the view.
   *
   * @param listener called on every fired change
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }
}
