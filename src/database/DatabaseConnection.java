package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


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
