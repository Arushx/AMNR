package use_case.explain_movement;

import entity.NewsArticle;
import entity.PriceHistory;
import entity.PriceMovement;
import entity.Sentiment;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import use_case.NewsDataAccessInterface;
import use_case.NewsUnavailableException;
import use_case.PriceHistoryDataAccessInterface;
import use_case.StockDataUnavailableException;

/**
 * Explains how a stock moved over a period and which news likely caused it.
 *
 * <ol>
 *   <li>Measure the move from the daily prices ({@link PriceMovement}).
 *   <li>Fetch the company's news from {@value #NEWS_DAYS_BEFORE} days before the period to its end
 *       (news just before the period can move the price on its first day).
 *   <li>Keep the articles about the company and label each one's sentiment.
 *   <li>Choose the articles that best explain the move ({@link EvidenceSelector}).
 *   <li>Write the summary ({@link MoveSummarizer}).
 * </ol>
 *
 * <p>If the prices can't be loaded, the fail view is shown. If only the news can't be loaded or
 * labeled, the success view still gets the price move, with an empty summary and the news error.
 */
public class ExplainMovementInteractor implements ExplainMovementInputBoundary {

  static final int NEWS_DAYS_BEFORE = 2;

  private static final DateTimeFormatter DATE =
      DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

  private final PriceHistoryDataAccessInterface priceDataAccess;
  private final NewsDataAccessInterface newsDataAccess;
  private final SentimentClassifier classifier;
  private final EvidenceSelector selector;
  private final MoveSummarizer summarizer;
  private final ExplainMovementOutputBoundary presenter;

  /**
   * Creates the interactor.
   *
   * @param priceDataAccess gives the daily prices
   * @param newsDataAccess gives the company news
   * @param classifier labels article sentiment
   * @param selector chooses the relevant, best-matching articles
   * @param summarizer writes the summary
   * @param presenter receives the result
   */
  public ExplainMovementInteractor(
      PriceHistoryDataAccessInterface priceDataAccess,
      NewsDataAccessInterface newsDataAccess,
      SentimentClassifier classifier,
      EvidenceSelector selector,
      MoveSummarizer summarizer,
      ExplainMovementOutputBoundary presenter) {
    this.priceDataAccess = priceDataAccess;
    this.newsDataAccess = newsDataAccess;
    this.classifier = classifier;
    this.selector = selector;
    this.summarizer = summarizer;
    this.presenter = presenter;
  }

  @Override
  public void execute(ExplainMovementInputData inputData) {
    final String symbol =
        inputData.getSymbol() == null
            ? ""
            : inputData.getSymbol().trim().toUpperCase(Locale.ROOT);
    final String companyName =
        inputData.getCompanyName() == null ? "" : inputData.getCompanyName().trim();
    if (symbol.isEmpty()) {
      presenter.prepareFailView("Please enter a ticker symbol.");
      return;
    }
    if (inputData.getStart() == null || inputData.getEnd() == null) {
      presenter.prepareFailView("Please choose a start and an end date.");
      return;
    }
    if (inputData.getStart().isAfter(inputData.getEnd())) {
      presenter.prepareFailView("The start date must not be after the end date.");
      return;
    }

    final PriceHistory history;
    try {
      history = priceDataAccess.getDailyHistory(symbol);
    } catch (StockDataUnavailableException e) {
      presenter.prepareFailView(e.getMessage());
      return;
    }
    final PriceHistory inRange = history.between(inputData.getStart(), inputData.getEnd());
    if (inRange.getPoints().size() < 2) {
      presenter.prepareFailView(
          "Not enough price data for " + symbol + " between "
              + DATE.format(inputData.getStart()) + " and " + DATE.format(inputData.getEnd())
              + ". Pick a range with at least two trading days within the last 100 trading days.");
      return;
    }
    final PriceMovement movement = PriceMovement.of(inRange);

    final List<NewsArticle> news;
    try {
      news =
          newsDataAccess.getNews(
              symbol, movement.getStart().minusDays(NEWS_DAYS_BEFORE), movement.getEnd());
    } catch (NewsUnavailableException e) {
      presenter.prepareSuccessView(output(movement, companyName, "", List.of(), e.getMessage()));
      return;
    }

    final List<LabeledArticle> relevant;
    try {
      relevant = label(selector.filterRelevant(news, movement.getSymbol(), companyName));
    } catch (SentimentUnavailableException e) {
      presenter.prepareSuccessView(
          output(movement, companyName, "", List.of(),
              "Could not analyse the news: " + e.getMessage()));
      return;
    }
    final NewsEvidence evidence = selector.select(relevant, movement, companyName);
    final MovementSummary summary;
    try {
      summary = summarizer.summarize(movement, companyName, evidence);
    } catch (SummaryUnavailableException e) {
      presenter.prepareFailView("Could not write a summary: " + e.getMessage());
      return;
    }
    presenter.prepareSuccessView(
        output(movement, companyName, summary.getText(), summary.getSources(), ""));
  }

  private List<LabeledArticle> label(List<NewsArticle> articles)
      throws SentimentUnavailableException {
    final List<Sentiment> labels = classifier.classify(articles);
    if (labels.size() != articles.size()) {
      throw new IllegalStateException("classifier must return one label per article");
    }
    final List<LabeledArticle> labeled = new ArrayList<>();
    for (int i = 0; i < articles.size(); i++) {
      labeled.add(new LabeledArticle(articles.get(i), labels.get(i)));
    }
    return labeled;
  }

  private static ExplainMovementOutputData output(
      PriceMovement movement,
      String companyName,
      String summary,
      List<LabeledArticle> cited,
      String newsError) {
    final List<ExplainMovementOutputData.Source> sources = new ArrayList<>();
    for (LabeledArticle labeled : cited) {
      final NewsArticle article = labeled.getArticle();
      sources.add(
          new ExplainMovementOutputData.Source(
              article.getHeadline(),
              article.getSource(),
              article.getUrl(),
              article.getPublishedAt(),
              labeled.getSentiment().name()));
    }
    return new ExplainMovementOutputData(
        movement.getSymbol(),
        companyName,
        movement.getStart(),
        movement.getEnd(),
        movement.getStartClose(),
        movement.getEndClose(),
        movement.getChangePercent(),
        movement.getDirection().name(),
        summary,
        sources,
        newsError);
  }
}
