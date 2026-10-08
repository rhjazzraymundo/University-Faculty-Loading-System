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

    // =========================
    // ADD SUBJECT
    // =========================

    public boolean addSubject(Subject subject) {

        String sql = "INSERT INTO subjects "
                   + "(subject_code, subject_title, units, department_id) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, subject.getSubjectCode());
            stmt.setString(2, subject.getSubjectTitle());
            stmt.setInt(3, subject.getUnits());
            stmt.setInt(4, subject.getDepartmentId());

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // =========================
    // GET ALL SUBJECTS
    // =========================

    public List<Subject> getAllSubjects() {

        List<Subject> subjects = new ArrayList<>();

        String sql = "SELECT subject_id, subject_code, subject_title, "
                   + "units, department_id "
                   + "FROM subjects "
                   + "ORDER BY subject_code";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Subject subject = new Subject();

                subject.setSubjectId(
                        rs.getInt("subject_id")
                );

                subject.setSubjectCode(
                        rs.getString("subject_code")
                );

                subject.setSubjectTitle(
                        rs.getString("subject_title")
                );

                subject.setUnits(
                        rs.getInt("units")
                );

                subject.setDepartmentId(
                        rs.getInt("department_id")
                );

                subjects.add(subject);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subjects;
    }


    // =========================
    // SEARCH SUBJECTS
    // =========================

    public List<Subject> searchSubjects(String keyword) {

        List<Subject> subjects = new ArrayList<>();

        String sql = "SELECT subject_id, subject_code, subject_title, "
                   + "units, department_id "
                   + "FROM subjects "
                   + "WHERE subject_code LIKE ? "
                   + "OR subject_title LIKE ? "
                   + "ORDER BY subject_code";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String search = "%" + keyword + "%";

            stmt.setString(1, search);
            stmt.setString(2, search);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    Subject subject = new Subject();

                    subject.setSubjectId(
                            rs.getInt("subject_id")
                    );

                    subject.setSubjectCode(
                            rs.getString("subject_code")
                    );

                    subject.setSubjectTitle(
                            rs.getString("subject_title")
                    );

                    subject.setUnits(
                            rs.getInt("units")
                    );

                    subject.setDepartmentId(
                            rs.getInt("department_id")
                    );

                    subjects.add(subject);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subjects;
    }


    // =========================
    // UPDATE SUBJECT
    // =========================

    public boolean updateSubject(Subject subject) {

        String sql = "UPDATE subjects SET "
                   + "subject_code = ?, "
                   + "subject_title = ?, "
                   + "units = ?, "
                   + "department_id = ? "
                   + "WHERE subject_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, subject.getSubjectCode());
            stmt.setString(2, subject.getSubjectTitle());
            stmt.setInt(3, subject.getUnits());
            stmt.setInt(4, subject.getDepartmentId());
            stmt.setInt(5, subject.getSubjectId());

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // =========================
    // DELETE SUBJECT
    // =========================

    public boolean deleteSubject(int subjectId) {

        String sql = "DELETE FROM subjects WHERE subject_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, subjectId);

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}