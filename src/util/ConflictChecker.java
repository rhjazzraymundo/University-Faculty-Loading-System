package util;

import database.DatabaseConnection;
import model.Schedule;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

/**
 * Validates scheduling constraints and detects time overlaps.
 * Timeline Conflict Rule:
 * Two time blocks overlap on the same day when:
 * existingStart < newEnd AND existingEnd > newStart
 */
public class ConflictChecker {

    public static boolean hasConflict(Component parent, Schedule schedule, int excludeScheduleId) {
        
        // 1. Exact Duplicate Check
        if (checkDuplicate(parent, schedule, excludeScheduleId)) {
            return true;
        }

        // 2. Faculty Conflict Check
        if (checkFacultyConflict(parent, schedule, excludeScheduleId)) {
            return true;
        }

        // 3. Room Conflict Check
        if (checkRoomConflict(parent, schedule, excludeScheduleId)) {
            return true;
        }

        // 4. Section Conflict Check
        if (checkSectionConflict(parent, schedule, excludeScheduleId)) {
            return true;
        }

        return false;
    }

    // =========================================================================
    // 1. EXACT DUPLICATE CHECK
    // =========================================================================
    private static boolean checkDuplicate(Component parent, Schedule schedule, int excludeId) {
        String sql = "SELECT s.schedule_id "
                   + "FROM schedules s "
                   + "WHERE s.subject_id = ? "
                   + "  AND s.section = ? "
                   + "  AND TRIM(s.term) = TRIM(?) "
                   + "  AND TRIM(s.day_of_week) = TRIM(?) "
                   + "  AND s.start_time = ? AND s.end_time = ? "
                   + "  AND s.schedule_id <> ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, schedule.getSubjectId());
            ps.setString(2, schedule.getSection().trim());
            ps.setString(3, schedule.getTerm().trim());
            ps.setString(4, schedule.getDayOfWeek().trim());
            ps.setTime(5, schedule.getStartTime());
            ps.setTime(6, schedule.getEndTime());
            ps.setInt(7, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(parent,
                            "Duplicate Schedule Error!\n\n"
                            + "This section is already scheduled for this subject on "
                            + schedule.getDayOfWeek() + " at the same time for " + schedule.getTerm() + ".",
                            "Duplicate Entry",
                            JOptionPane.ERROR_MESSAGE);
                    return true;
                }
            }
        } catch (SQLException e) {
            return dbError(parent, "duplicate", e);
        }
        return false;
    }

    // =========================================================================
    // 2. FACULTY CONFLICT CHECK
    // Overlap: existingStart < newEnd AND existingEnd > newStart
    // =========================================================================
    private static boolean checkFacultyConflict(Component parent, Schedule schedule, int excludeId) {
        if (schedule.getFacultyId() <= 0) {
            return false;
        }

        String sql = "SELECT sub.subject_code, s.section, r.room_name, "
                   + "       s.start_time, s.end_time, "
                   + "       f.first_name, f.last_name "
                   + "FROM schedules s "
                   + "INNER JOIN faculty f ON s.faculty_id = f.faculty_id "
                   + "INNER JOIN subjects sub ON s.subject_id = sub.subject_id "
                   + "INNER JOIN rooms r ON s.room_id = r.room_id "
                   + "WHERE s.faculty_id = ? "
                   + "  AND TRIM(s.term) = TRIM(?) "
                   + "  AND TRIM(s.day_of_week) = TRIM(?) "
                   + "  AND s.schedule_id <> ? "
                   + "  AND s.start_time < ? "
                   + "  AND s.end_time > ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, schedule.getFacultyId());
            ps.setString(2, schedule.getTerm().trim());
            ps.setString(3, schedule.getDayOfWeek().trim());
            ps.setInt(4, excludeId);
            ps.setTime(5, schedule.getEndTime());   // existingStart < newEnd
            ps.setTime(6, schedule.getStartTime()); // existingEnd > newStart

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String instructor = rs.getString("first_name") + " " + rs.getString("last_name");
                    String conflictingSubject = rs.getString("subject_code");
                    String conflictingSection = rs.getString("section");
                    String conflictingRoom = rs.getString("room_name");
                    Time existStart = rs.getTime("start_time");
                    Time existEnd = rs.getTime("end_time");

                    JOptionPane.showMessageDialog(parent,
                            "Faculty Conflict Detected!\n\n"
                            + "Instructor " + instructor + " is already teaching:\n"
                            + "• Class: " + conflictingSubject + " (" + conflictingSection + ")\n"
                            + "• Room: " + conflictingRoom + "\n"
                            + "• Time: " + existStart + " - " + existEnd + "\n"
                            + "• Day: " + schedule.getDayOfWeek() + "\n\n"
                            + "An instructor cannot be scheduled in two classes at the same time.",
                            "Schedule Conflict",
                            JOptionPane.WARNING_MESSAGE);
                    return true;
                }
            }
        } catch (SQLException e) {
            return dbError(parent, "faculty conflict", e);
        }
        return false;
    }

    // =========================================================================
    // 3. ROOM CONFLICT CHECK
    // =========================================================================
    private static boolean checkRoomConflict(Component parent, Schedule schedule, int excludeId) {
        String sql = "SELECT sub.subject_code, s.section, r.room_name, "
                   + "       s.start_time, s.end_time "
                   + "FROM schedules s "
                   + "INNER JOIN rooms r ON s.room_id = r.room_id "
                   + "INNER JOIN subjects sub ON s.subject_id = sub.subject_id "
                   + "WHERE s.room_id = ? "
                   + "  AND TRIM(s.term) = TRIM(?) "
                   + "  AND TRIM(s.day_of_week) = TRIM(?) "
                   + "  AND s.schedule_id <> ? "
                   + "  AND s.start_time < ? "
                   + "  AND s.end_time > ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, schedule.getRoomId());
            ps.setString(2, schedule.getTerm().trim());
            ps.setString(3, schedule.getDayOfWeek().trim());
            ps.setInt(4, excludeId);
            ps.setTime(5, schedule.getEndTime());   // existingStart < newEnd
            ps.setTime(6, schedule.getStartTime()); // existingEnd > newStart

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String roomName = rs.getString("room_name");
                    String conflictingSubject = rs.getString("subject_code");
                    String conflictingSection = rs.getString("section");
                    Time existStart = rs.getTime("start_time");
                    Time existEnd = rs.getTime("end_time");

                    JOptionPane.showMessageDialog(parent,
                            "Room Conflict Detected!\n\n"
                            + "Room \"" + roomName + "\" is already occupied by:\n"
                            + "• Class: " + conflictingSubject + " (" + conflictingSection + ")\n"
                            + "• Time: " + existStart + " - " + existEnd + "\n"
                            + "• Day: " + schedule.getDayOfWeek() + "\n\n"
                            + "Please choose another room or adjust the time slot.",
                            "Schedule Conflict",
                            JOptionPane.WARNING_MESSAGE);
                    return true;
                }
            }
        } catch (SQLException e) {
            return dbError(parent, "room conflict", e);
        }
        return false;
    }

    // =========================================================================
    // 4. SECTION CONFLICT CHECK
    // =========================================================================
    private static boolean checkSectionConflict(Component parent, Schedule schedule, int excludeId) {
        String sql = "SELECT sub.subject_code, r.room_name, "
                   + "       s.start_time, s.end_time "
                   + "FROM schedules s "
                   + "INNER JOIN subjects sub ON s.subject_id = sub.subject_id "
                   + "INNER JOIN rooms r ON s.room_id = r.room_id "
                   + "WHERE LOWER(s.section) = LOWER(?) "
                   + "  AND TRIM(s.term) = TRIM(?) "
                   + "  AND TRIM(s.day_of_week) = TRIM(?) "
                   + "  AND s.schedule_id <> ? "
                   + "  AND s.start_time < ? "
                   + "  AND s.end_time > ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, schedule.getSection().trim());
            ps.setString(2, schedule.getTerm().trim());
            ps.setString(3, schedule.getDayOfWeek().trim());
            ps.setInt(4, excludeId);
            ps.setTime(5, schedule.getEndTime());   // existingStart < newEnd
            ps.setTime(6, schedule.getStartTime()); // existingEnd > newStart

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String conflictingSubject = rs.getString("subject_code");
                    String conflictingRoom = rs.getString("room_name");
                    Time existStart = rs.getTime("start_time");
                    Time existEnd = rs.getTime("end_time");

                    JOptionPane.showMessageDialog(parent,
                            "Section Conflict Detected!\n\n"
                            + "Section \"" + schedule.getSection() + "\" already has another class scheduled:\n"
                            + "• Class: " + conflictingSubject + "\n"
                            + "• Room: " + conflictingRoom + "\n"
                            + "• Time: " + existStart + " - " + existEnd + "\n"
                            + "• Day: " + schedule.getDayOfWeek() + "\n\n"
                            + "Students in this section cannot attend two classes at the same time.",
                            "Schedule Conflict",
                            JOptionPane.WARNING_MESSAGE);
                    return true;
                }
            }
        } catch (SQLException e) {
            return dbError(parent, "section conflict", e);
        }
        return false;
    }

    /** If a check cannot run, block the save so a conflict is never missed silently. */
    private static boolean dbError(Component parent, String what, SQLException e) {
        System.err.println("Error checking " + what + ": " + e.getMessage());
        JOptionPane.showMessageDialog(parent,
                "Could not verify " + what + " because of a database error.\n"
                + "The schedule was NOT saved.\n\n" + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        return true;
    }
}
