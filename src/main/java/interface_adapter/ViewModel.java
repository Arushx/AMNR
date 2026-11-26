package interface_adapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Holds the state one view displays and notifies the view when it changes.
 *
 * <p>Presenters call {@link #setState} and then {@link #firePropertyChange()}; views register
 * with {@link #addPropertyChangeListener} and redraw from the event's new value. Events are sent
 * with a null old value so they fire even when the state is equal to the previous one (e.g. the
 * same error twice in a row).
 *
 * @param <T> the state type; state objects are replaced, never modified
 */
public class ViewModel<T> {

  /** Property name used by {@link #firePropertyChange()}. */
  public static final String STATE_PROPERTY = "state";

  private final String viewName;
  private final PropertyChangeSupport support = new PropertyChangeSupport(this);
  private T state;

  /**
   * Creates the view model.
   *
   * @param viewName the name the view is registered under in the card layout
   * @param initialState the state before anything happens
   */
  public ViewModel(String viewName, T initialState) {
    this.viewName = viewName;
    this.state = initialState;
  }

  public String getViewName() {
    return viewName;
  }

  public T getState() {
    return state;
  }

  /**
   * Replaces the state. Listeners are not told until {@link #firePropertyChange()} is called.
   *
   * @param state the new state
   */
  public void setState(T state) {
    this.state = state;
  }

  /** Tells listeners the state changed, under the property name {@value #STATE_PROPERTY}. */
  public void firePropertyChange() {
    firePropertyChange(STATE_PROPERTY);
  }

  /**
   * Tells listeners the state changed, under a custom property name.
   *
   * @param propertyName lets a view react differently to different kinds of change
   */
  public void firePropertyChange(String propertyName) {
    support.firePropertyChange(propertyName, null, state);
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
