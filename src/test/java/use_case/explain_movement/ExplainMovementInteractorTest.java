package use_case.explain_movement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static use_case.explain_movement.TestData.DAY1;
import static use_case.explain_movement.TestData.article;
import static use_case.explain_movement.TestData.history;

import entity.NewsArticle;
import entity.PriceHistory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import use_case.NewsDataAccessInterface;
import use_case.NewsUnavailableException;
import use_case.PriceHistoryDataAccessInterface;
import use_case.StockDataUnavailableException;

/** Runs the whole use case with fake data access objects and a recording presenter. */
class ExplainMovementInteractorTest {

  /** Prices 100, 95, 88 on DAY1..DAY1+2: a 12% drop. */
  private static final PriceHistory DROP = history(100, 95, 88);

  private final RecordingPresenter presenter = new RecordingPresenter();
  private final FakeNews news = new FakeNews();
  private final SentimentClassifier classifier =
      new LexiconSentimentClassifier(new SentimentLexicon(Set.of("gain"), Set.of("loss")));

  private ExplainMovementInteractor interactor(PriceHistoryDataAccessInterface prices) {
    return new ExplainMovementInteractor(
        prices, news, classifier, new EvidenceSelector(), new ExtractiveMoveSummarizer(),
        presenter);
  }

  private static PriceHistoryDataAccessInterface prices(PriceHistory history) {
    return symbol -> history;
  }

  private static ExplainMovementInputData input(LocalDate start, LocalDate end) {
    return new ExplainMovementInputData("acme", "Acme Corp", start, end);
  }

  @Test
  void explainsADropWithTheMatchingNews() {
    news.articles =
        List.of(
            article("ACME reports loss", DAY1.plusDays(1)),
            article("ACME annual meeting", DAY1),
            article("Oil prices rise", DAY1, List.of("XOM")));

    interactor(prices(DROP)).execute(input(DAY1, DAY1.plusDays(2)));

    ExplainMovementOutputData out = presenter.success;
    assertEquals("ACME", out.getSymbol());
    assertEquals("DOWN", out.getDirection());
    assertEquals(-12.0, out.getChangePercent(), 1e-9);
    assertEquals(DAY1, out.getStart());
    assertTrue(out.getSummary().contains("likely related to: \"ACME reports loss\" [1]"));
    assertEquals(1, out.getSources().size());
    assertEquals("ACME reports loss", out.getSources().get(0).getHeadline());
    assertEquals("NEGATIVE", out.getSources().get(0).getSentiment());
    assertEquals("", out.getNewsError());
  }

  @Test
  void asksForNewsFromTwoDaysBeforeTheFirstTradingDay() {
    interactor(prices(DROP)).execute(input(DAY1.minusDays(5), DAY1.plusDays(9)));

    assertEquals("ACME", news.symbol);
    assertEquals(DAY1.minusDays(2), news.from);
    assertEquals(DAY1.plusDays(2), news.to);
  }

  @Test
  void newsFailureStillReportsThePriceMove() {
    news.failure = "Finnhub rate limit reached";

    interactor(prices(DROP)).execute(input(DAY1, DAY1.plusDays(2)));

    assertEquals(-12.0, presenter.success.getChangePercent(), 1e-9);
    assertEquals("", presenter.success.getSummary());
    assertTrue(presenter.success.getSources().isEmpty());
    assertEquals("Finnhub rate limit reached", presenter.success.getNewsError());
  }

  @Test
  void priceFailureShowsTheFailView() {
    PriceHistoryDataAccessInterface failing =
        symbol -> {
          throw new StockDataUnavailableException("Could not get price history for ACME");
        };

    interactor(failing).execute(input(DAY1, DAY1.plusDays(2)));

    assertEquals("Could not get price history for ACME", presenter.failure);
    assertNull(presenter.success);
  }

  @Test
  void rangeWithFewerThanTwoTradingDaysFails() {
    interactor(prices(DROP)).execute(input(DAY1.plusDays(2), DAY1.plusDays(30)));

    assertTrue(presenter.failure.startsWith("Not enough price data for ACME between"));
  }

  @Test
  void rejectsBadInput() {
    interactor(prices(DROP)).execute(new ExplainMovementInputData(" ", "", DAY1, DAY1));
    assertEquals("Please enter a ticker symbol.", presenter.failure);

    interactor(prices(DROP)).execute(input(DAY1.plusDays(1), DAY1));
    assertEquals("The start date must not be after the end date.", presenter.failure);

    interactor(prices(DROP)).execute(input(null, DAY1));
    assertEquals("Please choose a start and an end date.", presenter.failure);
  }

  @Test
  void sentimentFailureStillReportsThePriceMove() {
    SentimentClassifier failing =
        articles -> {
          throw new SentimentUnavailableException("model missing");
        };
    news.articles = List.of(article("ACME reports loss", DAY1));

    new ExplainMovementInteractor(
            prices(DROP), news, failing, new EvidenceSelector(),
            new ExtractiveMoveSummarizer(), presenter)
        .execute(input(DAY1, DAY1.plusDays(2)));

    assertEquals(-12.0, presenter.success.getChangePercent(), 1e-9);
    assertEquals("", presenter.success.getSummary());
    assertEquals("Could not analyse the news: model missing", presenter.success.getNewsError());
  }

  @Test
  void summarizerFailureShowsTheFailView() {
    MoveSummarizer failing =
        (movement, companyName, evidence) -> {
          throw new SummaryUnavailableException("model is down");
        };
    new ExplainMovementInteractor(
            prices(DROP), news, classifier, new EvidenceSelector(), failing, presenter)
        .execute(input(DAY1, DAY1.plusDays(2)));

    assertEquals("Could not write a summary: model is down", presenter.failure);
  }

  @Test
  void onlyRelevantArticlesAreClassified() {
    List<Integer> batchSizes = new ArrayList<>();
    SentimentClassifier counting =
        articles -> {
          batchSizes.add(articles.size());
          return classifier.classify(articles);
        };
    news.articles =
        List.of(article("ACME reports loss", DAY1), article("Oil", DAY1, List.of("XOM")));

    new ExplainMovementInteractor(
            prices(DROP), news, counting, new EvidenceSelector(),
            new ExtractiveMoveSummarizer(), presenter)
        .execute(input(DAY1, DAY1.plusDays(2)));

    assertEquals(List.of(1), batchSizes);
  }

  /** Remembers what it was asked for and answers with {@link #articles}, or fails. */
  private static class FakeNews implements NewsDataAccessInterface {
    private List<NewsArticle> articles = List.of();
    private String failure;
    private String symbol;
    private LocalDate from;
    private LocalDate to;

    @Override
    public List<NewsArticle> getNews(String symbol, LocalDate from, LocalDate to)
        throws NewsUnavailableException {
      this.symbol = symbol;
      this.from = from;
      this.to = to;
      if (failure != null) {
        throw new NewsUnavailableException(failure);
      }
      return articles;
    }
  }

  /** Keeps the last result it was given. */
  private static class RecordingPresenter implements ExplainMovementOutputBoundary {
    private ExplainMovementOutputData success;
    private String failure;

    @Override
    public void prepareSuccessView(ExplainMovementOutputData outputData) {
      success = outputData;
    }

    @Override
    public void prepareFailView(String errorMessage) {
      failure = errorMessage;
    }
  }

}
