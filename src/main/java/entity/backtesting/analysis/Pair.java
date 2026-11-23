package entity.backtesting.analysis;

import entity.PriceHistory;
import entity.PricePoint;
import java.util.ArrayList;
import java.util.List;

/**
 * A pair of stocks and the correlation between their daily returns.
 *
 * <p>Returns are calculated only over trading dates shared by both price histories. Instances are
 * immutable.
 */
public class Pair {

  private final PriceHistory firstHistory;
  private final PriceHistory secondHistory;

  /**
   * Creates a pair of stocks.
   *
   * @param firstHistory the first stock's price history
   * @param secondHistory the second stock's price history
   * @throws IllegalArgumentException if either history is null
   */
  public Pair(PriceHistory firstHistory, PriceHistory secondHistory) {
    if (firstHistory == null || secondHistory == null) {
      throw new IllegalArgumentException("price histories must not be null");
    }
    this.firstHistory = firstHistory;
    this.secondHistory = secondHistory;
  }

  public String getFirstTicker() {
    return firstHistory.getSymbol();
  }

  public String getSecondTicker() {
    return secondHistory.getSymbol();
  }

  public PriceHistory getFirstHistory() {
    return firstHistory;
  }

  public PriceHistory getSecondHistory() {
    return secondHistory;
  }

  /**
   * Returns how many daily returns both stocks share, i.e. the number of observations used by
   * {@link #getReturnsCorrelation()}. This is one less than the number of shared trading dates.
   *
   * @return the number of shared daily returns (0 if fewer than two dates are shared)
   */
  public int getSharedReturnCount() {
    return alignedReturns().getFirst().size();
  }

  /**
   * Calculates the Pearson correlation between the two stocks' daily percentage returns.
   *
   * @return the returns correlation, between -1 and 1
   * @throws IllegalStateException if fewer than three dates are shared or either return series is
   *     constant
   */
  public double getReturnsCorrelation() {
    final ReturnSeries returns = alignedReturns();
    if (returns.getFirst().size() < 2) {
      throw new IllegalStateException("At least three shared trading dates are required");
    }
    return pearsonCorrelation(returns.getFirst(), returns.getSecond());
  }

  /**
   * Walks both histories (each sorted oldest first) together and computes daily returns for each
   * stock between consecutive shared dates.
   */
  private ReturnSeries alignedReturns() {
    final List<PricePoint> firstPoints = firstHistory.getPoints();
    final List<PricePoint> secondPoints = secondHistory.getPoints();
    final List<Double> firstReturns = new ArrayList<>();
    final List<Double> secondReturns = new ArrayList<>();

    int i = 0;
    int j = 0;
    PricePoint previousFirst = null;
    PricePoint previousSecond = null;

    while (i < firstPoints.size() && j < secondPoints.size()) {
      final PricePoint first = firstPoints.get(i);
      final PricePoint second = secondPoints.get(j);
      final int comparison = first.getDate().compareTo(second.getDate());

      if (comparison < 0) {
        i++;
      } else if (comparison > 0) {
        j++;
      } else {
        // Both stocks have a price on this date.
        if (previousFirst != null) {
          firstReturns.add(dailyReturn(previousFirst, first));
          secondReturns.add(dailyReturn(previousSecond, second));
        }
        previousFirst = first;
        previousSecond = second;
        i++;
        j++;
      }
    }
    return new ReturnSeries(firstReturns, secondReturns);
  }

  private static double dailyReturn(PricePoint previous, PricePoint current) {
    return (current.getClose() - previous.getClose()) / previous.getClose();
  }

  /** Calculates the Pearson correlation of two equally sized series. */
  private static double pearsonCorrelation(List<Double> x, List<Double> y) {
    final int n = x.size();
    double meanX = 0;
    double meanY = 0;
    for (int i = 0; i < n; i++) {
      meanX += x.get(i);
      meanY += y.get(i);
    }
    meanX /= n;
    meanY /= n;

    double numerator = 0;
    double sumSquaresX = 0;
    double sumSquaresY = 0;
    for (int i = 0; i < n; i++) {
      final double dx = x.get(i) - meanX;
      final double dy = y.get(i) - meanY;
      numerator += dx * dy;
      sumSquaresX += dx * dx;
      sumSquaresY += dy * dy;
    }

    if (sumSquaresX == 0 || sumSquaresY == 0) {
      throw new IllegalStateException("Correlation is undefined for constant returns");
    }
    return numerator / Math.sqrt(sumSquaresX * sumSquaresY);
  }

  /** The two stocks' daily returns on shared dates, index-aligned. */
  private static final class ReturnSeries {
    private final List<Double> first;
    private final List<Double> second;

    private ReturnSeries(List<Double> first, List<Double> second) {
      this.first = first;
      this.second = second;
    }

    private List<Double> getFirst() {
      return first;
    }

    private List<Double> getSecond() {
      return second;
    }
  }
}
