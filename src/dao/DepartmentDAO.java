package dao;

import database.DatabaseConnection;
import model.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    public List<Department> getAllDepartments() throws SQLException {
        String sql = "SELECT department_id, department_code, department_name "
                   + "FROM departments ORDER BY department_code";
        List<Department> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Department> searchDepartments(String keyword) throws SQLException {
        String sql = "SELECT department_id, department_code, department_name FROM departments "
                   + "WHERE UPPER(department_code) LIKE ? OR UPPER(department_name) LIKE ? "
                   + "ORDER BY department_code";
        String like = "%" + keyword.trim().toUpperCase() + "%";
        List<Department> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public boolean addDepartment(Department department) throws SQLException {
        String sql = "INSERT INTO departments (department_code, department_name) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, department.getDepartmentCode().trim());
            ps.setString(2, department.getDepartmentName().trim());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateDepartment(Department department) throws SQLException {
        String sql = "UPDATE departments SET department_code = ?, department_name = ? "
                   + "WHERE department_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, department.getDepartmentCode().trim());
            ps.setString(2, department.getDepartmentName().trim());
            ps.setInt(3, department.getDepartmentId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteDepartment(int departmentId) throws SQLException {
        String sql = "DELETE FROM departments WHERE department_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            return ps.executeUpdate() > 0;
        }
    }

    private Department map(ResultSet rs) throws SQLException {
        return new Department(rs.getInt("department_id"),
                rs.getString("department_code"), rs.getString("department_name"));
    }
}