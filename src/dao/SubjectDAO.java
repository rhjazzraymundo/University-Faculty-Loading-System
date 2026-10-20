package dao;

import database.DatabaseConnection;
import model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {

    // CREATE
    public boolean addSubject(Subject subject) throws SQLException {
        String sql = "INSERT INTO subjects (subject_code, subject_title, units, department_id) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject.getSubjectCode());
            ps.setString(2, subject.getSubjectTitle());
            ps.setInt(3, subject.getUnits());
            ps.setInt(4, subject.getDepartmentId());
            return ps.executeUpdate() > 0;
        }
    }

    // READ
    public List<Subject> getAllSubjects() throws SQLException {
        String sql = "SELECT subject_id, subject_code, subject_title, units, department_id "
                   + "FROM subjects ORDER BY subject_code";
        List<Subject> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // SEARCH
    public List<Subject> searchSubjects(String keyword) throws SQLException {
        String sql = "SELECT subject_id, subject_code, subject_title, units, department_id "
                   + "FROM subjects WHERE UPPER(subject_code) LIKE ? OR UPPER(subject_title) LIKE ? "
                   + "ORDER BY subject_code";
        String like = "%" + keyword.trim().toUpperCase() + "%";
        List<Subject> list = new ArrayList<>();
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

    // UPDATE
    public boolean updateSubject(Subject subject) throws SQLException {
        String sql = "UPDATE subjects SET subject_code = ?, subject_title = ?, units = ?, "
                   + "department_id = ? WHERE subject_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject.getSubjectCode());
            ps.setString(2, subject.getSubjectTitle());
            ps.setInt(3, subject.getUnits());
            ps.setInt(4, subject.getDepartmentId());
            ps.setInt(5, subject.getSubjectId());
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE (fails with SQLState 23503 if the subject is scheduled)
    public boolean deleteSubject(int subjectId) throws SQLException {
        String sql = "DELETE FROM subjects WHERE subject_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            return ps.executeUpdate() > 0;
        }
    }

    private Subject map(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setSubjectId(rs.getInt("subject_id"));
        s.setSubjectCode(rs.getString("subject_code"));
        s.setSubjectTitle(rs.getString("subject_title"));
        s.setUnits(rs.getInt("units"));
        s.setDepartmentId(rs.getInt("department_id"));
        return s;
    }
}