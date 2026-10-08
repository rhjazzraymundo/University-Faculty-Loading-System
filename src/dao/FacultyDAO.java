package dao;

import database.DatabaseConnection;
import model.Faculty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FacultyDAO {

    // =========================
    // CREATE
    // =========================

    public boolean addFaculty(Faculty faculty) throws SQLException {

        String sql = "INSERT INTO faculty "
                   + "(employee_no, first_name, last_name, department_id, "
                   + "email, contact_no, employment_type, max_units, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, faculty.getEmployeeNo());
            ps.setString(2, faculty.getFirstName());
            ps.setString(3, faculty.getLastName());
            ps.setInt(4, faculty.getDepartmentId());
            ps.setString(5, faculty.getEmail());
            ps.setString(6, faculty.getContactNo());
            ps.setString(7, faculty.getEmploymentType());
            ps.setInt(8, faculty.getMaxUnits());
            ps.setString(9, faculty.getStatus());

            return ps.executeUpdate() > 0;
        }
    }

    // =========================
    // READ
    // =========================

    public List<Faculty> getAllFaculty() throws SQLException {

        List<Faculty> facultyList = new ArrayList<>();

        String sql = "SELECT faculty_id, employee_no, department_id, "
                   + "first_name, last_name, email, contact_no, "
                   + "employment_type, max_units, status "
                   + "FROM faculty "
                   + "ORDER BY last_name, first_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Faculty faculty = new Faculty();

                faculty.setFacultyId(rs.getInt("faculty_id"));
                faculty.setEmployeeNo(rs.getString("employee_no"));
                faculty.setDepartmentId(rs.getInt("department_id"));
                faculty.setFirstName(rs.getString("first_name"));
                faculty.setLastName(rs.getString("last_name"));
                faculty.setEmail(rs.getString("email"));
                faculty.setContactNo(rs.getString("contact_no"));
                faculty.setEmploymentType(
                        rs.getString("employment_type"));
                faculty.setMaxUnits(rs.getInt("max_units"));
                faculty.setStatus(rs.getString("status"));

                facultyList.add(faculty);
            }
        }

        return facultyList;
    }

    // =========================
    // SEARCH
    // =========================

    public List<Faculty> searchFaculty(String keyword)
            throws SQLException {

        List<Faculty> facultyList = new ArrayList<>();

        String sql = "SELECT faculty_id, employee_no, department_id, "
                   + "first_name, last_name, email, contact_no, "
                   + "employment_type, max_units, status "
                   + "FROM faculty "
                   + "WHERE LOWER(employee_no) LIKE LOWER(?) "
                   + "OR LOWER(first_name) LIKE LOWER(?) "
                   + "OR LOWER(last_name) LIKE LOWER(?) "
                   + "OR LOWER(email) LIKE LOWER(?) "
                   + "ORDER BY last_name, first_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String search = "%" + keyword + "%";

            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);
            ps.setString(4, search);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Faculty faculty = new Faculty();

                    faculty.setFacultyId(
                            rs.getInt("faculty_id"));

                    faculty.setEmployeeNo(
                            rs.getString("employee_no"));

                    faculty.setDepartmentId(
                            rs.getInt("department_id"));

                    faculty.setFirstName(
                            rs.getString("first_name"));

                    faculty.setLastName(
                            rs.getString("last_name"));

                    faculty.setEmail(
                            rs.getString("email"));

                    faculty.setContactNo(
                            rs.getString("contact_no"));

                    faculty.setEmploymentType(
                            rs.getString("employment_type"));

                    faculty.setMaxUnits(
                            rs.getInt("max_units"));

                    faculty.setStatus(
                            rs.getString("status"));

                    facultyList.add(faculty);
                }
            }
        }

        return facultyList;
    }

    // =========================
    // UPDATE
    // =========================

    public boolean updateFaculty(Faculty faculty)
            throws SQLException {

        String sql = "UPDATE faculty SET "
                   + "employee_no = ?, "
                   + "first_name = ?, "
                   + "last_name = ?, "
                   + "department_id = ?, "
                   + "email = ?, "
                   + "contact_no = ?, "
                   + "employment_type = ?, "
                   + "max_units = ?, "
                   + "status = ? "
                   + "WHERE faculty_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, faculty.getEmployeeNo());
            ps.setString(2, faculty.getFirstName());
            ps.setString(3, faculty.getLastName());
            ps.setInt(4, faculty.getDepartmentId());
            ps.setString(5, faculty.getEmail());
            ps.setString(6, faculty.getContactNo());
            ps.setString(7, faculty.getEmploymentType());
            ps.setInt(8, faculty.getMaxUnits());
            ps.setString(9, faculty.getStatus());
            ps.setInt(10, faculty.getFacultyId());

            return ps.executeUpdate() > 0;
        }
    }

    // =========================
    // DELETE
    // =========================

    public boolean deleteFaculty(int facultyId)
            throws SQLException {

        String sql = "DELETE FROM faculty WHERE faculty_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facultyId);

            return ps.executeUpdate() > 0;
        }
    }
}