package use_case.explain_movement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A short explanation of a price move and the articles it cites, in citation order (the first
 * source is "[1]" in the text). Immutable.
 */
public class MovementSummary {

  private final String text;
  private final List<LabeledArticle> sources;

  /**
   * Creates a summary.
   *
   * @param text the explanation shown to the user
   * @param sources the cited articles, in the order they are numbered in the text
   * @throws IllegalArgumentException if the text is blank or sources is null
   */
  public MovementSummary(String text, List<LabeledArticle> sources) {
    if (text == null || text.isBlank()) {
      throw new IllegalArgumentException("summary text must not be blank");
    }
    if (sources == null) {
      throw new IllegalArgumentException("sources must not be null");
    }
    this.text = text;
    this.sources = Collections.unmodifiableList(new ArrayList<>(sources));
  }

  public String getText() {
    return text;
  }

  /**
   * Returns the cited articles, in citation order.
   *
   * @return an unmodifiable list
   */
  public List<LabeledArticle> getSources() {
    return sources;
  }
}
