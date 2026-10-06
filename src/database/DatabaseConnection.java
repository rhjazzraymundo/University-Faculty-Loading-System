package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Single place for JDBC connection settings (see requirement sections 17-18).
 * Day 2: run the Derby Network Server first, then test with Main.
 */
public class DatabaseConnection {

    private static final String URL =
        "jdbc:derby://localhost:1527/FacultyLoadingDB;create=true";
    private static final String USER = "app";
    private static final String PASSWORD = "app";

    private DatabaseConnection() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
