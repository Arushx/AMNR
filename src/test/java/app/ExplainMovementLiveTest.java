package app;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import data_access.AlphaVantageClient;
import data_access.AlphaVantageParser;
import data_access.AlphaVantageStockDataAccessObject;
import data_access.FinnhubClient;
import data_access.FinnhubNewsDataAccessObject;
import data_access.FinnhubNewsParser;
import data_access.FinbertModelFiles;
import data_access.LocalFinbertSentimentClassifier;
import data_access.SentimentLexiconLoader;
import entity.NewsArticle;
import entity.PriceHistory;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import use_case.NewsDataAccessInterface;
import use_case.PriceHistoryDataAccessInterface;
import use_case.explain_movement.EvidenceSelector;
import use_case.explain_movement.ExplainMovementInputData;
import use_case.explain_movement.ExplainMovementInteractor;
import use_case.explain_movement.ExplainMovementOutputBoundary;
import use_case.explain_movement.ExplainMovementOutputData;
import use_case.explain_movement.ExtractiveMoveSummarizer;
import use_case.explain_movement.FallbackSentimentClassifier;
import use_case.explain_movement.LexiconSentimentClassifier;
import use_case.explain_movement.SentimentClassifier;

/**
 * Runs Use Case 5 end to end against the real APIs and prints the result. Not a normal unit test:
 * it runs only when both API keys are set, and uses 1 Alpha Vantage request plus 1-2 Finnhub
 * requests.
 *
 * <p>Optional environment variables: {@code LIVE_SYMBOL} (default NVDA), {@code LIVE_COMPANY}
 * (default NVIDIA Corporation) and {@code LIVE_DAYS} (default 30, how many days back from
 * yesterday).
 *
 * <p>Sentiment: FinBERT is used when its files are available: in {@code FINBERT_MODEL_DIR}, or
 * downloaded first when {@code FINBERT_DOWNLOAD=true}, or already in the default folder. {@code
 * LIVE_SENTIMENT} picks what runs: {@code both} (the default when FinBERT is available: the use
 * case runs twice on the same prices and news, once per classifier, with no extra API requests),
 * {@code finbert} or {@code dictionary}.
 */
@EnabledIfEnvironmentVariable(named = "ALPHA_VANTAGE_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "FINNHUB_API_KEY", matches = ".+")
class ExplainMovementLiveTest {

  @Test
  void explainsARealStock() throws IOException {
    final String symbol = env("LIVE_SYMBOL", "NVDA");
    final String company = env("LIVE_COMPANY", "NVIDIA Corporation");
    final int days = Integer.parseInt(env("LIVE_DAYS", "30"));
    final LocalDate end = LocalDate.now().minusDays(1);
    final LocalDate start = end.minusDays(days);

    final PriceHistoryDataAccessInterface prices =
        once(new AlphaVantageStockDataAccessObject(
            new AlphaVantageClient(key("ALPHA_VANTAGE_API_KEY")), new AlphaVantageParser()));
    final NewsDataAccessInterface news =
        once(new FinnhubNewsDataAccessObject(
            new FinnhubClient(key("FINNHUB_API_KEY")), new FinnhubNewsParser()));
    final SentimentClassifier dictionary =
        new LexiconSentimentClassifier(new SentimentLexiconLoader().loadDefault());

    final LocalFinbertSentimentClassifier finbert = loadFinbertIfAvailable();
    try {
      final String mode =
          env("LIVE_SENTIMENT", finbert == null ? "dictionary" : "both").toLowerCase(Locale.ROOT);
      if (finbert == null && !mode.equals("dictionary")) {
        fail("LIVE_SENTIMENT=" + mode + " needs FinBERT: set FINBERT_DOWNLOAD=true or"
            + " FINBERT_MODEL_DIR");
      }
      if (mode.equals("finbert") || mode.equals("both")) {
        run("FinBERT (dictionary as backup)",
            new FallbackSentimentClassifier(finbert, dictionary), prices, news, symbol, company,
            start, end);
      }
      if (mode.equals("dictionary") || mode.equals("both")) {
        run("Loughran-McDonald dictionary", dictionary, prices, news, symbol, company, start,
            end);
      }
    } finally {
      if (finbert != null) {
        finbert.close();
      }
    }
  }

  private static void run(
      String label, SentimentClassifier classifier, PriceHistoryDataAccessInterface prices,
      NewsDataAccessInterface news, String symbol, String company, LocalDate start,
      LocalDate end) {
    System.out.println();
    System.out.println("=== Use Case 5 live result, sentiment: " + label + " ===");
    final PrintingPresenter presenter = new PrintingPresenter();
    final long startTime = System.nanoTime();
    new ExplainMovementInteractor(
            prices, news, classifier, new EvidenceSelector(), new ExtractiveMoveSummarizer(),
            presenter)
        .execute(new ExplainMovementInputData(symbol, company, start, end));
    System.out.printf("(took %.2f s)%n", (System.nanoTime() - startTime) / 1e9);

    assertNull(presenter.failure, presenter.failure);
    assertNotNull(presenter.success);
  }

  /** Loads FinBERT if its files are available (downloading first if asked); otherwise null. */
  private static LocalFinbertSentimentClassifier loadFinbertIfAvailable() throws IOException {
    final FinbertModelFiles files =
        new FinbertModelFiles(
            FinbertModelFiles.directoryOrDefault(System.getenv("FINBERT_MODEL_DIR")));
    if ("true".equalsIgnoreCase(env("FINBERT_DOWNLOAD", "false").strip())) {
      files.downloadMissing();
    }
    if (!files.isComplete()) {
      System.out.println("FinBERT files not found in " + files.getDirectory()
          + "; using the dictionary only.");
      return null;
    }
    System.out.println("FinBERT files: " + files.getDirectory());
    return LocalFinbertSentimentClassifier.load(files);
  }

  /** Asks the real data access object once per symbol, then answers from memory. */
  private static PriceHistoryDataAccessInterface once(PriceHistoryDataAccessInterface source) {
    final Map<String, PriceHistory> seen = new HashMap<>();
    return symbol -> {
      if (!seen.containsKey(symbol)) {
        seen.put(symbol, source.getDailyHistory(symbol));
      }
      return seen.get(symbol);
    };
  }

  /** Asks the real data access object once per request, then answers from memory. */
  private static NewsDataAccessInterface once(NewsDataAccessInterface source) {
    final Map<String, List<NewsArticle>> seen = new HashMap<>();
    return (symbol, from, to) -> {
      final String key = symbol + from + to;
      if (!seen.containsKey(key)) {
        seen.put(key, source.getNews(symbol, from, to));
      }
      return seen.get(key);
    };
  }

  /**
   * Reads an API key and fails with a clear message if it is unusable. Prints only the key's
   * length and first two characters, never the whole key.
   */
  private static String key(String name) {
    final String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      fail(
          name + " reached the test but is blank: length "
              + (value == null ? "null" : value.length())
              + ". Check the line in .env (key on the same line, right after =) and that the"
              + " run configuration's Environment variables box doesn't set it to nothing.");
    }
    System.out.println(
        name + ": length " + value.length() + ", starts with \""
            + value.strip().substring(0, Math.min(2, value.strip().length())) + "\""
            + (value.equals(value.strip()) ? "" : " (has spaces or line breaks around it)"));
    return value.strip();
  }

  private static String env(String name, String fallback) {
    final String value = System.getenv(name);
    return value == null || value.isBlank() ? fallback : value;
  }

  /** Prints the result so it can be read in the test output. */
  private static class PrintingPresenter implements ExplainMovementOutputBoundary {
    private ExplainMovementOutputData success;
    private String failure;

    @Override
    public void prepareSuccessView(ExplainMovementOutputData out) {
      success = out;
      System.out.printf(
          "%s %s %.2f%% (%s -> %s, close %.2f -> %.2f)%n",
          out.getSymbol(), out.getDirection(), out.getChangePercent(), out.getStart(),
          out.getEnd(), out.getStartClose(), out.getEndClose());
      if (!out.getNewsError().isEmpty()) {
        System.out.println("News error: " + out.getNewsError());
      }
      System.out.println(out.getSummary());
      for (int i = 0; i < out.getSources().size(); i++) {
        final ExplainMovementOutputData.Source source = out.getSources().get(i);
        System.out.printf(
            "[%d] %s (%s, %s, %s)%n    %s%n",
            i + 1, source.getHeadline(), source.getPublisher(), source.getPublishedAt(),
            source.getSentiment(), source.getUrl());
      }
    }

    @Override
    public void prepareFailView(String errorMessage) {
      failure = errorMessage;
      System.out.println("Failed: " + errorMessage);
    }
  }
}
