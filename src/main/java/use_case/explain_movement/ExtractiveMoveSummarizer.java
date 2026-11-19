package use_case.explain_movement;

import entity.Direction;
import entity.NewsArticle;
import entity.PriceMovement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds the summary from the chosen headlines, with no language model. Examples:
 *
 * <pre>
 * NVIDIA (NVDA) fell 12.4% between Mar 2, 2026 and Mar 13, 2026, likely related to:
 * "Nvidia shares slide after export ban" [1] (Reuters, Mar 3) and "..." [2] (CNBC, Mar 4).
 * News in this period was mostly negative (9 of 12 articles).
 * </pre>
 *
 * <p>It cites up to {@value #MAX_DRIVERS} of the chosen articles whose sentiment matches the move.
 * If none does, it says no clear news explanation was found and lists the top related articles
 * instead. It never fails, so it is also the fallback for other summarizers.
 */
public class ExtractiveMoveSummarizer implements MoveSummarizer {

  static final int MAX_DRIVERS = 3;

  private static final DateTimeFormatter LONG_DATE =
      DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
  private static final DateTimeFormatter SHORT_DATE =
      DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

  @Override
  public MovementSummary summarize(
      PriceMovement movement, String companyName, NewsEvidence evidence) {
    final StringBuilder text = new StringBuilder(describeMove(movement, companyName));
    if (evidence.getRelevantCount() == 0) {
      text.append(". No news about this company was found for this period.");
      return new MovementSummary(text.toString(), List.of());
    }

    List<LabeledArticle> cited = drivers(evidence, movement.getDirection());
    if (cited.isEmpty()) {
      cited = firstN(evidence.getSelected(), MAX_DRIVERS);
      text.append(". No single news event clearly matches this move. Related news: ");
    } else {
      text.append(", likely related to: ");
    }
    text.append(listHeadlines(cited)).append(". ").append(describeTone(evidence));
    return new MovementSummary(text.toString(), cited);
  }

  private static String describeMove(PriceMovement movement, String companyName) {
    final String who =
        companyName == null || companyName.isBlank()
            ? movement.getSymbol()
            : companyName.trim() + " (" + movement.getSymbol() + ")";
    final String between =
        " between " + LONG_DATE.format(movement.getStart()) + " and "
            + LONG_DATE.format(movement.getEnd());
    final String percent =
        String.format(Locale.ROOT, "%.1f%%", Math.abs(movement.getChangePercent()));
    switch (movement.getDirection()) {
      case UP:
        return who + " rose " + percent + between;
      case DOWN:
        return who + " fell " + percent + between;
      default:
        return who + " was roughly flat (" + signed(movement.getChangePercent()) + ")" + between;
    }
  }

  private static List<LabeledArticle> drivers(NewsEvidence evidence, Direction direction) {
    final List<LabeledArticle> drivers = new ArrayList<>();
    for (LabeledArticle labeled : evidence.getSelected()) {
      if (drivers.size() < MAX_DRIVERS
          && EvidenceSelector.matchesDirection(labeled.getSentiment(), direction)) {
        drivers.add(labeled);
      }
    }
    return drivers;
  }

  private static String listHeadlines(List<LabeledArticle> cited) {
    final List<String> parts = new ArrayList<>();
    for (int i = 0; i < cited.size(); i++) {
      final NewsArticle article = cited.get(i).getArticle();
      final String where =
          article.getSource().isEmpty()
              ? shortDate(article.getPublishedDate())
              : article.getSource() + ", " + shortDate(article.getPublishedDate());
      parts.add("\"" + article.getHeadline() + "\" [" + (i + 1) + "] (" + where + ")");
    }
    if (parts.size() == 1) {
      return parts.get(0);
    }
    return String.join(", ", parts.subList(0, parts.size() - 1))
        + " and " + parts.get(parts.size() - 1);
  }

  private static String describeTone(NewsEvidence evidence) {
    final int total = evidence.getRelevantCount();
    final int positive = evidence.getPositiveCount();
    final int negative = evidence.getNegativeCount();
    final int neutral = evidence.getNeutralCount();
    final String tone;
    final int count;
    if (negative > positive && negative > neutral) {
      tone = "mostly negative";
      count = negative;
    } else if (positive > negative && positive > neutral) {
      tone = "mostly positive";
      count = positive;
    } else if (neutral > positive && neutral > negative) {
      tone = "mostly neutral";
      count = neutral;
    } else {
      return "News in this period was mixed (" + total + " " + articles(total) + ").";
    }
    return "News in this period was " + tone + " (" + count + " of " + total + " "
        + articles(total) + ").";
  }

  private static List<LabeledArticle> firstN(List<LabeledArticle> list, int n) {
    return new ArrayList<>(list.subList(0, Math.min(n, list.size())));
  }

  private static String articles(int count) {
    return count == 1 ? "article" : "articles";
  }

  private static String shortDate(LocalDate date) {
    return SHORT_DATE.format(date);
  }

  private static String signed(double percent) {
    return String.format(Locale.ROOT, "%+.1f%%", percent);
  }
}
