package ui;

import dao.FacultyDAO;
import dao.RoomDAO;
import dao.ScheduleDAO;
import dao.SubjectDAO;
import model.Faculty;
import model.Room;
import model.Schedule;
import model.Subject;
import util.ConflictChecker;
import util.DbErrors;
import util.LoadCalculator;
import util.WorkingHours;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class ScheduleFrame extends JFrame {

    private JComboBox<String> cmbSubject;
    private JComboBox<String> cmbFaculty;
    private JComboBox<String> cmbRoom;
    private JTextField txtSection;
    private JComboBox<String> cmbTerm;
    private JComboBox<String> cmbDay;
    private JComboBox<String> cmbStartTime;
    private JComboBox<String> cmbEndTime;
    private JComboBox<String> cmbSessionType;

    private JLabel lblFacultyLoad;
    private JTextField txtSearch;

    private JTable table;
    private DefaultTableModel tableModel;

    private ScheduleDAO scheduleDAO;
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private int selectedScheduleId = -1;

    private List<Integer> subjectIds = new ArrayList<>();
    private List<Integer> facultyIds = new ArrayList<>();
    private List<Integer> roomIds = new ArrayList<>();
    private List<String> roomTypes = new ArrayList<>();

    public ScheduleFrame() {
        scheduleDAO = new ScheduleDAO();

        setTitle("Faculty Loading & Schedule Management");
        setSize(1150, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();
        loadSubjects();
        loadFaculty();
        loadRooms();
        loadSchedules();
        updateFacultyLoadDisplay();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.HEADER);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("FACULTY LOADING & SCHEDULE MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Timetable allocation with real-time conflict detection and load monitoring in hours (working hours 7:00 AM - 6:00 PM)");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(Theme.HEADER_SUB);

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(12, 20, 15, 20));

        JPanel formCard = new JPanel(new GridLayout(5, 4, 10, 8));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        cmbSubject = new JComboBox<>();
        cmbFaculty = new JComboBox<>();
        cmbRoom = new JComboBox<>();
        txtSection = new JTextField();
        cmbTerm = new JComboBox<>(new String[]{"2026-2027 1st Sem", "2026-2027 2nd Sem", "Summer"});
        cmbDay = new JComboBox<>(new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"});
        cmbStartTime = createTimeComboBox(false);
        cmbEndTime = createTimeComboBox(true);
        cmbSessionType = new JComboBox<>(new String[]{"Lecture", "Laboratory"});
        cmbRoom.addActionListener(e -> syncSessionTypeWithRoom());   // lab rooms default to Laboratory

        formCard.add(new JLabel("Subject:")); formCard.add(cmbSubject);
        formCard.add(new JLabel("Faculty:")); formCard.add(cmbFaculty);
        formCard.add(new JLabel("Room:")); formCard.add(cmbRoom);
        formCard.add(new JLabel("Section:")); formCard.add(txtSection);
        formCard.add(new JLabel("Term:")); formCard.add(cmbTerm);
        formCard.add(new JLabel("Day:")); formCard.add(cmbDay);
        formCard.add(new JLabel("Start Time:")); formCard.add(cmbStartTime);
        formCard.add(new JLabel("End Time:")); formCard.add(cmbEndTime);

        String hintColor = String.format("#%06x", Theme.MUTED.getRGB() & 0xFFFFFF);
        formCard.add(new JLabel("<html>Teaching Load: <font color='" + hintColor
                + "' size='-1'><i>(hover over the load to view more details)</i></font></html>"));
        lblFacultyLoad = new JLabel("Select an instructor");
        lblFacultyLoad.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formCard.add(lblFacultyLoad);
        formCard.add(new JLabel("Class Type:")); formCard.add(cmbSessionType);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btnPanel.setOpaque(false);

        JButton btnSave = createBtn("Save", Theme.PRIMARY);
        JButton btnUpdate = createBtn("Update", Theme.ACCENT);
        JButton btnDelete = createBtn("Delete", Theme.DANGER);
        JButton btnClear = createBtn("Clear", Theme.SECONDARY);

        btnPanel.add(btnSave);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        JPanel formSection = new JPanel(new BorderLayout(0, 8));
        formSection.setOpaque(false);
        formSection.add(formCard, BorderLayout.CENTER);
        formSection.add(btnPanel, BorderLayout.SOUTH);
        body.add(formSection, BorderLayout.NORTH);

        JPanel tableCard = new JPanel(new BorderLayout(0, 8));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        searchBar.add(new JLabel("Search Schedules:"));
        txtSearch = new JTextField(18);
        searchBar.add(txtSearch);

        JButton btnSearch = createBtn("Search", Theme.PRIMARY);
        JButton btnShowAll = createBtn("Show All", Theme.SECONDARY);
        searchBar.add(btnSearch);
        searchBar.add(btnShowAll);
        tableCard.add(searchBar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Subject", "Units", "Faculty", "Room", "Section", "Term", "Day", "Start", "End", "Type"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(26);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        body.add(tableCard, BorderLayout.CENTER);

        root.add(body, BorderLayout.CENTER);
        setContentPane(root);

        btnSave.addActionListener(e -> saveSchedule());
        btnUpdate.addActionListener(e -> updateSchedule());
        btnDelete.addActionListener(e -> deleteSchedule());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> searchSchedules());
        btnShowAll.addActionListener(e -> loadSchedules());
        txtSearch.addActionListener(e -> searchSchedules());

        cmbFaculty.addActionListener(e -> updateFacultyLoadDisplay());
        cmbTerm.addActionListener(e -> updateFacultyLoadDisplay());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                selectSchedule();
            }
        });
    }

   private JButton createBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(85, 30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Start times run 7:00 AM to 5:30 PM; end times run 7:30 AM to 6:00 PM (working hours). */
    private JComboBox<String> createTimeComboBox(boolean endTimes) {
        JComboBox<String> combo = new JComboBox<>();
        int first = WorkingHours.START_HOUR * 60 + (endTimes ? 30 : 0);
        int last = WorkingHours.END_HOUR * 60 - (endTimes ? 0 : 30);
        for (int minutes = first; minutes <= last; minutes += 30) {
            int h = minutes / 60;
            int m = minutes % 60;
            String period = h >= 12 ? "PM" : "AM";
            int dh = h > 12 ? h - 12 : (h == 0 ? 12 : h);
            combo.addItem(String.format("%02d:%02d %s", dh, m, period));
        }
        return combo;
    }

    private void syncSessionTypeWithRoom() {
        int idx = cmbRoom.getSelectedIndex();
        if (idx >= 0 && idx < roomTypes.size()) {
            cmbSessionType.setSelectedItem(roomTypes.get(idx));
        }
    }

    private void updateFacultyLoadDisplay() {
        int idx = cmbFaculty.getSelectedIndex();
        if (idx < 0 || idx >= facultyIds.size()) {
            lblFacultyLoad.setText("No faculty selected");
            lblFacultyLoad.setForeground(Theme.MUTED);
            return;
        }

        int fId = facultyIds.get(idx);
        String term = (String) cmbTerm.getSelectedItem();
        try {
            double[] h = scheduleDAO.getLectureLabHours(fId, term, selectedScheduleId);
            double credited = LoadCalculator.creditedHours(h[0], h[1]);
            double regular = LoadCalculator.regularHours(scheduleDAO.getEmploymentType(fId));
            double ot = LoadCalculator.overtime(credited, regular);

            lblFacultyLoad.setText(String.format("%.2f / %.0f hrs  |  OT %.2f / %.0f hrs",
                    credited, regular, ot, LoadCalculator.MAX_OVERTIME_HOURS));
            lblFacultyLoad.setToolTipText(String.format(
                    "Lecture %.2f hrs + Lab %.2f hrs x %.2f = %.2f credited hrs (regular load %.0f hrs, max OT %.0f hrs)",
                    h[0], h[1], LoadCalculator.LAB_WEIGHT, credited, regular, LoadCalculator.MAX_OVERTIME_HOURS));
            if (LoadCalculator.exceedsMax(credited, regular)) {
                lblFacultyLoad.setForeground(Theme.STATUS_OVER);
            } else if (LoadCalculator.hasOvertime(credited, regular)) {
                lblFacultyLoad.setForeground(Theme.STATUS_WARN);
            } else {
                lblFacultyLoad.setForeground(Theme.STATUS_OK);
            }
        } catch (SQLException e) {
            lblFacultyLoad.setText("Cannot load faculty load: database error");
            lblFacultyLoad.setForeground(Theme.STATUS_OVER);
        }
    }

    private void loadSubjects() {
        cmbSubject.removeAllItems();
        subjectIds.clear();
        try {
            for (Subject sub : subjectDAO.getAllSubjects()) {
                subjectIds.add(sub.getSubjectId());
                cmbSubject.addItem(sub.getSubjectCode() + " - " + sub.getSubjectTitle() + " (" + sub.getUnits() + "u)");
            }
        } catch (SQLException e) {
            DbErrors.show(this, "Error loading subjects", e);
        }
    }

    private void loadFaculty() {
        cmbFaculty.removeAllItems();
        facultyIds.clear();
        try {
            for (Faculty f : facultyDAO.getActiveFaculty()) {
                facultyIds.add(f.getFacultyId());
                cmbFaculty.addItem(f.getEmployeeNo() + " - " + f.getFirstName() + " " + f.getLastName()
                        + " (" + f.getEmploymentType() + ")");
            }
        } catch (SQLException e) {
            DbErrors.show(this, "Error loading faculty", e);
        }
    }

    private void loadRooms() {
        cmbRoom.removeAllItems();
        roomIds.clear();
        roomTypes.clear();
        try {
            for (Room r : roomDAO.getAllRooms()) {
                roomIds.add(r.getRoomId());
                roomTypes.add(r.getRoomType());
                cmbRoom.addItem(r.getRoomName());
            }
        } catch (SQLException e) {
            DbErrors.show(this, "Error loading rooms", e);
        }
    }

    private void saveSchedule() {
        Schedule sched = getScheduleFromForm();
        if (sched == null) return;

        try {
            if (isOverloaded(sched, -1)) return;
            if (ConflictChecker.hasConflict(this, sched, -1)) return;

            if (scheduleDAO.addSchedule(sched)) {
                JOptionPane.showMessageDialog(this, "Schedule added successfully.");
                clearForm();
                loadSchedules();
            }
        } catch (SQLException e) {
            DbErrors.show(this, "Error adding schedule", e,
                    "The selected subject, faculty or room no longer exists.\nClose and reopen this screen, then try again.");
        }
    }

    private void updateSchedule() {
        if (selectedScheduleId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a schedule to update.");
            return;
        }
        Schedule sched = getScheduleFromForm();
        if (sched == null) return;
        sched.setScheduleId(selectedScheduleId);

        try {
            if (isOverloaded(sched, selectedScheduleId)) return;
            if (ConflictChecker.hasConflict(this, sched, selectedScheduleId)) return;

            if (scheduleDAO.updateSchedule(sched)) {
                JOptionPane.showMessageDialog(this, "Schedule updated successfully.");
                clearForm();
                loadSchedules();
            } else {
                JOptionPane.showMessageDialog(this, "The schedule no longer exists.",
                        "Not Found", JOptionPane.WARNING_MESSAGE);
                loadSchedules();
            }
        } catch (SQLException e) {
            DbErrors.show(this, "Error updating schedule", e,
                    "The selected subject, faculty or room no longer exists.\nClose and reopen this screen, then try again.");
        }
    }

   
    private boolean isOverloaded(Schedule s, int excludeId) throws SQLException {
        double[] cur = scheduleDAO.getLectureLabHours(s.getFacultyId(), s.getTerm(), excludeId);
        double current = LoadCalculator.creditedHours(cur[0], cur[1]);

        double clockHours = (s.getEndTime().getTime() - s.getStartTime().getTime()) / (1000.0 * 60 * 60);
        boolean lab = "Laboratory".equals(s.getSessionType());
        double added = lab ? clockHours * LoadCalculator.LAB_WEIGHT : clockHours;
        double total = current + added;

        double regular = LoadCalculator.regularHours(scheduleDAO.getEmploymentType(s.getFacultyId()));
        double max = LoadCalculator.maxHours(regular);

        if (LoadCalculator.exceedsMax(total, regular)) {
            JOptionPane.showMessageDialog(this,
                    String.format("Faculty Overload Error!\n\nThis class: %.2f hr(s) (%s)\nCurrent load: %.2f hr(s)\n"
                            + "New total: %.2f hr(s)\nMaximum allowed: %.0f regular + %.0f overtime = %.0f hr(s)",
                            added, lab ? "lab hours x " + LoadCalculator.LAB_WEIGHT : "lecture hours", current,
                            total, regular, LoadCalculator.MAX_OVERTIME_HOURS, max),
                    "Max Load Exceeded", JOptionPane.WARNING_MESSAGE);
            return true;
        }
        if (LoadCalculator.hasOvertime(total, regular)) {
            int choice = JOptionPane.showConfirmDialog(this,
                    String.format("This class puts the faculty member into overtime.\n\nRegular load: %.0f hr(s)\n"
                            + "New total: %.2f hr(s)\nOvertime: %.2f of %.0f hr(s)\n\nContinue?",
                            regular, total, LoadCalculator.overtime(total, regular),
                            LoadCalculator.MAX_OVERTIME_HOURS),
                    "Overtime Load", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            return choice != JOptionPane.YES_OPTION;
        }
        return false;
    }

    private void deleteSchedule() {
        if (selectedScheduleId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a schedule to delete.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this schedule?", "Confirm Delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        try {
            if (scheduleDAO.deleteSchedule(selectedScheduleId)) {
                JOptionPane.showMessageDialog(this, "Schedule deleted.");
                clearForm();
                loadSchedules();
            }
        } catch (SQLException e) {
            DbErrors.show(this, "Cannot delete schedule", e);
        }
    }

    private Schedule getScheduleFromForm() {
        if (cmbSubject.getSelectedIndex() == -1 || cmbFaculty.getSelectedIndex() == -1 || cmbRoom.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Please make sure Subject, Faculty, and Room are all selected.");
            return null;
        }
        String section = txtSection.getText().trim();
        if (section.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a class section.");
            return null;
        }
        Time st = convertToTime((String) cmbStartTime.getSelectedItem());
        Time et = convertToTime((String) cmbEndTime.getSelectedItem());
        if (!et.after(st)) {
            JOptionPane.showMessageDialog(this, "End time must be later than start time.", "Invalid Time", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (!WorkingHours.isWithin(st, et)) {
            JOptionPane.showMessageDialog(this,
                    "Classes must be held within working hours (" + WorkingHours.LABEL + ").",
                    "Outside Working Hours", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        Schedule s = new Schedule();
        s.setSubjectId(subjectIds.get(cmbSubject.getSelectedIndex()));
        s.setFacultyId(facultyIds.get(cmbFaculty.getSelectedIndex()));
        s.setRoomId(roomIds.get(cmbRoom.getSelectedIndex()));
        s.setSection(section);
        s.setTerm((String) cmbTerm.getSelectedItem());
        s.setDayOfWeek((String) cmbDay.getSelectedItem());
        s.setStartTime(st);
        s.setEndTime(et);
        s.setSessionType((String) cmbSessionType.getSelectedItem());
        return s;
    }

    private Time convertToTime(String t) {
        String[] parts = t.split(" ");
        String[] hm = parts[0].split(":");
        int h = Integer.parseInt(hm[0]);
        int m = Integer.parseInt(hm[1]);
        if (parts[1].equals("PM") && h != 12) h += 12;
        if (parts[1].equals("AM") && h == 12) h = 0;
        return Time.valueOf(String.format("%02d:%02d:00", h, m));
    }

    private void loadSchedules() {
        showScheduleRows(null, "Error loading schedules");
    }

    private void searchSchedules() {
        String kw = txtSearch.getText().trim();
        if (kw.isEmpty()) { loadSchedules(); return; }
        showScheduleRows(kw, "Error searching schedules");
    }

    private void showScheduleRows(String keyword, String errorTitle) {
        tableModel.setRowCount(0);
        try {
            for (Object[] row : scheduleDAO.getScheduleRows(keyword)) {
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            DbErrors.show(this, errorTitle, e);
        }
    }

    private void selectSchedule() {
        int r = table.getSelectedRow();
        if (r == -1) return;

        selectedScheduleId = Integer.parseInt(tableModel.getValueAt(r, 0).toString());
        String code = tableModel.getValueAt(r, 1).toString();
        String faculty = tableModel.getValueAt(r, 3).toString();
        String room = tableModel.getValueAt(r, 4).toString();
        txtSection.setText(tableModel.getValueAt(r, 5).toString());
        cmbTerm.setSelectedItem(tableModel.getValueAt(r, 6).toString());
        cmbDay.setSelectedItem(tableModel.getValueAt(r, 7).toString());

        for (int i = 0; i < cmbSubject.getItemCount(); i++) {
            if (cmbSubject.getItemAt(i).startsWith(code + " - ")) { cmbSubject.setSelectedIndex(i); break; }
        }
        for (int i = 0; i < cmbFaculty.getItemCount(); i++) {
            if (cmbFaculty.getItemAt(i).contains(faculty)) { cmbFaculty.setSelectedIndex(i); break; }
        }
        cmbRoom.setSelectedItem(room);

        Time st = (Time) tableModel.getValueAt(r, 8);
        Time et = (Time) tableModel.getValueAt(r, 9);
        cmbStartTime.setSelectedItem(formatTime(st));
        cmbEndTime.setSelectedItem(formatTime(et));
        if (tableModel.getColumnCount() > 10 && tableModel.getValueAt(r, 10) != null) {
            cmbSessionType.setSelectedItem(tableModel.getValueAt(r, 10).toString());   // after the room, which sets a default
        }
        updateFacultyLoadDisplay();
    }

    private String formatTime(Time t) {
        String[] p = t.toString().split(":");
        int h = Integer.parseInt(p[0]);
        int m = Integer.parseInt(p[1]);
        String period = h >= 12 ? "PM" : "AM";
        int dh = h > 12 ? h - 12 : (h == 0 ? 12 : h);
        return String.format("%02d:%02d %s", dh, m, period);
    }

    private void clearForm() {
        selectedScheduleId = -1;
        txtSection.setText("");
        if (cmbSubject.getItemCount() > 0) cmbSubject.setSelectedIndex(0);
        if (cmbFaculty.getItemCount() > 0) cmbFaculty.setSelectedIndex(0);
        if (cmbRoom.getItemCount() > 0) cmbRoom.setSelectedIndex(0);
        cmbTerm.setSelectedIndex(0);
        cmbDay.setSelectedIndex(0);
        cmbStartTime.setSelectedIndex(0);
        cmbEndTime.setSelectedIndex(0);
        syncSessionTypeWithRoom();
        table.clearSelection();
        updateFacultyLoadDisplay();
    }
}