package ui;

import database.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportsFrame extends JFrame {

    private JComboBox<String> cmbSummaryTerm;
    private JTable tblFacultySummary;
    private DefaultTableModel modelSummary;

    private JComboBox<String> cmbFacultyPicker;
    private JComboBox<String> cmbFacultyTerm;
    private JTable tblFacultySchedule;
    private DefaultTableModel modelFacultySchedule;
    private List<Integer> facultyIds = new ArrayList<>();

    private JComboBox<String> cmbRoomPicker;
    private JComboBox<String> cmbRoomTerm;
    private JTable tblRoomSchedule;
    private DefaultTableModel modelRoomSchedule;
    private List<Integer> roomIds = new ArrayList<>();

    public ReportsFrame() {
        super("System Reports - Faculty Loading & Room Utilization");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();

        loadFacultyList();
        loadRoomList();

        loadFacultySummaryReport();
        loadFacultyScheduleReport();
        loadRoomUtilizationReport();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(241, 245, 249)); // Unified slate background

        // =====================================================================
        // 1. UNIFIED HEADER BANNER (Identical to Faculty, Subject, Schedule)
        // =====================================================================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59)); // University Navy
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("SYSTEM REPORTS & LOAD ANALYTICS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Official faculty teaching load summaries, instructor timetables, and room utilization records");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(203, 213, 225));

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        // =====================================================================
        // 2. TABBED PANE (Wrapped with generous consistent padding)
        // =====================================================================
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("  1. Faculty Load Summary  ", createSummaryPanel());
        tabbedPane.addTab("  2. Schedule per Faculty  ", createFacultySchedulePanel());
        tabbedPane.addTab("  3. Room Utilization  ", createRoomUtilizationPanel());

        JPanel tabContainer = new JPanel(new BorderLayout());
        tabContainer.setOpaque(false);
        tabContainer.setBorder(new EmptyBorder(12, 20, 15, 20));
        tabContainer.add(tabbedPane, BorderLayout.CENTER);

        root.add(tabContainer, BorderLayout.CENTER);
        setContentPane(root);
    }

    // =========================================================================
    // REPORT 1: FACULTY LOAD SUMMARY
    // =========================================================================
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(241, 245, 249));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // FILTER CARD
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(6, 12, 6, 12)
        ));

        filterCard.add(new JLabel("Academic Term:"));
        cmbSummaryTerm = new JComboBox<>(new String[]{
            "2026-2027 1st Sem", "2026-2027 2nd Sem", "Summer"
        });
        filterCard.add(cmbSummaryTerm);

        JButton btnRefreshSummary = createBtn("Generate Report", new Color(37, 99, 235), 140);
        filterCard.add(btnRefreshSummary);

        panel.add(filterCard, BorderLayout.NORTH);

        // TABLE CARD
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        modelSummary = new DefaultTableModel(
                new Object[]{
                    "Employee No", "Faculty Name", "Department", "Type",
                    "Assigned Units", "Max Units", "Weekly Hours", "Status"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tblFacultySummary = new JTable(modelSummary);
        tblFacultySummary.setRowHeight(26);
        tblFacultySummary.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        tableCard.add(new JScrollPane(tblFacultySummary), BorderLayout.CENTER);
        panel.add(tableCard, BorderLayout.CENTER);

        btnRefreshSummary.addActionListener(e -> loadFacultySummaryReport());
        cmbSummaryTerm.addActionListener(e -> loadFacultySummaryReport());

        return panel;
    }

    private void loadFacultySummaryReport() {
        modelSummary.setRowCount(0);
        String term = (String) cmbSummaryTerm.getSelectedItem();

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

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int facultyId = rs.getInt("faculty_id");
                    String empNo = rs.getString("employee_no");
                    String name = rs.getString("first_name") + " " + rs.getString("last_name");
                    String dept = rs.getString("department_code");
                    String type = rs.getString("employment_type");
                    int assignedUnits = rs.getInt("total_units");
                    int maxUnits = rs.getInt("max_units");

                    double hours = calculateHoursForFaculty(facultyId, term);

                    String loadStatus;
                    if (assignedUnits == 0) {
                        loadStatus = "No Load";
                    } else if (assignedUnits == maxUnits) {
                        loadStatus = "Full Load";
                    } else if (assignedUnits > maxUnits) {
                        loadStatus = "OVERLOAD";
                    } else {
                        loadStatus = "Normal";
                    }

                    modelSummary.addRow(new Object[]{
                        empNo, name, dept, type, assignedUnits, maxUnits,
                        String.format("%.1f hrs", hours), loadStatus
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error generating summary: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private double calculateHoursForFaculty(int facultyId, String term) {
        String sql = "SELECT start_time, end_time FROM schedules WHERE faculty_id = ? AND term = ?";
        double total = 0.0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facultyId);
            ps.setString(2, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time start = rs.getTime("start_time");
                    Time end = rs.getTime("end_time");
                    if (start != null && end != null) {
                        total += (end.getTime() - start.getTime()) / (1000.0 * 60 * 60);
                    }
                }
            }
        } catch (SQLException e) { }
        return total;
    }

    // =========================================================================
    // REPORT 2: SCHEDULE PER FACULTY
    // =========================================================================
    private JPanel createFacultySchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(241, 245, 249));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // FILTER CARD
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(6, 12, 6, 12)
        ));

        filterCard.add(new JLabel("Instructor:"));
        cmbFacultyPicker = new JComboBox<>();
        filterCard.add(cmbFacultyPicker);

        filterCard.add(new JLabel("Term:"));
        cmbFacultyTerm = new JComboBox<>(new String[]{
            "2026-2027 1st Sem", "2026-2027 2nd Sem", "Summer"
        });
        filterCard.add(cmbFacultyTerm);

        JButton btnFilter = createBtn("View Timetable", new Color(37, 99, 235), 135);
        filterCard.add(btnFilter);

        panel.add(filterCard, BorderLayout.NORTH);

        // TABLE CARD
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        modelFacultySchedule = new DefaultTableModel(
                new Object[]{
                    "Day", "Time Slot", "Subject Code", "Subject Title", "Units", "Section", "Room"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tblFacultySchedule = new JTable(modelFacultySchedule);
        tblFacultySchedule.setRowHeight(26);
        tblFacultySchedule.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        tableCard.add(new JScrollPane(tblFacultySchedule), BorderLayout.CENTER);
        panel.add(tableCard, BorderLayout.CENTER);

        btnFilter.addActionListener(e -> loadFacultyScheduleReport());
        cmbFacultyPicker.addActionListener(e -> loadFacultyScheduleReport());
        cmbFacultyTerm.addActionListener(e -> loadFacultyScheduleReport());

        return panel;
    }

    private void loadFacultyScheduleReport() {
        modelFacultySchedule.setRowCount(0);
        int idx = cmbFacultyPicker.getSelectedIndex();
        if (idx < 0 || idx >= facultyIds.size()) return;

        int facultyId = facultyIds.get(idx);
        String term = (String) cmbFacultyTerm.getSelectedItem();

        String sql = "SELECT s.day_of_week, s.start_time, s.end_time, "
                   + "       sub.subject_code, sub.subject_title, sub.units, "
                   + "       s.section, r.room_name "
                   + "FROM schedules s "
                   + "INNER JOIN subjects sub ON s.subject_id = sub.subject_id "
                   + "INNER JOIN rooms r ON s.room_id = r.room_id "
                   + "WHERE s.faculty_id = ? AND s.term = ? "
                   + "ORDER BY s.day_of_week, s.start_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, facultyId);
            ps.setString(2, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String timeSlot = rs.getTime("start_time") + " - " + rs.getTime("end_time");
                    modelFacultySchedule.addRow(new Object[]{
                        rs.getString("day_of_week"),
                        timeSlot,
                        rs.getString("subject_code"),
                        rs.getString("subject_title"),
                        rs.getInt("units"),
                        rs.getString("section"),
                        rs.getString("room_name")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading faculty timetable: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // REPORT 3: ROOM UTILIZATION
    // =========================================================================
    private JPanel createRoomUtilizationPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(241, 245, 249));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // FILTER CARD
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(6, 12, 6, 12)
        ));

        filterCard.add(new JLabel("Classroom / Lab:"));
        cmbRoomPicker = new JComboBox<>();
        filterCard.add(cmbRoomPicker);

        filterCard.add(new JLabel("Term:"));
        cmbRoomTerm = new JComboBox<>(new String[]{
            "2026-2027 1st Sem", "2026-2027 2nd Sem", "Summer"
        });
        filterCard.add(cmbRoomTerm);

        JButton btnFilter = createBtn("View Utilization", new Color(37, 99, 235), 140);
        filterCard.add(btnFilter);

        panel.add(filterCard, BorderLayout.NORTH);

        // TABLE CARD
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        modelRoomSchedule = new DefaultTableModel(
                new Object[]{
                    "Day", "Time Slot", "Subject", "Section", "Instructor"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tblRoomSchedule = new JTable(modelRoomSchedule);
        tblRoomSchedule.setRowHeight(26);
        tblRoomSchedule.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        tableCard.add(new JScrollPane(tblRoomSchedule), BorderLayout.CENTER);
        panel.add(tableCard, BorderLayout.CENTER);

        btnFilter.addActionListener(e -> loadRoomUtilizationReport());
        cmbRoomPicker.addActionListener(e -> loadRoomUtilizationReport());
        cmbRoomTerm.addActionListener(e -> loadRoomUtilizationReport());

        return panel;
    }

    private void loadRoomUtilizationReport() {
        modelRoomSchedule.setRowCount(0);
        int idx = cmbRoomPicker.getSelectedIndex();
        if (idx < 0 || idx >= roomIds.size()) return;

        int roomId = roomIds.get(idx);
        String term = (String) cmbRoomTerm.getSelectedItem();

        String sql = "SELECT s.day_of_week, s.start_time, s.end_time, "
                   + "       sub.subject_code, s.section, "
                   + "       f.first_name, f.last_name "
                   + "FROM schedules s "
                   + "INNER JOIN subjects sub ON s.subject_id = sub.subject_id "
                   + "LEFT JOIN faculty f ON s.faculty_id = f.faculty_id "
                   + "WHERE s.room_id = ? AND s.term = ? "
                   + "ORDER BY s.day_of_week, s.start_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setString(2, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String timeSlot = rs.getTime("start_time") + " - " + rs.getTime("end_time");
                    String facultyName = rs.getString("first_name") != null
                            ? rs.getString("first_name") + " " + rs.getString("last_name")
                            : "Unassigned";

                    modelRoomSchedule.addRow(new Object[]{
                        rs.getString("day_of_week"),
                        timeSlot,
                        rs.getString("subject_code"),
                        rs.getString("section"),
                        facultyName
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading room utilization: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // BUTTON HELPER
    // =========================================================================
    private JButton createBtn(String text, Color bg, int width) {
        JButton btn = new JButton(text);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(width, 30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadFacultyList() {
        cmbFacultyPicker.removeAllItems();
        facultyIds.clear();
        String sql = "SELECT faculty_id, first_name, last_name FROM faculty WHERE status = 'Active' ORDER BY last_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                facultyIds.add(rs.getInt("faculty_id"));
                cmbFacultyPicker.addItem(rs.getString("first_name") + " " + rs.getString("last_name"));
            }
        } catch (SQLException e) {
            System.err.println("Error loading faculty picker: " + e.getMessage());
        }
    }

    private void loadRoomList() {
        cmbRoomPicker.removeAllItems();
        roomIds.clear();
        String sql = "SELECT room_id, room_name FROM rooms ORDER BY room_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roomIds.add(rs.getInt("room_id"));
                cmbRoomPicker.addItem(rs.getString("room_name"));
            }
        } catch (SQLException e) {
            System.err.println("Error loading room picker: " + e.getMessage());
        }
    }
}