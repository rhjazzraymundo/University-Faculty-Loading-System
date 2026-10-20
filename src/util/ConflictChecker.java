package util;

import dao.ScheduleDAO;
import model.Schedule;
import model.ScheduleConflict;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.sql.SQLException;


public class ConflictChecker {

    private static final ScheduleDAO scheduleDAO = new ScheduleDAO();

    
    public static boolean hasConflict(Component parent, Schedule schedule, int excludeScheduleId) {
        try {
            // 1. Exact duplicate
            if (scheduleDAO.existsDuplicate(schedule, excludeScheduleId)) {
                JOptionPane.showMessageDialog(parent,
                        "Duplicate Schedule Error!\n\n"
                        + "This section is already scheduled for this subject on "
                        + schedule.getDayOfWeek() + " at the same time for " + schedule.getTerm() + ".",
                        "Duplicate Entry", JOptionPane.ERROR_MESSAGE);
                return true;
            }

            // 2. Faculty conflict
            ScheduleConflict c = scheduleDAO.findFacultyConflict(schedule, excludeScheduleId);
            if (c != null) {
                JOptionPane.showMessageDialog(parent,
                        "Faculty Conflict Detected!\n\n"
                        + "Instructor " + c.getInstructor() + " is already teaching:\n"
                        + "\u2022 Class: " + c.getSubjectCode() + " (" + c.getSection() + ")\n"
                        + "\u2022 Room: " + c.getRoomName() + "\n"
                        + "\u2022 Time: " + c.getStartTime() + " - " + c.getEndTime() + "\n"
                        + "\u2022 Day: " + schedule.getDayOfWeek() + "\n\n"
                        + "An instructor cannot be scheduled in two classes at the same time.",
                        "Schedule Conflict", JOptionPane.WARNING_MESSAGE);
                return true;
            }

            // 3. Room conflict
            c = scheduleDAO.findRoomConflict(schedule, excludeScheduleId);
            if (c != null) {
                JOptionPane.showMessageDialog(parent,
                        "Room Conflict Detected!\n\n"
                        + "Room \"" + c.getRoomName() + "\" is already occupied by:\n"
                        + "\u2022 Class: " + c.getSubjectCode() + " (" + c.getSection() + ")\n"
                        + "\u2022 Time: " + c.getStartTime() + " - " + c.getEndTime() + "\n"
                        + "\u2022 Day: " + schedule.getDayOfWeek() + "\n\n"
                        + "Please choose another room or adjust the time slot.",
                        "Schedule Conflict", JOptionPane.WARNING_MESSAGE);
                return true;
            }

            // 4. Section conflict
            c = scheduleDAO.findSectionConflict(schedule, excludeScheduleId);
            if (c != null) {
                JOptionPane.showMessageDialog(parent,
                        "Section Conflict Detected!\n\n"
                        + "Section \"" + schedule.getSection() + "\" already has another class scheduled:\n"
                        + "\u2022 Class: " + c.getSubjectCode() + "\n"
                        + "\u2022 Room: " + c.getRoomName() + "\n"
                        + "\u2022 Time: " + c.getStartTime() + " - " + c.getEndTime() + "\n"
                        + "\u2022 Day: " + schedule.getDayOfWeek() + "\n\n"
                        + "Students in this section cannot attend two classes at the same time.",
                        "Schedule Conflict", JOptionPane.WARNING_MESSAGE);
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parent,
                    "Could not verify the schedule because of a database error.\n"
                    + "The schedule was NOT saved.\n\n" + DbErrors.message(e),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return true;
        }
        return false;
    }
}