package dao;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Read-only queries for the Reports screen. Each row is ready to add to a JTable. */
public class ReportDAO {

    /**
     * Faculty load summary for a term (JOIN of faculty, departments, schedules and subjects).
     * Row: employee no, name, department code, employment type,
     *      assigned units (Integer), max units (Integer), weekly hours (Double)
     */
    public List<Object[]> getFacultyLoadSummary(String term) throws SQLException {
        String sql = "SELECT f.faculty_id, f.employee_no, f.first_name, f.last_name, "
                   + "       d.department_code, f.employment_type, f.max_units, "
                   + "       COALESCE(SUM(sub.units), 0) AS total_units "
                   + "FROM faculty f "
                   + "INNER JOIN departments d ON f.department_id = d.department_id "
                   + "LEFT JOIN schedules s ON f.faculty_id = s.faculty_id AND s.term = ? "
                   + "LEFT JOIN subjects sub ON s.subject_id = sub.subject_id "
                   + "WHERE f.status = 'Active' "
                   + "GROUP BY f.faculty_id, f.employee_no, f.first_name, f.last_name, "
                   + "         d.department_code, f.employment_type, f.max_units "
                   + "ORDER BY d.department_code, f.last_name";

        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            Map<Integer, Double> hours = weeklyHours(conn, term);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, term);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int facultyId = rs.getInt("faculty_id");
                        rows.add(new Object[]{
                            rs.getString("employee_no"),
                            rs.getString("first_name") + " " + rs.getString("last_name"),
                            rs.getString("department_code"),
                            rs.getString("employment_type"),
                            rs.getInt("total_units"),
                            rs.getInt("max_units"),
                            hours.getOrDefault(facultyId, 0.0)
                        });
                    }
                }
            }
        }
        return rows;
    }

    /** Total weekly hours per faculty id for a term. */
    private Map<Integer, Double> weeklyHours(Connection conn, String term) throws SQLException {
        String sql = "SELECT faculty_id, start_time, end_time FROM schedules "
                   + "WHERE term = ? AND faculty_id IS NOT NULL";
        Map<Integer, Double> hours = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time start = rs.getTime("start_time");
                    Time end = rs.getTime("end_time");
                    if (start != null && end != null) {
                        double h = (end.getTime() - start.getTime()) / (1000.0 * 60 * 60);
                        hours.merge(rs.getInt("faculty_id"), h, Double::sum);
                    }
                }
            }
        }
        return hours;
    }

    /**
     * Weekly timetable of one instructor for a term (JOIN of schedules, subjects and rooms).
     * Row: day, time slot, subject code, subject title, units, section, room
     */
    public List<Object[]> getFacultyTimetable(int facultyId, String term) throws SQLException {
        String sql = "SELECT s.day_of_week, s.start_time, s.end_time, "
                   + "       sub.subject_code, sub.subject_title, sub.units, s.section, r.room_name "
                   + "FROM schedules s "
                   + "INNER JOIN subjects sub ON s.subject_id = sub.subject_id "
                   + "INNER JOIN rooms r ON s.room_id = r.room_id "
                   + "WHERE s.faculty_id = ? AND s.term = ? "
                   + "ORDER BY CASE s.day_of_week WHEN 'Monday' THEN 1 WHEN 'Tuesday' THEN 2 "
                   + "WHEN 'Wednesday' THEN 3 WHEN 'Thursday' THEN 4 WHEN 'Friday' THEN 5 "
                   + "WHEN 'Saturday' THEN 6 ELSE 7 END, s.start_time";

        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            ps.setString(2, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getString("day_of_week"),
                        rs.getTime("start_time") + " - " + rs.getTime("end_time"),
                        rs.getString("subject_code"),
                        rs.getString("subject_title"),
                        rs.getInt("units"),
                        rs.getString("section"),
                        rs.getString("room_name")
                    });
                }
            }
        }
        return rows;
    }
}