package app;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import data_access.AlphaVantageClient;
import data_access.AlphaVantageParser;
import data_access.AlphaVantageStockDataAccessObject;
import data_access.FinnhubClient;
import data_access.FinnhubNewsDataAccessObject;
import data_access.FinnhubNewsParser;
import data_access.SentimentLexiconLoader;
import java.io.IOException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import use_case.explain_movement.EvidenceSelector;
import use_case.explain_movement.ExplainMovementInputData;
import use_case.explain_movement.ExplainMovementInteractor;
import use_case.explain_movement.ExplainMovementOutputBoundary;
import use_case.explain_movement.ExplainMovementOutputData;
import use_case.explain_movement.ExtractiveMoveSummarizer;
import use_case.explain_movement.LexiconSentimentClassifier;

/**
 * Runs Use Case 5 end to end against the real APIs and prints the result. Not a normal unit test:
 * it runs only when both API keys are set, and uses 1 Alpha Vantage request plus 1-2 Finnhub
 * requests.
 *
 * <p>Optional environment variables: {@code LIVE_SYMBOL} (default NVDA), {@code LIVE_COMPANY}
 * (default NVIDIA Corporation) and {@code LIVE_DAYS} (default 30, how many days back from
 * yesterday).
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

    final PrintingPresenter presenter = new PrintingPresenter();
    new ExplainMovementInteractor(
            new AlphaVantageStockDataAccessObject(
                new AlphaVantageClient(System.getenv("ALPHA_VANTAGE_API_KEY")),
                new AlphaVantageParser()),
            new FinnhubNewsDataAccessObject(
                new FinnhubClient(System.getenv("FINNHUB_API_KEY")), new FinnhubNewsParser()),
            new LexiconSentimentClassifier(new SentimentLexiconLoader().loadDefault()),
            new EvidenceSelector(),
            new ExtractiveMoveSummarizer(),
            presenter)
        .execute(new ExplainMovementInputData(symbol, company, start, end));

    assertNull(presenter.failure, presenter.failure);
    assertNotNull(presenter.success);
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
      System.out.println("=== Use Case 5 live result ===");
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
