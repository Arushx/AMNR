package use_case.explain_movement;

import java.time.LocalDate;

/** What the user asked: explain how this stock moved between these two days. */
public class ExplainMovementInputData {

  private final String symbol;
  private final String companyName;
  private final LocalDate start;
  private final LocalDate end;

  /**
   * Creates the input.
   *
   * @param symbol the ticker symbol, e.g. "NVDA"
   * @param companyName the company's name, e.g. "NVIDIA Corporation"; may be blank
   * @param start the first day of the period
   * @param end the last day of the period
   */
  public ExplainMovementInputData(
      String symbol, String companyName, LocalDate start, LocalDate end) {
    this.symbol = symbol;
    this.companyName = companyName;
    this.start = start;
    this.end = end;
  }

  public String getSymbol() {
    return symbol;
  }

  public String getCompanyName() {
    return companyName;
  }

  public LocalDate getStart() {
    return start;
  }

  public LocalDate getEnd() {
    return end;
  }
}
