import database.DatabaseConnection;
import ui.LoginFrame;

import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Entry point: tests the Derby connection, then opens the login screen. */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // fall back to the default look and feel
        }

        try (Connection c = DatabaseConnection.getConnection()) {
            System.out.println("Derby connection OK: " + c.getMetaData().getURL());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Cannot connect to the Derby database.\n"
                    + "1. Start the Derby Network Server.\n"
                    + "2. Run database.DatabaseSetup once to create the tables.\n\n" + e.getMessage(),
                    "Database Connection Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
