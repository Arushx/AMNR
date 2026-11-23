package view;

import interface_adapter.pairanalysis.PairAnalysisController;
import interface_adapter.pairanalysis.PairAnalysisViewModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Lets the user enter two ticker symbols and shows the correlation of their daily returns.
 *
 * <p>A symbol that doesn't match any known stock gets a red outline and a hint underneath. The
 * view only reads {@link PairAnalysisViewModel} and calls {@link PairAnalysisController}; it holds
 * no business logic.
 */
public class PairAnalysisView extends JPanel implements PropertyChangeListener {

  private static final Color ERROR_COLOR = new Color(0xC6, 0x28, 0x28);
  private static final String BLANK = " ";

  private final PairAnalysisController controller;
  private final PairAnalysisViewModel viewModel;

  private final JTextField firstField = new JTextField(10);
  private final JTextField secondField = new JTextField(10);
  private final JLabel firstHint = hintLabel();
  private final JLabel secondHint = hintLabel();
  private final JButton computeButton = new JButton("Compute correlation");
  private final JLabel statusLabel = new JLabel(BLANK);

  private final JLabel pairLabel = new JLabel(BLANK);
  private final JLabel correlationLabel = new JLabel(BLANK);
  private final JLabel detailLabel = new JLabel(BLANK);
  private final JLabel errorLabel = new JLabel(BLANK);

  private final Border normalBorder = firstField.getBorder();
  private final Border errorBorder = redBorderSizedLike(normalBorder, firstField);

  private boolean running;

  /**
   * Creates the view and registers it with its view model.
   *
   * @param controller starts a pair analysis
   * @param viewModel the information to display
   */
  public PairAnalysisView(PairAnalysisController controller, PairAnalysisViewModel viewModel) {
    this.controller = controller;
    this.viewModel = viewModel;
    viewModel.addPropertyChangeListener(this);

    setLayout(new BorderLayout(0, 16));
    setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
    add(buildInputPanel(), BorderLayout.NORTH);
    add(buildResultPanel(), BorderLayout.CENTER);

    computeButton.addActionListener(event -> runAnalysis());
    firstField.addActionListener(event -> runAnalysis());
    secondField.addActionListener(event -> runAnalysis());
    clearMarkWhenEdited(firstField, firstHint);
    clearMarkWhenEdited(secondField, secondHint);
  }

  public String getViewName() {
    return PairAnalysisViewModel.VIEW_NAME;
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    // The presenter may run on the background worker thread; Swing must be updated on the EDT.
    SwingUtilities.invokeLater(this::refresh);
  }

  private JPanel buildInputPanel() {
    final JPanel panel = new JPanel(new GridBagLayout());
    final GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    c.insets = new Insets(2, 0, 2, 8);

    addRow(panel, c, 0, "Symbol 1", firstField, firstHint);
    addRow(panel, c, 2, "Symbol 2", secondField, secondHint);

    c.gridx = 1;
    c.gridy = 4;
    c.insets = new Insets(8, 0, 0, 8);
    panel.add(computeButton, c);
    c.gridx = 2;
    panel.add(statusLabel, c);

    // An empty column that takes the spare width, so the form stays left-aligned.
    c.gridx = 3;
    c.weightx = 1;
    panel.add(Box.createHorizontalGlue(), c);
    return panel;
  }

  private static void addRow(
      JPanel panel, GridBagConstraints c, int row, String label, JTextField field, JLabel hint) {
    c.gridx = 0;
    c.gridy = row;
    panel.add(new JLabel(label), c);
    c.gridx = 1;
    c.gridwidth = 2;
    panel.add(field, c);
    c.gridy = row + 1;
    panel.add(hint, c);
    c.gridwidth = 1;
  }

  private JPanel buildResultPanel() {
    final JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    pairLabel.setFont(pairLabel.getFont().deriveFont(Font.BOLD, 16f));
    correlationLabel.setFont(correlationLabel.getFont().deriveFont(Font.BOLD, 36f));
    errorLabel.setForeground(ERROR_COLOR);

    for (JLabel label : new JLabel[] {pairLabel, correlationLabel, detailLabel, errorLabel}) {
      label.setAlignmentX(Component.LEFT_ALIGNMENT);
      panel.add(label);
      panel.add(Box.createVerticalStrut(4));
    }
    return panel;
  }

  /** A red outline with the same total insets as the normal border, so the field doesn't resize. */
  private static Border redBorderSizedLike(Border normal, JTextField field) {
    final int thickness = 2;
    final Insets insets = normal.getBorderInsets(field);
    return BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ERROR_COLOR, thickness),
        BorderFactory.createEmptyBorder(
            Math.max(0, insets.top - thickness),
            Math.max(0, insets.left - thickness),
            Math.max(0, insets.bottom - thickness),
            Math.max(0, insets.right - thickness)));
  }

  private static JLabel hintLabel() {
    final JLabel hint = new JLabel(BLANK);
    hint.setForeground(ERROR_COLOR);
    hint.setFont(hint.getFont().deriveFont(11f));
    return hint;
  }

  /** Runs the use case off the Swing thread, since it may wait on network requests. */
  private void runAnalysis() {
    if (running) {
      return;
    }
    running = true;
    computeButton.setEnabled(false);
    statusLabel.setText("Calculating...");

    final String first = firstField.getText();
    final String second = secondField.getText();
    new SwingWorker<Void, Void>() {
      @Override
      protected Void doInBackground() {
        controller.execute(first, second);
        return null;
      }

      @Override
      protected void done() {
        running = false;
        computeButton.setEnabled(true);
        statusLabel.setText(BLANK);
        try {
          get();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
          errorLabel.setText("Something went wrong: " + e.getCause().getMessage());
        }
      }
    }.execute();
  }

  /** Copies the view model onto the screen. */
  private void refresh() {
    pairLabel.setText(orBlank(viewModel.getPairText()));
    correlationLabel.setText(orBlank(viewModel.getCorrelationText()));
    detailLabel.setText(orBlank(viewModel.getDetailText()));
    errorLabel.setText(orBlank(viewModel.getError()));
    mark(firstField, firstHint, viewModel.getFirstSymbolError());
    mark(secondField, secondHint, viewModel.getSecondSymbolError());
  }

  /** Outlines a field in red and shows the hint, or restores it when there is no error. */
  private void mark(JTextField field, JLabel hint, String error) {
    field.setBorder(error == null ? normalBorder : errorBorder);
    hint.setText(orBlank(error));
  }

  /** Removes a field's red outline as soon as the user starts correcting it. */
  private void clearMarkWhenEdited(JTextField field, JLabel hint) {
    field
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              @Override
              public void insertUpdate(DocumentEvent event) {
                mark(field, hint, null);
              }

              @Override
              public void removeUpdate(DocumentEvent event) {
                mark(field, hint, null);
              }

              @Override
              public void changedUpdate(DocumentEvent event) {
                mark(field, hint, null);
              }
            });
  }

  /** JLabels collapse when empty; a single space keeps the layout from jumping. */
  private static String orBlank(String text) {
    if (text == null || text.isEmpty()) {
      return BLANK;
    }
    return text;
  }
}
