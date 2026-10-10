package dao;

import database.DatabaseConnection;
import model.Schedule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {

    /** Sorts days Monday to Saturday instead of alphabetically. */
    private static final String DAY_ORDER =
            "CASE day_of_week WHEN 'Monday' THEN 1 WHEN 'Tuesday' THEN 2 WHEN 'Wednesday' THEN 3 "
          + "WHEN 'Thursday' THEN 4 WHEN 'Friday' THEN 5 WHEN 'Saturday' THEN 6 ELSE 7 END";

    /** An unassigned faculty (id 0 or less) is stored as NULL so the foreign key stays valid. */
    private static void setFaculty(PreparedStatement pst, int index, int facultyId) throws SQLException {
        if (facultyId <= 0) pst.setNull(index, Types.INTEGER);
        else pst.setInt(index, facultyId);
    }

    // ADD
    public boolean addSchedule(Schedule schedule) {

        String sql = "INSERT INTO schedules "
                + "(subject_id, faculty_id, room_id, section, term, "
                + "day_of_week, start_time, end_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, schedule.getSubjectId());
            setFaculty(pst, 2, schedule.getFacultyId());
            pst.setInt(3, schedule.getRoomId());
            pst.setString(4, schedule.getSection());
            pst.setString(5, schedule.getTerm());
            pst.setString(6, schedule.getDayOfWeek());
            pst.setTime(7, schedule.getStartTime());
            pst.setTime(8, schedule.getEndTime());

            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error adding schedule: " + e.getMessage());
            return false;
        }
    }

    // GET ALL
    public List<Schedule> getAllSchedules() {

        List<Schedule> schedules = new ArrayList<>();

        String sql = "SELECT schedule_id, subject_id, faculty_id, "
                + "room_id, section, term, day_of_week, start_time, end_time "
                + "FROM schedules "
                + "ORDER BY "
                + DAY_ORDER + ", start_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {

                Schedule schedule = new Schedule();

                schedule.setScheduleId(rs.getInt("schedule_id"));
                schedule.setSubjectId(rs.getInt("subject_id"));
                schedule.setFacultyId(rs.getInt("faculty_id"));
                schedule.setRoomId(rs.getInt("room_id"));
                schedule.setSection(rs.getString("section"));
                schedule.setTerm(rs.getString("term"));
                schedule.setDayOfWeek(rs.getString("day_of_week"));
                schedule.setStartTime(rs.getTime("start_time"));
                schedule.setEndTime(rs.getTime("end_time"));

                schedules.add(schedule);
            }

        } catch (SQLException e) {
            System.out.println("Error loading schedules: " + e.getMessage());
        }

        return schedules;
    }

    // UPDATE
    public boolean updateSchedule(Schedule schedule) {

        String sql = "UPDATE schedules SET "
                + "subject_id = ?, "
                + "faculty_id = ?, "
                + "room_id = ?, "
                + "section = ?, "
                + "term = ?, "
                + "day_of_week = ?, "
                + "start_time = ?, "
                + "end_time = ? "
                + "WHERE schedule_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, schedule.getSubjectId());
            setFaculty(pst, 2, schedule.getFacultyId());
            pst.setInt(3, schedule.getRoomId());
            pst.setString(4, schedule.getSection());
            pst.setString(5, schedule.getTerm());
            pst.setString(6, schedule.getDayOfWeek());
            pst.setTime(7, schedule.getStartTime());
            pst.setTime(8, schedule.getEndTime());
            pst.setInt(9, schedule.getScheduleId());

            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error updating schedule: " + e.getMessage());
            return false;
        }
    }

    // DELETE
    public boolean deleteSchedule(int scheduleId) {

        String sql = "DELETE FROM schedules WHERE schedule_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, scheduleId);

            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting schedule: " + e.getMessage());
            return false;
        }
    }

    // SEARCH
    public List<Schedule> searchSchedules(String keyword) {

        List<Schedule> schedules = new ArrayList<>();

        String sql = "SELECT s.schedule_id, s.subject_id, s.faculty_id, "
                + "s.room_id, s.section, s.term, s.day_of_week, "
                + "s.start_time, s.end_time "
                + "FROM schedules s "
                + "WHERE LOWER(s.section) LIKE ? "
                + "OR LOWER(s.term) LIKE ? "
                + "OR LOWER(s.day_of_week) LIKE ? "
                + "ORDER BY "
                + DAY_ORDER + ", s.start_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            String search = "%" + keyword.toLowerCase() + "%";

            pst.setString(1, search);
            pst.setString(2, search);
            pst.setString(3, search);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                Schedule schedule = new Schedule();

                schedule.setScheduleId(rs.getInt("schedule_id"));
                schedule.setSubjectId(rs.getInt("subject_id"));
                schedule.setFacultyId(rs.getInt("faculty_id"));
                schedule.setRoomId(rs.getInt("room_id"));
                schedule.setSection(rs.getString("section"));
                schedule.setTerm(rs.getString("term"));
                schedule.setDayOfWeek(rs.getString("day_of_week"));
                schedule.setStartTime(rs.getTime("start_time"));
                schedule.setEndTime(rs.getTime("end_time"));

                schedules.add(schedule);
            }

        } catch (SQLException e) {
            System.out.println("Error searching schedules: " + e.getMessage());
        }

        return schedules;
    }
    // ==========================================
    // DAY 6: TEACHING LOAD CALCULATIONS & RULES
    // ==========================================

    /**
     * Calculates total units currently assigned to a faculty in a given term.
     * excludeScheduleId is used when updating so the current record isn't double-counted (pass -1 when adding).
     */
    public int getTotalAssignedUnits(int facultyId, String term, int excludeScheduleId) {
        String sql = "SELECT COALESCE(SUM(sub.units), 0) AS total_units "
                   + "FROM schedules s "
                   + "INNER JOIN subjects sub ON s.subject_id = sub.subject_id "
                   + "WHERE s.faculty_id = ? AND s.term = ? AND s.schedule_id <> ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, facultyId);
            pst.setString(2, term);
            pst.setInt(3, excludeScheduleId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_units");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating faculty units: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Calculates total weekly hours assigned to a faculty in a given term.
     */
    public double getTotalAssignedHours(int facultyId, String term, int excludeScheduleId) {
        String sql = "SELECT s.start_time, s.end_time "
                   + "FROM schedules s "
                   + "WHERE s.faculty_id = ? AND s.term = ? AND s.schedule_id <> ?";

        double totalHours = 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, facultyId);
            pst.setString(2, term);
            pst.setInt(3, excludeScheduleId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Time start = rs.getTime("start_time");
                    Time end = rs.getTime("end_time");
                    if (start != null && end != null) {
                        long diffMillis = end.getTime() - start.getTime();
                        totalHours += diffMillis / (1000.0 * 60 * 60);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating faculty hours: " + e.getMessage());
        }
        return totalHours;
    }

    /**
     * Gets the number of units for a specific subject.
     */
    public int getSubjectUnits(int subjectId) {
        String sql = "SELECT units FROM subjects WHERE subject_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, subjectId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("units");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching subject units: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Gets the maximum unit load allowed for a faculty member.
     */
    public int getFacultyMaxUnits(int facultyId) {
        String sql = "SELECT max_units FROM faculty WHERE faculty_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, facultyId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("max_units");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching faculty max units: " + e.getMessage());
        }
        return 24; // Default fallback
    }
}