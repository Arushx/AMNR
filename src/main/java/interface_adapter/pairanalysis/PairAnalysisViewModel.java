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

  private String pairText = "";
  private String correlationText = "";
  private String detailText = "";
  private String error;
  private String firstSymbolError;
  private String secondSymbolError;

  /**
   * Returns the stocks that were compared, e.g. "KO (Coca-Cola Co) &amp; PEP (PepsiCo Inc)".
   *
   * @return the text, or "" when there is no result
   */
  public String getPairText() {
    return pairText;
  }

  public void setPairText(String pairText) {
    this.pairText = pairText;
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
   * Returns the error about the request as a whole.
   *
   * @return the message, or null when there is no error
   */
  public String getError() {
    return error;
  }

  /**
   * Sets the error about the request as a whole.
   *
   * @param error the message, or null for none
   */
  public void setError(String error) {
    this.error = error;
  }

  /**
   * Returns the problem with the first symbol field.
   *
   * @return the message, or null when the field is fine
   */
  public String getFirstSymbolError() {
    return firstSymbolError;
  }

  /**
   * Sets the problem with the first symbol field.
   *
   * @param firstSymbolError the message, or null for none
   */
  public void setFirstSymbolError(String firstSymbolError) {
    this.firstSymbolError = firstSymbolError;
  }

  /**
   * Returns the problem with the second symbol field.
   *
   * @return the message, or null when the field is fine
   */
  public String getSecondSymbolError() {
    return secondSymbolError;
  }

  /**
   * Sets the problem with the second symbol field.
   *
   * @param secondSymbolError the message, or null for none
   */
  public void setSecondSymbolError(String secondSymbolError) {
    this.secondSymbolError = secondSymbolError;
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
