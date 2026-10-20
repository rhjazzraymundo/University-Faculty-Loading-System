package dao;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardDAO {

    public int countFaculty() throws SQLException {
        return count("SELECT COUNT(*) FROM faculty");
    }

    public int countSubjects() throws SQLException {
        return count("SELECT COUNT(*) FROM subjects");
    }

    public int countDepartments() throws SQLException {
        return count("SELECT COUNT(*) FROM departments");
    }

    public int countRooms() throws SQLException {
        return count("SELECT COUNT(*) FROM rooms");
    }

    private int count(String sql) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}