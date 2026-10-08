package ui;

import dao.ScheduleDAO;
import database.DatabaseConnection;
import model.Schedule;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
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

    private JTextField txtSearch;

    private JTable table;
    private DefaultTableModel tableModel;

    private ScheduleDAO scheduleDAO;

    private int selectedScheduleId = -1;

    // Store database IDs separately
    private List<Integer> subjectIds = new ArrayList<>();
    private List<Integer> facultyIds = new ArrayList<>();
    private List<Integer> roomIds = new ArrayList<>();

    public ScheduleFrame() {

        scheduleDAO = new ScheduleDAO();

        setTitle("Schedule Management");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initializeUI();
        loadSubjects();
        loadFaculty();
        loadRooms();
        loadSchedules();
    }

    private void initializeUI() {

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("SCHEDULE MANAGEMENT");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));

        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(4, 4, 10, 10));

        cmbSubject = new JComboBox<>();
        cmbFaculty = new JComboBox<>();
        cmbRoom = new JComboBox<>();

        txtSection = new JTextField();

        cmbTerm = new JComboBox<>(
                new String[]{
                    "1st Semester",
                    "2nd Semester",
                    "Summer"
                }
        );

        cmbDay = new JComboBox<>(
                new String[]{
                    "Monday",
                    "Tuesday",
                    "Wednesday",
                    "Thursday",
                    "Friday",
                    "Saturday"
                }
        );

        cmbStartTime = createTimeComboBox();
        cmbEndTime = createTimeComboBox();

        formPanel.add(new JLabel("Subject:"));
        formPanel.add(cmbSubject);

        formPanel.add(new JLabel("Faculty:"));
        formPanel.add(cmbFaculty);

        formPanel.add(new JLabel("Room:"));
        formPanel.add(cmbRoom);

        formPanel.add(new JLabel("Section:"));
        formPanel.add(txtSection);

        formPanel.add(new JLabel("Term:"));
        formPanel.add(cmbTerm);

        formPanel.add(new JLabel("Day:"));
        formPanel.add(cmbDay);

        formPanel.add(new JLabel("Start Time:"));
        formPanel.add(cmbStartTime);

        formPanel.add(new JLabel("End Time:"));
        formPanel.add(cmbEndTime);

        JPanel topPanel = new JPanel(new BorderLayout(10, 15));
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 5)
        );

        JButton btnSave = new JButton("Save");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");

        buttonPanel.add(btnSave);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // SEARCH PANEL

        JPanel searchPanel = new JPanel(new BorderLayout(10, 5));

        txtSearch = new JTextField();

        JButton btnSearch = new JButton("Search");
        JButton btnShowAll = new JButton("Show All");

        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);

        JPanel searchButtons = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 5, 0)
        );

        searchButtons.add(btnSearch);
        searchButtons.add(btnShowAll);

        searchPanel.add(searchButtons, BorderLayout.EAST);

        // TABLE

        tableModel = new DefaultTableModel(
                new Object[]{
                    "ID",
                    "Subject",
                    "Faculty",
                    "Room",
                    "Section",
                    "Term",
                    "Day",
                    "Start",
                    "End"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);

        // BUTTON EVENTS

        btnSave.addActionListener(e -> saveSchedule());

        btnUpdate.addActionListener(e -> updateSchedule());

        btnDelete.addActionListener(e -> deleteSchedule());

        btnClear.addActionListener(e -> clearForm());

        btnSearch.addActionListener(e -> searchSchedules());

        btnShowAll.addActionListener(e -> loadSchedules());

        // TABLE ROW SELECTION

        table.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()
                    && table.getSelectedRow() != -1) {

                selectSchedule();
            }
        });
    }

    private JComboBox<String> createTimeComboBox() {

        JComboBox<String> combo = new JComboBox<>();

        for (int hour = 7; hour <= 20; hour++) {

            for (int minute = 0; minute < 60; minute += 30) {

                String period = hour >= 12 ? "PM" : "AM";

                int displayHour = hour;

                if (displayHour > 12) {
                    displayHour -= 12;
                }

                if (displayHour == 0) {
                    displayHour = 12;
                }

                String time = String.format(
                        "%02d:%02d %s",
                        displayHour,
                        minute,
                        period
                );

                combo.addItem(time);
            }
        }

        return combo;
    }

    // =========================
    // LOAD SUBJECTS
    // =========================

    private void loadSubjects() {

        cmbSubject.removeAllItems();
        subjectIds.clear();

        String sql =
                "SELECT subject_id, subject_code, subject_title "
                + "FROM subjects "
                + "ORDER BY subject_code";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {

                int id = rs.getInt("subject_id");

                String display =
                        rs.getString("subject_code")
                        + " - "
                        + rs.getString("subject_title");

                subjectIds.add(id);
                cmbSubject.addItem(display);
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error loading subjects:\n"
                    + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================
    // LOAD FACULTY
    // =========================

    private void loadFaculty() {

        cmbFaculty.removeAllItems();
        facultyIds.clear();

        String sql =
                "SELECT faculty_id, employee_no, first_name, last_name "
                + "FROM faculty "
                + "ORDER BY last_name, first_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {

                int id = rs.getInt("faculty_id");

                String display =
                        rs.getString("employee_no")
                        + " - "
                        + rs.getString("first_name")
                        + " "
                        + rs.getString("last_name");

                facultyIds.add(id);
                cmbFaculty.addItem(display);
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error loading faculty:\n"
                    + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================
    // LOAD ROOMS
    // =========================

    private void loadRooms() {

        cmbRoom.removeAllItems();
        roomIds.clear();

        String sql =
                "SELECT room_id, room_name "
                + "FROM rooms "
                + "ORDER BY room_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {

                int id = rs.getInt("room_id");

                String display = rs.getString("room_name");

                roomIds.add(id);
                cmbRoom.addItem(display);
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error loading rooms:\n"
                    + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================
    // SAVE
    // =========================

    private void saveSchedule() {

        Schedule schedule = getScheduleFromForm();

        if (schedule == null) {
            return;
        }

        if (scheduleDAO.addSchedule(schedule)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Schedule added successfully."
            );

            clearForm();
            loadSchedules();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Failed to add schedule.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================
    // UPDATE
    // =========================

    private void updateSchedule() {

        if (selectedScheduleId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a schedule first."
            );

            return;
        }

        Schedule schedule = getScheduleFromForm();

        if (schedule == null) {
            return;
        }

        schedule.setScheduleId(selectedScheduleId);

        if (scheduleDAO.updateSchedule(schedule)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Schedule updated successfully."
            );

            clearForm();
            loadSchedules();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Failed to update schedule.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================
    // DELETE
    // =========================

    private void deleteSchedule() {

        if (selectedScheduleId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a schedule first."
            );

            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this schedule?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        if (scheduleDAO.deleteSchedule(selectedScheduleId)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Schedule deleted successfully."
            );

            clearForm();
            loadSchedules();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete schedule.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================
    // GET FORM DATA
    // =========================

    private Schedule getScheduleFromForm() {

        if (cmbSubject.getSelectedIndex() == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a subject."
            );

            return null;
        }

        if (cmbFaculty.getSelectedIndex() == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a faculty member."
            );

            return null;
        }

        if (cmbRoom.getSelectedIndex() == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a room."
            );

            return null;
        }

        String section = txtSection.getText().trim();

        if (section.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a section."
            );

            txtSection.requestFocus();

            return null;
        }

        String startText =
                (String) cmbStartTime.getSelectedItem();

        String endText =
                (String) cmbEndTime.getSelectedItem();

        Time startTime = convertToTime(startText);
        Time endTime = convertToTime(endText);

        // IMPORTANT VALIDATION
        if (!endTime.after(startTime)) {

            JOptionPane.showMessageDialog(
                    this,
                    "End time must be later than start time.",
                    "Invalid Time",
                    JOptionPane.ERROR_MESSAGE
            );

            return null;
        }

        Schedule schedule = new Schedule();

        schedule.setSubjectId(
                subjectIds.get(cmbSubject.getSelectedIndex())
        );

        schedule.setFacultyId(
                facultyIds.get(cmbFaculty.getSelectedIndex())
        );

        schedule.setRoomId(
                roomIds.get(cmbRoom.getSelectedIndex())
        );

        schedule.setSection(section);

        schedule.setTerm(
                (String) cmbTerm.getSelectedItem()
        );

        schedule.setDayOfWeek(
                (String) cmbDay.getSelectedItem()
        );

        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);

        return schedule;
    }

    // =========================
    // CONVERT TIME
    // =========================

    private Time convertToTime(String timeText) {

        try {

            String[] parts = timeText.split(" ");

            String[] hm = parts[0].split(":");

            int hour = Integer.parseInt(hm[0]);
            int minute = Integer.parseInt(hm[1]);

            String period = parts[1];

            if (period.equals("PM") && hour != 12) {
                hour += 12;
            }

            if (period.equals("AM") && hour == 12) {
                hour = 0;
            }

            return Time.valueOf(
                    String.format(
                            "%02d:%02d:00",
                            hour,
                            minute
                    )
            );

        } catch (Exception e) {

            return Time.valueOf("00:00:00");
        }
    }

    // =========================
    // LOAD TABLE
    // =========================

    private void loadSchedules() {

        tableModel.setRowCount(0);

        String sql =
                "SELECT "
                + "s.schedule_id, "
                + "s.section, "
                + "s.term, "
                + "s.day_of_week, "
                + "s.start_time, "
                + "s.end_time, "
                + "sub.subject_code, "
                + "sub.subject_title, "
                + "f.employee_no, "
                + "f.first_name, "
                + "f.last_name, "
                + "r.room_name "
                + "FROM schedules s "
                + "INNER JOIN subjects sub "
                + "ON s.subject_id = sub.subject_id "
                + "LEFT JOIN faculty f "
                + "ON s.faculty_id = f.faculty_id "
                + "INNER JOIN rooms r "
                + "ON s.room_id = r.room_id "
                + "ORDER BY s.day_of_week, s.start_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {

                String facultyName =
                        rs.getString("first_name")
                        + " "
                        + rs.getString("last_name");

                tableModel.addRow(new Object[]{
                    rs.getInt("schedule_id"),
                    rs.getString("subject_code"),
                    facultyName,
                    rs.getString("room_name"),
                    rs.getString("section"),
                    rs.getString("term"),
                    rs.getString("day_of_week"),
                    rs.getTime("start_time"),
                    rs.getTime("end_time")
                });
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error loading schedules:\n"
                    + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================
    // SEARCH
    // =========================

    private void searchSchedules() {

        String keyword = txtSearch.getText().trim();

        if (keyword.isEmpty()) {

            loadSchedules();

            return;
        }

        tableModel.setRowCount(0);

        String sql =
                "SELECT "
                + "s.schedule_id, "
                + "s.section, "
                + "s.term, "
                + "s.day_of_week, "
                + "s.start_time, "
                + "s.end_time, "
                + "sub.subject_code, "
                + "sub.subject_title, "
                + "f.first_name, "
                + "f.last_name, "
                + "r.room_name "
                + "FROM schedules s "
                + "INNER JOIN subjects sub "
                + "ON s.subject_id = sub.subject_id "
                + "LEFT JOIN faculty f "
                + "ON s.faculty_id = f.faculty_id "
                + "INNER JOIN rooms r "
                + "ON s.room_id = r.room_id "
                + "WHERE LOWER(sub.subject_code) LIKE ? "
                + "OR LOWER(sub.subject_title) LIKE ? "
                + "OR LOWER(s.section) LIKE ? "
                + "OR LOWER(s.term) LIKE ? "
                + "OR LOWER(s.day_of_week) LIKE ? "
                + "OR LOWER(r.room_name) LIKE ? "
                + "ORDER BY s.day_of_week, s.start_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            String search = "%" + keyword.toLowerCase() + "%";

            for (int i = 1; i <= 6; i++) {
                pst.setString(i, search);
            }

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {

                String facultyName =
                        rs.getString("first_name")
                        + " "
                        + rs.getString("last_name");

                tableModel.addRow(new Object[]{
                    rs.getInt("schedule_id"),
                    rs.getString("subject_code"),
                    facultyName,
                    rs.getString("room_name"),
                    rs.getString("section"),
                    rs.getString("term"),
                    rs.getString("day_of_week"),
                    rs.getTime("start_time"),
                    rs.getTime("end_time")
                });
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error searching schedules:\n"
                    + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================
    // SELECT TABLE ROW
    // =========================

    private void selectSchedule() {

        int row = table.getSelectedRow();

        if (row == -1) {
            return;
        }

        selectedScheduleId =
                Integer.parseInt(
                        tableModel.getValueAt(row, 0).toString()
                );

        String subjectCode =
                tableModel.getValueAt(row, 1).toString();

        String facultyName =
                tableModel.getValueAt(row, 2).toString();

        String roomName =
                tableModel.getValueAt(row, 3).toString();

        String section =
                tableModel.getValueAt(row, 4).toString();

        String term =
                tableModel.getValueAt(row, 5).toString();

        String day =
                tableModel.getValueAt(row, 6).toString();

        Time startTime =
                (Time) tableModel.getValueAt(row, 7);

        Time endTime =
                (Time) tableModel.getValueAt(row, 8);

        txtSection.setText(section);

        cmbTerm.setSelectedItem(term);
        cmbDay.setSelectedItem(day);

        cmbSubject.setSelectedItem(findSubject(subjectCode));

        cmbFaculty.setSelectedItem(findFaculty(facultyName));

        cmbRoom.setSelectedItem(roomName);

        cmbStartTime.setSelectedItem(
                formatTime(startTime)
        );

        cmbEndTime.setSelectedItem(
                formatTime(endTime)
        );
    }

    private String findSubject(String subjectCode) {

        for (int i = 0; i < cmbSubject.getItemCount(); i++) {

            String item = cmbSubject.getItemAt(i);

            if (item.startsWith(subjectCode + " - ")) {
                return item;
            }
        }

        return null;
    }

    private String findFaculty(String facultyName) {

        for (int i = 0; i < cmbFaculty.getItemCount(); i++) {

            String item = cmbFaculty.getItemAt(i);

            if (item.endsWith(" - " + facultyName)) {
                return item;
            }
        }

        return null;
    }

    private String formatTime(Time time) {

        if (time == null) {
            return "";
        }

        String[] parts = time.toString().split(":");

        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        String period = hour >= 12 ? "PM" : "AM";

        int displayHour = hour;

        if (displayHour > 12) {
            displayHour -= 12;
        }

        if (displayHour == 0) {
            displayHour = 12;
        }

        return String.format(
                "%02d:%02d %s",
                displayHour,
                minute,
                period
        );
    }

    // =========================
    // CLEAR
    // =========================

    private void clearForm() {

        selectedScheduleId = -1;

        txtSection.setText("");

        if (cmbSubject.getItemCount() > 0) {
            cmbSubject.setSelectedIndex(0);
        }

        if (cmbFaculty.getItemCount() > 0) {
            cmbFaculty.setSelectedIndex(0);
        }

        if (cmbRoom.getItemCount() > 0) {
            cmbRoom.setSelectedIndex(0);
        }

        cmbTerm.setSelectedIndex(0);
        cmbDay.setSelectedIndex(0);
        cmbStartTime.setSelectedIndex(0);
        cmbEndTime.setSelectedIndex(0);

        table.clearSelection();
    }
}