package util;

import java.awt.Component;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * SQLState code:
 *   23505 = duplicate value (UNIQUE constraint)
 *   23503 = linked record (FOREIGN KEY constraint)
 *   08xxx = cannot connect to the database
 */
public final class DbErrors {

    private DbErrors() { }

    public static String message(SQLException e) {
        return message(e, null);
    }

    public static String message(SQLException e, String fkHint) {
        String state = e.getSQLState() == null ? "" : e.getSQLState();

        if (state.startsWith("08")) {
            return "Cannot connect to the Derby database.\n"
                 + "Please start the Derby Network Server and try again.";
        }
        if (state.equals("23505")) {
            return "Duplicate record: a record with the same unique value "
                 + "(for example a code, name or number) already exists.";
        }
        if (state.equals("23503")) {
            return fkHint != null ? fkHint
                 : "This record is linked to other records. Remove the related records first.";
        }
        return "Database error: " + e.getMessage();
    }

    public static void show(Component parent, String title, SQLException e) {
        show(parent, title, e, null);
    }

    public static void show(Component parent, String title, SQLException e, String fkHint) {
        JOptionPane.showMessageDialog(parent, message(e, fkHint), title, JOptionPane.ERROR_MESSAGE);
    }
}