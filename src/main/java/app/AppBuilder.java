package app;

import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Assembles the Clean Architecture engine for each use case and wires it into one window.
 *
 * <p>This is the "Main component": the only place that knows about concrete classes from every
 * layer. Each use case lead adds an {@code addXView()} method that registers their view in
 * {@link #cardPanel}, and an {@code addXUseCase()} method that creates the data access object,
 * presenter, interactor and controller and connects them. Calls are chained in {@link Main}.
 */
public class AppBuilder {

  private static final String TITLE = "AMNR"; // TODO Decide if this is our final

  private final CardLayout cardLayout = new CardLayout();
  private final JPanel cardPanel = new JPanel(cardLayout);

  /**
   * Creates the application window containing every view added so far.
   *
   * @return the configured, not yet visible, main window
   */
  public JFrame build() {
    if (cardPanel.getComponentCount() == 0) {
      // Placeholder until the first view is added; remove once a real view exists.
      cardPanel.add(new JLabel(TITLE + " - no views yet", SwingConstants.CENTER), "placeholder");
    }
    final JFrame application = new JFrame(TITLE);
    application.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    application.add(cardPanel);
    return application;
  }
}
