package database;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * One-click database builder. Run this file (Right-click > Run File) once after
 * starting the Derby Network Server. It runs sql/schema.sql: creates the tables
 * and loads the sample data. Running it again resets the data.
 */
public class DatabaseSetup {

    public static void main(String[] args) {
        String path = args.length > 0 ? args[0] : "sql/schema.sql";
        try {
            run(path);
            System.out.println("Database setup complete.");
        } catch (IOException e) {
            System.err.println("Cannot read " + path + ": " + e.getMessage());
            System.err.println("Run this from the project folder (NetBeans does this by default).");
        } catch (SQLException e) {
            System.err.println("Setup failed: " + e.getMessage());
            System.err.println("Is the Derby Network Server running?");
        }
    }

    public static void run(String path) throws IOException, SQLException {
        List<String> statements = readStatements(path);
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            for (String sql : statements) {
                try {
                    st.execute(sql);
                } catch (SQLException e) {
                    // The first run has nothing to drop, so DROP errors are expected.
                    if (!sql.toUpperCase().startsWith("DROP")) {
                        throw e;
                    }
                }
            }
            System.out.println("Executed " + statements.size() + " statements.");
        }
    }

    /** Splits the script on semicolons and ignores -- comment lines. */
    private static List<String> readStatements(String path) throws IOException {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            current.append(line).append('\n');
            if (trimmed.endsWith(";")) {
                String sql = current.toString().trim();
                result.add(sql.substring(0, sql.length() - 1).trim());
                current.setLength(0);
            }
        }
        return result;
    }
}
