package interface_adapter.pairanalysis;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Stores the information the pair analysis view needs to display.
 *
 * <p>The presenter writes ready-to-display strings here and then calls {@link
 * #firePropertyChanged()}; the view registers with {@link #addPropertyChangeListener} and redraws
 * from the getters when alerted. The view does no formatting of its own.
 */
public class PairAnalysisViewModel {

  public static final String VIEW_NAME = "pair analysis";

  private final PropertyChangeSupport support = new PropertyChangeSupport(this);

  private String firstTicker = "";
  private String secondTicker = "";
  private String correlationText = "";
  private String detailText = "";
  private String error;

  public String getFirstTicker() {
    return firstTicker;
  }

  public void setFirstTicker(String firstTicker) {
    this.firstTicker = firstTicker;
  }

  public String getSecondTicker() {
    return secondTicker;
  }

  public void setSecondTicker(String secondTicker) {
    this.secondTicker = secondTicker;
  }

  public String getCorrelationText() {
    return correlationText;
  }

  public void setCorrelationText(String correlationText) {
    this.correlationText = correlationText;
  }

  public String getDetailText() {
    return detailText;
  }

  public void setDetailText(String detailText) {
    this.detailText = detailText;
  }

  /**
   * Returns the error to show.
   *
   * @return the message, or null when there is no error
   */
  public String getError() {
    return error;
  }

  /**
   * Sets the error to show.
   *
   * @param error the message, or null for none
   */
  public void setError(String error) {
    this.error = error;
  }

  /**
   * Alerts listening views that the stored information changed. The old value is null so the
   * event fires even if nothing visibly differs (e.g. the same error twice in a row).
   */
  public void firePropertyChanged() {
    support.firePropertyChange(VIEW_NAME, null, this);
  }

  /**
   * Registers a view to be alerted when the stored information changes.
   *
   * @param listener usually the view
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    support.addPropertyChangeListener(listener);
  }
}
