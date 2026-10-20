package dao;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO {

    /**
     * Hours a room can be used in one week: classes may be scheduled from 7:00 AM to
     * 9:00 PM (14 hours) on Monday to Saturday (6 days), as allowed by the time lists
     * in ScheduleFrame. Used as the 100% mark of the Room Utilization report.
     */
    public static final double WEEKLY_AVAILABLE_HOURS = 14.0 * 6;


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

    /**
     * Room utilization for a term (LEFT JOIN of rooms and schedules, so rooms with no
     * class still appear).
     */
    public List<Object[]> getRoomUtilization(String term) throws SQLException {
        String sql = "SELECT r.room_id, r.room_name, r.building, r.room_type, r.capacity, "
                   + "       s.start_time, s.end_time "
                   + "FROM rooms r "
                   + "LEFT JOIN schedules s ON r.room_id = s.room_id AND s.term = ? "
                   + "ORDER BY r.room_name";

        // One entry per room; the schedule rows of a room are added up in Java
        // (same approach as weeklyHours above, because Derby has no simple time difference).
        Map<Integer, Object[]> rooms = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int roomId = rs.getInt("room_id");
                    Object[] row = rooms.get(roomId);
                    if (row == null) {
                        String building = rs.getString("building");
                        row = new Object[]{
                            rs.getString("room_name"),
                            building == null ? "" : building,
                            rs.getString("room_type"),
                            rs.getInt("capacity"),
                            0,      // classes per week
                            0.0     // booked hours per week
                        };
                        rooms.put(roomId, row);
                    }
                    Time start = rs.getTime("start_time");
                    Time end = rs.getTime("end_time");
                    if (start != null && end != null) {   // null when the room has no class
                        row[4] = (Integer) row[4] + 1;
                        row[5] = (Double) row[5] + (end.getTime() - start.getTime()) / (1000.0 * 60 * 60);
                    }
                }
            }
        }
        return new ArrayList<>(rooms.values());
    }
}