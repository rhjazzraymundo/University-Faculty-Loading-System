package ui;

import dao.FacultyDAO;
import dao.ReportDAO;
import model.Faculty;
import util.DbErrors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportsFrame extends JFrame {

    private final ReportDAO reportDAO = new ReportDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();

    private JComboBox<String> cmbSummaryTerm;
    private JTable tblFacultySummary;
    private DefaultTableModel modelSummary;

    private JComboBox<String> cmbFacultyPicker;
    private JComboBox<String> cmbFacultyTerm;
    private JTable tblFacultySchedule;
    private DefaultTableModel modelFacultySchedule;
    private List<Integer> facultyIds = new ArrayList<>();

    public ReportsFrame() {
        super("System Reports - Faculty Loading");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();

        loadFacultyList();

        loadFacultySummaryReport();
        loadFacultyScheduleReport();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.BACKGROUND); // Unified slate background

        // =====================================================================
        // 1. UNIFIED HEADER BANNER (Identical to Faculty, Subject, Schedule)
        // =====================================================================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.HEADER); // University Navy
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("SYSTEM REPORTS & LOAD ANALYTICS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Official faculty teaching load summaries and instructor timetables");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(Theme.HEADER_SUB);

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
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // FILTER CARD
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));

        filterCard.add(new JLabel("Academic Term:"));
        cmbSummaryTerm = new JComboBox<>(new String[]{
            "2026-2027 1st Sem", "2026-2027 2nd Sem", "Summer"
        });
        filterCard.add(cmbSummaryTerm);

        JButton btnRefreshSummary = createBtn("Generate Report", Theme.PRIMARY, 140);
        filterCard.add(btnRefreshSummary);

        panel.add(filterCard, BorderLayout.NORTH);

        // TABLE CARD
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
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
        Theme.styleTable(tblFacultySummary);

        tableCard.add(new JScrollPane(tblFacultySummary), BorderLayout.CENTER);
        panel.add(tableCard, BorderLayout.CENTER);

        btnRefreshSummary.addActionListener(e -> loadFacultySummaryReport());
        cmbSummaryTerm.addActionListener(e -> loadFacultySummaryReport());

        return panel;
    }

    private void loadFacultySummaryReport() {
        modelSummary.setRowCount(0);
        String term = (String) cmbSummaryTerm.getSelectedItem();

        try {
            for (Object[] r : reportDAO.getFacultyLoadSummary(term)) {
                int assignedUnits = (Integer) r[4];
                int maxUnits = (Integer) r[5];
                double hours = (Double) r[6];

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
                    r[0], r[1], r[2], r[3], assignedUnits, maxUnits,
                    String.format("%.1f hrs", hours), loadStatus
                });
            }
        } catch (SQLException e) {
            DbErrors.show(this, "Error generating summary", e);
        }
    }

    // =========================================================================
    // REPORT 2: SCHEDULE PER FACULTY
    // =========================================================================
    private JPanel createFacultySchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // FILTER CARD
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
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

        JButton btnFilter = createBtn("View Timetable", Theme.PRIMARY, 135);
        filterCard.add(btnFilter);

        panel.add(filterCard, BorderLayout.NORTH);

        // TABLE CARD
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
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
        Theme.styleTable(tblFacultySchedule);

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

        try {
            for (Object[] row : reportDAO.getFacultyTimetable(facultyId, term)) {
                modelFacultySchedule.addRow(row);
            }
        } catch (SQLException e) {
            DbErrors.show(this, "Error loading faculty timetable", e);
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
        try {
            for (Faculty f : facultyDAO.getActiveFaculty()) {
                facultyIds.add(f.getFacultyId());
                cmbFacultyPicker.addItem(f.getFirstName() + " " + f.getLastName());
            }
        } catch (SQLException e) {
            DbErrors.show(this, "Error loading instructors", e);
        }
    }
}