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

    public List<Department> getAllDepartments() {

        List<Department> departments = new ArrayList<>();

        String sql = "SELECT department_id, department_code, department_name "
                   + "FROM departments "
                   + "ORDER BY department_code";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Department department = new Department();

                department.setDepartmentId(
                        rs.getInt("department_id")
                );

                department.setDepartmentCode(
                        rs.getString("department_code")
                );

                department.setDepartmentName(
                        rs.getString("department_name")
                );

                departments.add(department);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return departments;
    }
    
    public boolean addDepartment(Department department) throws SQLException {
        String sql = "INSERT INTO departments (department_code, department_name) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, department.getDepartmentCode().trim());
            stmt.setString(2, department.getDepartmentName().trim());

            return stmt.executeUpdate() > 0;
        }
    }
}