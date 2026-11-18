package use_case;

import entity.NewsArticle;
import java.time.LocalDate;
import java.util.List;

/** Gives a use case the news about a company, without saying where it comes from. */
public interface NewsDataAccessInterface {

  /**
   * Returns the news articles about a stock published from {@code from} to {@code to}, both
   * included, oldest first.
   *
   * @param symbol the ticker symbol, e.g. "NVDA"
   * @param from the first day to include
   * @param to the last day to include
   * @return the articles; empty if there were none
   * @throws NewsUnavailableException if the news can't be provided
   */
  List<NewsArticle> getNews(String symbol, LocalDate from, LocalDate to)
      throws NewsUnavailableException;
}
