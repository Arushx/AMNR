package use_case.explain_movement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static use_case.explain_movement.TestData.movement;

import java.util.List;
import org.junit.jupiter.api.Test;

class FallbackMoveSummarizerTest {

  private static final NewsEvidence NO_NEWS = new NewsEvidence(List.of(), List.of());

  private static MoveSummarizer fixed(String text) {
    return (movement, companyName, evidence) -> new MovementSummary(text, List.of());
  }

  private static final MoveSummarizer FAILING =
      (movement, companyName, evidence) -> {
        throw new SummaryUnavailableException("model is down");
      };

  @Test
  void usesThePrimaryWhenItWorks() throws SummaryUnavailableException {
    MoveSummarizer summarizer = new FallbackMoveSummarizer(fixed("primary"), fixed("backup"));

    assertEquals("primary", summarizer.summarize(movement(1, 2), "", NO_NEWS).getText());
  }

  @Test
  void usesTheBackupWhenThePrimaryFails() throws SummaryUnavailableException {
    MoveSummarizer summarizer = new FallbackMoveSummarizer(FAILING, fixed("backup"));

    assertEquals("backup", summarizer.summarize(movement(1, 2), "", NO_NEWS).getText());
  }
}
