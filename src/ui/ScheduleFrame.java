package ui;

import dao.ScheduleDAO;
import database.DatabaseConnection;
import model.Schedule;
import util.ConflictChecker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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

    private JLabel lblFacultyLoad;
    private JTextField txtSearch;

    private JTable table;
    private DefaultTableModel tableModel;

    private ScheduleDAO scheduleDAO;
    private int selectedScheduleId = -1;

    private List<Integer> subjectIds = new ArrayList<>();
    private List<Integer> facultyIds = new ArrayList<>();
    private List<Integer> roomIds = new ArrayList<>();

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

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.HEADER);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("FACULTY LOADING & SCHEDULE MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Timetable allocation with real-time conflict detection and max-unit monitoring");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(Theme.HEADER_SUB);

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        // BODY
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(12, 20, 15, 20));

        // FORM CARD
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
        cmbStartTime = createTimeComboBox();
        cmbEndTime = createTimeComboBox();

        formCard.add(new JLabel("Subject:")); formCard.add(cmbSubject);
        formCard.add(new JLabel("Faculty:")); formCard.add(cmbFaculty);
        formCard.add(new JLabel("Room:")); formCard.add(cmbRoom);
        formCard.add(new JLabel("Section:")); formCard.add(txtSection);
        formCard.add(new JLabel("Term:")); formCard.add(cmbTerm);
        formCard.add(new JLabel("Day:")); formCard.add(cmbDay);
        formCard.add(new JLabel("Start Time:")); formCard.add(cmbStartTime);
        formCard.add(new JLabel("End Time:")); formCard.add(cmbEndTime);

        formCard.add(new JLabel("Teaching Load:"));
        lblFacultyLoad = new JLabel("Select an instructor");
        lblFacultyLoad.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formCard.add(lblFacultyLoad);
        formCard.add(new JLabel("")); formCard.add(new JLabel(""));

        // ACTION BUTTONS
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

        // TABLE CARD
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
                new Object[]{"ID", "Subject", "Units", "Faculty", "Room", "Section", "Term", "Day", "Start", "End"}, 0
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

        // LISTENERS
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

    private JComboBox<String> createTimeComboBox() {
        JComboBox<String> combo = new JComboBox<>();
        for (int h = 7; h <= 20; h++) {
            for (int m = 0; m < 60; m += 30) {
                String period = h >= 12 ? "PM" : "AM";
                int dh = h > 12 ? h - 12 : (h == 0 ? 12 : h);
                combo.addItem(String.format("%02d:%02d %s", dh, m, period));
            }
        }
        return combo;
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
        int units = scheduleDAO.getTotalAssignedUnits(fId, term, selectedScheduleId);
        double hrs = scheduleDAO.getTotalAssignedHours(fId, term, selectedScheduleId);
        int max = scheduleDAO.getFacultyMaxUnits(fId);

        lblFacultyLoad.setText(String.format("Units: %d / %d  |  Hours: %.1f hrs/week", units, max, hrs));
        if (units > max) {
            lblFacultyLoad.setForeground(Theme.STATUS_OVER);
        } else if (units >= max - 3) {
            lblFacultyLoad.setForeground(Theme.STATUS_WARN);
        } else {
            lblFacultyLoad.setForeground(Theme.STATUS_OK);
        }
    }

    private void loadSubjects() {
        cmbSubject.removeAllItems();
        subjectIds.clear();
        String sql = "SELECT subject_id, subject_code, subject_title, units FROM subjects ORDER BY subject_code";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                subjectIds.add(rs.getInt("subject_id"));
                cmbSubject.addItem(rs.getString("subject_code") + " - " + rs.getString("subject_title") + " (" + rs.getInt("units") + "u)");
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
    }

    private void loadFaculty() {
        cmbFaculty.removeAllItems();
        facultyIds.clear();
        String sql = "SELECT faculty_id, employee_no, first_name, last_name, max_units FROM faculty WHERE status = 'Active' ORDER BY last_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                facultyIds.add(rs.getInt("faculty_id"));
                cmbFaculty.addItem(rs.getString("employee_no") + " - " + rs.getString("first_name") + " " + rs.getString("last_name") + " (Max: " + rs.getInt("max_units") + "u)");
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
    }

    private void loadRooms() {
        cmbRoom.removeAllItems();
        roomIds.clear();
        String sql = "SELECT room_id, room_name FROM rooms ORDER BY room_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roomIds.add(rs.getInt("room_id"));
                cmbRoom.addItem(rs.getString("room_name"));
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
    }

    private void saveSchedule() {
        Schedule sched = getScheduleFromForm();
        if (sched == null) return;

        if (isOverloaded(sched, -1)) return;
        if (ConflictChecker.hasConflict(this, sched, -1)) return;

        if (scheduleDAO.addSchedule(sched)) {
            JOptionPane.showMessageDialog(this, "Schedule added successfully.");
            clearForm();
            loadSchedules();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add schedule.", "Error", JOptionPane.ERROR_MESSAGE);
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

        if (isOverloaded(sched, selectedScheduleId)) return;
        if (ConflictChecker.hasConflict(this, sched, selectedScheduleId)) return;

        if (scheduleDAO.updateSchedule(sched)) {
            JOptionPane.showMessageDialog(this, "Schedule updated successfully.");
            clearForm();
            loadSchedules();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update schedule.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isOverloaded(Schedule s, int excludeId) {
        int u = scheduleDAO.getSubjectUnits(s.getSubjectId());
        int cur = scheduleDAO.getTotalAssignedUnits(s.getFacultyId(), s.getTerm(), excludeId);
        int max = scheduleDAO.getFacultyMaxUnits(s.getFacultyId());
        if (cur + u > max) {
            JOptionPane.showMessageDialog(this,
                    String.format("Faculty Overload Error!\n\nSubject: %d unit(s)\nCurrent: %d unit(s)\nMax Limit: %d unit(s)", u, cur, max),
                    "Max Load Exceeded", JOptionPane.WARNING_MESSAGE);
            return true;
        }
        return false;
    }

    private void deleteSchedule() {
        if (selectedScheduleId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a schedule to delete.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this schedule?", "Confirm Delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        if (scheduleDAO.deleteSchedule(selectedScheduleId)) {
            JOptionPane.showMessageDialog(this, "Schedule deleted.");
            clearForm();
            loadSchedules();
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

        Schedule s = new Schedule();
        s.setSubjectId(subjectIds.get(cmbSubject.getSelectedIndex()));
        s.setFacultyId(facultyIds.get(cmbFaculty.getSelectedIndex()));
        s.setRoomId(roomIds.get(cmbRoom.getSelectedIndex()));
        s.setSection(section);
        s.setTerm((String) cmbTerm.getSelectedItem());
        s.setDayOfWeek((String) cmbDay.getSelectedItem());
        s.setStartTime(st);
        s.setEndTime(et);
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
        tableModel.setRowCount(0);
        String sql = "SELECT s.schedule_id, s.section, s.term, s.day_of_week, s.start_time, s.end_time, "
                   + "       sub.subject_code, sub.units, f.first_name, f.last_name, r.room_name "
                   + "FROM schedules s "
                   + "INNER JOIN subjects sub ON s.subject_id = sub.subject_id "
                   + "LEFT JOIN faculty f ON s.faculty_id = f.faculty_id "
                   + "INNER JOIN rooms r ON s.room_id = r.room_id "
                   + "ORDER BY CASE s.day_of_week WHEN 'Monday' THEN 1 WHEN 'Tuesday' THEN 2 WHEN 'Wednesday' THEN 3 WHEN 'Thursday' THEN 4 WHEN 'Friday' THEN 5 WHEN 'Saturday' THEN 6 ELSE 7 END, s.start_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String fname = rs.getString("first_name") != null ? rs.getString("first_name") + " " + rs.getString("last_name") : "Unassigned";
                tableModel.addRow(new Object[]{
                    rs.getInt("schedule_id"), rs.getString("subject_code"), rs.getInt("units"),
                    fname, rs.getString("room_name"), rs.getString("section"), rs.getString("term"),
                    rs.getString("day_of_week"), rs.getTime("start_time"), rs.getTime("end_time")
                });
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
    }

    private void searchSchedules() {
        String kw = txtSearch.getText().trim();
        if (kw.isEmpty()) { loadSchedules(); return; }
        tableModel.setRowCount(0);
        String sql = "SELECT s.schedule_id, s.section, s.term, s.day_of_week, s.start_time, s.end_time, "
                   + "       sub.subject_code, sub.units, f.first_name, f.last_name, r.room_name "
                   + "FROM schedules s "
                   + "INNER JOIN subjects sub ON s.subject_id = sub.subject_id "
                   + "LEFT JOIN faculty f ON s.faculty_id = f.faculty_id "
                   + "INNER JOIN rooms r ON s.room_id = r.room_id "
                   + "WHERE LOWER(sub.subject_code) LIKE ? OR LOWER(s.section) LIKE ? OR LOWER(r.room_name) LIKE ? "
                   + "ORDER BY CASE s.day_of_week WHEN 'Monday' THEN 1 WHEN 'Tuesday' THEN 2 WHEN 'Wednesday' THEN 3 WHEN 'Thursday' THEN 4 WHEN 'Friday' THEN 5 WHEN 'Saturday' THEN 6 ELSE 7 END, s.start_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String p = "%" + kw.toLowerCase() + "%";
            ps.setString(1, p); ps.setString(2, p); ps.setString(3, p);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String fname = rs.getString("first_name") != null ? rs.getString("first_name") + " " + rs.getString("last_name") : "Unassigned";
                tableModel.addRow(new Object[]{
                    rs.getInt("schedule_id"), rs.getString("subject_code"), rs.getInt("units"),
                    fname, rs.getString("room_name"), rs.getString("section"), rs.getString("term"),
                    rs.getString("day_of_week"), rs.getTime("start_time"), rs.getTime("end_time")
                });
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
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
        table.clearSelection();
        updateFacultyLoadDisplay();
    }
}