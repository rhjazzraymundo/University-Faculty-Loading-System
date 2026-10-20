package dao;

import database.DatabaseConnection;
import model.Schedule;
import model.ScheduleConflict;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;


public class ScheduleDAO {

    /** Sorts days Monday to Saturday instead of alphabetically. */
    private static final String DAY_ORDER =
            "CASE day_of_week WHEN 'Monday' THEN 1 WHEN 'Tuesday' THEN 2 WHEN 'Wednesday' THEN 3 "
          + "WHEN 'Thursday' THEN 4 WHEN 'Friday' THEN 5 WHEN 'Saturday' THEN 6 ELSE 7 END";

    /** An unassigned faculty (id 0 or less) is stored as NULL so the foreign key stays valid. */
    private static void setFaculty(PreparedStatement ps, int index, int facultyId) throws SQLException {
        if (facultyId <= 0) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, facultyId);
    }

    // CREATE
    public boolean addSchedule(Schedule schedule) throws SQLException {
        String sql = "INSERT INTO schedules "
                   + "(subject_id, faculty_id, room_id, section, term, day_of_week, start_time, end_time) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, schedule.getSubjectId());
            setFaculty(ps, 2, schedule.getFacultyId());
            ps.setInt(3, schedule.getRoomId());
            ps.setString(4, schedule.getSection());
            ps.setString(5, schedule.getTerm());
            ps.setString(6, schedule.getDayOfWeek());
            ps.setTime(7, schedule.getStartTime());
            ps.setTime(8, schedule.getEndTime());
            return ps.executeUpdate() > 0;
        }
    }

    // READ / SEARCH 
    public List<Object[]> getScheduleRows(String keyword) throws SQLException {
        boolean all = keyword == null || keyword.trim().isEmpty();
        String sql = "SELECT s.schedule_id, s.section, s.term, s.day_of_week, s.start_time, s.end_time, "
                   + "       sub.subject_code, sub.units, f.first_name, f.last_name, r.room_name "
                   + "FROM schedules s "
                   + "INNER JOIN subjects sub ON s.subject_id = sub.subject_id "
                   + "LEFT JOIN faculty f ON s.faculty_id = f.faculty_id "
                   + "INNER JOIN rooms r ON s.room_id = r.room_id "
                   + (all ? "" : "WHERE LOWER(sub.subject_code) LIKE ? OR LOWER(s.section) LIKE ? "
                               + "OR LOWER(r.room_name) LIKE ? ")
                   + "ORDER BY " + DAY_ORDER + ", s.start_time";

        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (!all) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String faculty = rs.getString("first_name") != null
                            ? rs.getString("first_name") + " " + rs.getString("last_name")
                            : "Unassigned";
                    rows.add(new Object[]{
                        rs.getInt("schedule_id"), rs.getString("subject_code"), rs.getInt("units"),
                        faculty, rs.getString("room_name"), rs.getString("section"), rs.getString("term"),
                        rs.getString("day_of_week"), rs.getTime("start_time"), rs.getTime("end_time")
                    });
                }
            }
        }
        return rows;
    }

    // =====================================================================
    // UPDATE
    // =====================================================================
    public boolean updateSchedule(Schedule schedule) throws SQLException {
        String sql = "UPDATE schedules SET subject_id = ?, faculty_id = ?, room_id = ?, section = ?, "
                   + "term = ?, day_of_week = ?, start_time = ?, end_time = ? WHERE schedule_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, schedule.getSubjectId());
            setFaculty(ps, 2, schedule.getFacultyId());
            ps.setInt(3, schedule.getRoomId());
            ps.setString(4, schedule.getSection());
            ps.setString(5, schedule.getTerm());
            ps.setString(6, schedule.getDayOfWeek());
            ps.setTime(7, schedule.getStartTime());
            ps.setTime(8, schedule.getEndTime());
            ps.setInt(9, schedule.getScheduleId());
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean deleteSchedule(int scheduleId) throws SQLException {
        String sql = "DELETE FROM schedules WHERE schedule_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, scheduleId);
            return ps.executeUpdate() > 0;
        }
    }

    // TEACHING LOAD CALCULATIONS
    public int getTotalAssignedUnits(int facultyId, String term, int excludeScheduleId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(sub.units), 0) AS total_units "
                   + "FROM schedules s "
                   + "INNER JOIN subjects sub ON s.subject_id = sub.subject_id "
                   + "WHERE s.faculty_id = ? AND s.term = ? AND s.schedule_id <> ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            ps.setString(2, term);
            ps.setInt(3, excludeScheduleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("total_units") : 0;
            }
        }
    }

    public double getTotalAssignedHours(int facultyId, String term, int excludeScheduleId) throws SQLException {
        String sql = "SELECT start_time, end_time FROM schedules "
                   + "WHERE faculty_id = ? AND term = ? AND schedule_id <> ?";
        double totalHours = 0.0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            ps.setString(2, term);
            ps.setInt(3, excludeScheduleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time start = rs.getTime("start_time");
                    Time end = rs.getTime("end_time");
                    if (start != null && end != null) {
                        totalHours += (end.getTime() - start.getTime()) / (1000.0 * 60 * 60);
                    }
                }
            }
        }
        return totalHours;
    }

    public int getSubjectUnits(int subjectId) throws SQLException {
        String sql = "SELECT units FROM subjects WHERE subject_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("units") : 0;
            }
        }
    }

    /** Maximum unit load of a faculty member (24 if the faculty row is not found). */
    public int getFacultyMaxUnits(int facultyId) throws SQLException {
        String sql = "SELECT max_units FROM faculty WHERE faculty_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("max_units") : 24;
            }
        }
    }

    // CONFLICT CHECKS
    public boolean existsDuplicate(Schedule s, int excludeId) throws SQLException {
        String sql = "SELECT schedule_id FROM schedules "
                   + "WHERE subject_id = ? AND section = ? AND TRIM(term) = TRIM(?) "
                   + "AND TRIM(day_of_week) = TRIM(?) AND start_time = ? AND end_time = ? "
                   + "AND schedule_id <> ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getSubjectId());
            ps.setString(2, s.getSection().trim());
            ps.setString(3, s.getTerm().trim());
            ps.setString(4, s.getDayOfWeek().trim());
            ps.setTime(5, s.getStartTime());
            ps.setTime(6, s.getEndTime());
            ps.setInt(7, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public ScheduleConflict findFacultyConflict(Schedule s, int excludeId) throws SQLException {
        if (s.getFacultyId() <= 0) return null;     // unassigned faculty cannot conflict
        String sql = "SELECT sub.subject_code, s.section, r.room_name, s.start_time, s.end_time, "
                   + "       f.first_name, f.last_name "
                   + "FROM schedules s "
                   + "INNER JOIN faculty f ON s.faculty_id = f.faculty_id "
                   + "INNER JOIN subjects sub ON s.subject_id = sub.subject_id "
                   + "INNER JOIN rooms r ON s.room_id = r.room_id "
                   + "WHERE s.faculty_id = ? AND TRIM(s.term) = TRIM(?) AND TRIM(s.day_of_week) = TRIM(?) "
                   + "AND s.schedule_id <> ? AND s.start_time < ? AND s.end_time > ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getFacultyId());
            ps.setString(2, s.getTerm().trim());
            ps.setString(3, s.getDayOfWeek().trim());
            ps.setInt(4, excludeId);
            ps.setTime(5, s.getEndTime());     // existingStart < newEnd
            ps.setTime(6, s.getStartTime());   // existingEnd > newStart
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new ScheduleConflict(rs.getString("subject_code"), rs.getString("section"),
                        rs.getString("room_name"),
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getTime("start_time"), rs.getTime("end_time"));
            }
        }
    }

    public ScheduleConflict findRoomConflict(Schedule s, int excludeId) throws SQLException {
        String sql = "SELECT sub.subject_code, s.section, r.room_name, s.start_time, s.end_time "
                   + "FROM schedules s "
                   + "INNER JOIN rooms r ON s.room_id = r.room_id "
                   + "INNER JOIN subjects sub ON s.subject_id = sub.subject_id "
                   + "WHERE s.room_id = ? AND TRIM(s.term) = TRIM(?) AND TRIM(s.day_of_week) = TRIM(?) "
                   + "AND s.schedule_id <> ? AND s.start_time < ? AND s.end_time > ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getRoomId());
            ps.setString(2, s.getTerm().trim());
            ps.setString(3, s.getDayOfWeek().trim());
            ps.setInt(4, excludeId);
            ps.setTime(5, s.getEndTime());
            ps.setTime(6, s.getStartTime());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new ScheduleConflict(rs.getString("subject_code"), rs.getString("section"),
                        rs.getString("room_name"), null,
                        rs.getTime("start_time"), rs.getTime("end_time"));
            }
        }
    }

    public ScheduleConflict findSectionConflict(Schedule s, int excludeId) throws SQLException {
        String sql = "SELECT sub.subject_code, s.section, r.room_name, s.start_time, s.end_time "
                   + "FROM schedules s "
                   + "INNER JOIN subjects sub ON s.subject_id = sub.subject_id "
                   + "INNER JOIN rooms r ON s.room_id = r.room_id "
                   + "WHERE LOWER(s.section) = LOWER(?) AND TRIM(s.term) = TRIM(?) "
                   + "AND TRIM(s.day_of_week) = TRIM(?) "
                   + "AND s.schedule_id <> ? AND s.start_time < ? AND s.end_time > ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getSection().trim());
            ps.setString(2, s.getTerm().trim());
            ps.setString(3, s.getDayOfWeek().trim());
            ps.setInt(4, excludeId);
            ps.setTime(5, s.getEndTime());
            ps.setTime(6, s.getStartTime());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new ScheduleConflict(rs.getString("subject_code"), rs.getString("section"),
                        rs.getString("room_name"), null,
                        rs.getTime("start_time"), rs.getTime("end_time"));
            }
        }
    }
}