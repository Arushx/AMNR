package app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** Entry point for JoomBerg. Builds the application and shows the main window. */
public class Main {

  /**
   * Starts the program.
   *
   * @param args unused command-line arguments
   */

  public static void main(String[] args) {
    SwingUtilities.invokeLater(
        () -> {
          final JFrame application = new AppBuilder().build();
          application.pack();
          application.setLocationRelativeTo(null);
          application.setVisible(true);
        });
  }
}
