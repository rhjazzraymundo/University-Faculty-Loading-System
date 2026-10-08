package dao;

import database.DatabaseConnection;
import model.Schedule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {

    // ADD
    public boolean addSchedule(Schedule schedule) {

        String sql = "INSERT INTO schedules "
                + "(subject_id, faculty_id, room_id, section, term, "
                + "day_of_week, start_time, end_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, schedule.getSubjectId());
            pst.setInt(2, schedule.getFacultyId());
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
                + "ORDER BY day_of_week, start_time";

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
            pst.setInt(2, schedule.getFacultyId());
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
                + "ORDER BY s.day_of_week, s.start_time";

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
}