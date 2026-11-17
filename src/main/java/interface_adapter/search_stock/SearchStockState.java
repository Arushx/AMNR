package interface_adapter.search_stock;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Everything the search view needs to display, already formatted.
 *
 * <p>After a search, either the quote fields and chart data are filled and {@code error} is null,
 * or {@code error} holds a message to show.
 */
public class SearchStockState {

  private String symbol = "";
  private String priceText = "";
  private String changeText = "";
  private String asOfText = "";
  private boolean up = true;
  private List<LocalDate> chartDates = new ArrayList<>();
  private List<Double> chartCloses = new ArrayList<>();
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

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
