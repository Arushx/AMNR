package interface_adapter.search_stock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Everything the search view needs to display, already formatted.
 *
 * <p>After a search, exactly one of these is shown:
 *
 * <ul>
 *   <li>a stock: the quote fields and chart data are filled;
 *   <li>name matches to pick from: {@code matchLabels} and {@code matchSymbols} are filled;
 *   <li>an error: {@code error} holds a message.
 * </ul>
 */
public class SearchStockState {

  private String symbol = "";
  private String priceText = "";
  private String changeText = "";
  private String asOfText = "";
  private boolean up = true;
  private List<LocalDate> chartDates = new ArrayList<>();
  private List<Double> chartCloses = new ArrayList<>();
  private List<String> matchLabels = new ArrayList<>();
  private List<String> matchSymbols = new ArrayList<>();
  private String error;

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public String getPriceText() {
    return priceText;
  }

  public void setPriceText(String priceText) {
    this.priceText = priceText;
  }

  public String getChangeText() {
    return changeText;
  }

  public void setChangeText(String changeText) {
    this.changeText = changeText;
  }

  public String getAsOfText() {
    return asOfText;
  }

  public void setAsOfText(String asOfText) {
    this.asOfText = asOfText;
  }

  /**
   * Returns whether the price rose (or stayed the same) since the previous close, so the view can
   * pick a colour.
   *
   * @return true if the change is zero or positive
   */
  public boolean isUp() {
    return up;
  }

  public void setUp(boolean up) {
    this.up = up;
  }

  public List<LocalDate> getChartDates() {
    return chartDates;
  }

  public void setChartDates(List<LocalDate> chartDates) {
    this.chartDates = chartDates;
  }

  public List<Double> getChartCloses() {
    return chartCloses;
  }

  public void setChartCloses(List<Double> chartCloses) {
    this.chartCloses = chartCloses;
  }

  /**
   * Returns the name matches to show the user, e.g. "AAPL  Apple Inc  (NASDAQ)", best first.
   *
   * @return the labels; empty unless the last search was a name search
   */
  public List<String> getMatchLabels() {
    return matchLabels;
  }

  public void setMatchLabels(List<String> matchLabels) {
    this.matchLabels = matchLabels;
  }

  /**
   * Returns the ticker behind each match label, in the same order. When the user picks label
   * {@code i}, the view searches for {@code getMatchSymbols().get(i)}.
   *
   * @return the tickers
   */
  public List<String> getMatchSymbols() {
    return matchSymbols;
  }

  public void setMatchSymbols(List<String> matchSymbols) {
    this.matchSymbols = matchSymbols;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
