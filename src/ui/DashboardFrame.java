package ui;

import database.DatabaseConnection;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardFrame extends JFrame {

    private final User currentUser;

    private final JLabel lblFacultyCount = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblSubjectCount = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblDepartmentCount = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblRoomCount = new JLabel("0", SwingConstants.CENTER);

    public DashboardFrame(User user) {
        super("University Faculty Loading System - Dashboard");
        this.currentUser = user;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 640);
        setLocationRelativeTo(null);

        buildUI();
        loadCounts();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.BACKGROUND); // Clean light slate background

        // =====================================================================
        // 1. TOP HEADER BANNER (Title, User Details, Logout)
        // =====================================================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.HEADER); // Professional Navy Slate
        headerPanel.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel headerTextPanel = new JPanel(new GridLayout(2, 1, 3, 3));
        headerTextPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("UNIVERSITY FACULTY LOADING SYSTEM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        String roleTag = currentUser.isAdmin() ? "Administrator (Full Access)" : "Staff (Scheduling & Reports)";
        JLabel lblUser = new JLabel("Welcome, " + currentUser.getFullName() + "  |  Role: " + roleTag);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUser.setForeground(Theme.HEADER_SUB);

        headerTextPanel.add(lblTitle);
        headerTextPanel.add(lblUser);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(Theme.ACCENT); // Crimson red
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setPreferredSize(new Dimension(85, 30));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> logout());

        headerPanel.add(headerTextPanel, BorderLayout.WEST);
        headerPanel.add(btnLogout, BorderLayout.EAST);

        root.add(headerPanel, BorderLayout.NORTH);

        // =====================================================================
        // 2. MAIN CENTER CONTENT
        // =====================================================================
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // ROW OF 4 METRIC CARDS
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsRow.setOpaque(false);
        cardsRow.setPreferredSize(new Dimension(900, 110)); // Prevents stretching!

        cardsRow.add(createStatCard("FACULTY", lblFacultyCount, "Active Instructors", Theme.CARD_1));
        cardsRow.add(createStatCard("SUBJECTS", lblSubjectCount, "Curriculum Courses", Theme.CARD_2));
        cardsRow.add(createStatCard("DEPARTMENTS", lblDepartmentCount, "Academic Units", Theme.CARD_3));
        cardsRow.add(createStatCard("ROOMS", lblRoomCount, "Lecture & Lab Rooms", Theme.CARD_4));

        centerPanel.add(cardsRow, BorderLayout.NORTH);

        // MODULE NAVIGATION SECTION
        JPanel moduleSection = new JPanel(new BorderLayout(0, 10));
        moduleSection.setOpaque(false);

        JLabel lblSection = new JLabel("SYSTEM MODULES & MANAGEMENT");
        lblSection.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSection.setForeground(Theme.TEXT);
        moduleSection.add(lblSection, BorderLayout.NORTH);

        JPanel moduleGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        moduleGrid.setOpaque(false);

        boolean isAdmin = currentUser.isAdmin();

        JButton btnFaculty = createNavButton("Faculty Management", "Manage instructor profiles & max load", isAdmin);
        JButton btnSubjects = createNavButton("Subjects & Curriculum", "Course codes, titles & unit weights", isAdmin);
        JButton btnDepartments = createNavButton("Department Registry", "Academic departments and codes", isAdmin);
        JButton btnRooms = createNavButton("Rooms & Facilities", "Classroom capacities & room types", isAdmin);
        JButton btnSchedule = createNavButton("Faculty Loading & Schedules", "Assign classes and check time conflicts", true);
        JButton btnReports = createNavButton("Reports & Timetables", "Faculty load summary & room usage", true);

        moduleGrid.add(btnFaculty);
        moduleGrid.add(btnSubjects);
        moduleGrid.add(btnDepartments);
        moduleGrid.add(btnRooms);
        moduleGrid.add(btnSchedule);
        moduleGrid.add(btnReports);

        moduleSection.add(moduleGrid, BorderLayout.CENTER);
        centerPanel.add(moduleSection, BorderLayout.CENTER);

        root.add(centerPanel, BorderLayout.CENTER);

        // =====================================================================
        // 3. STATUS FOOTER
        // =====================================================================
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                new EmptyBorder(8, 24, 8, 24)
        ));

        JLabel lblFooter = new JLabel("Database: Apache Derby (Port 1527)  |  Faculty Loading System v1.0");
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooter.setForeground(Theme.MUTED);

        JButton btnRefresh = new JButton("Refresh Data");
        btnRefresh.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnRefresh.setBackground(Theme.PRIMARY);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setPreferredSize(new Dimension(110, 26));
        btnRefresh.addActionListener(e -> loadCounts());

        footer.add(lblFooter, BorderLayout.WEST);
        footer.add(btnRefresh, BorderLayout.EAST);

        root.add(footer, BorderLayout.SOUTH);

        // =====================================================================
        // NAVIGATION ACTIONS (WITH ROLE ACCESS CHECK)
        // =====================================================================
        btnFaculty.addActionListener(e -> {
            if (!isAdmin) {
                showAccessDenied("Only Administrators can modify Faculty records.");
                return;
            }
            new FacultyFrame().setVisible(true);
        });

        btnSubjects.addActionListener(e -> {
            if (!isAdmin) {
                showAccessDenied("Only Administrators can modify Subjects.");
                return;
            }
            new SubjectFrame().setVisible(true);
        });

        btnDepartments.addActionListener(e -> {
            if (!isAdmin) {
                showAccessDenied("Only Administrators can manage Departments.");
                return;
            }
            new DepartmentDialog(this).setVisible(true);
            loadCounts();
        });

        btnRooms.addActionListener(e -> {
            if (!isAdmin) {
                showAccessDenied("Only Administrators can manage Rooms.");
                return;
            }
            new RoomDialog(this).setVisible(true);
            loadCounts();
        });

        btnSchedule.addActionListener(e -> new ScheduleFrame().setVisible(true));
        btnReports.addActionListener(e -> new ReportsFrame().setVisible(true));

        setContentPane(root);
    }

    // =========================================================================
    // STAT CARD HELPER (Standard Swing/AWT components)
    // =========================================================================
    private JPanel createStatCard(String title, JLabel countLabel, String subtitle, Color themeColor) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(themeColor);

        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        countLabel.setForeground(Theme.TEXT);

        JLabel lblSub = new JLabel(subtitle, SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(Theme.MUTED);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(countLabel, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);

        return card;
    }

    // =========================================================================
    // NAVIGATION BUTTON HELPER (Standard JButton + Fonts)
    // =========================================================================
    private JButton createNavButton(String title, String subtitle, boolean enabled) {
        JButton btn = new JButton();
        btn.setLayout(new GridLayout(2, 1, 2, 2));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblMain = new JLabel(title);
        lblMain.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMain.setForeground(enabled ? Theme.TEXT : Theme.DISABLED);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(Theme.MUTED);

        btn.add(lblMain);
        btn.add(lblSub);

        return btn;
    }

    private void showAccessDenied(String message) {
        JOptionPane.showMessageDialog(this,
                "Access Denied!\n\n" + message + "\nYour current role is: " + currentUser.getRole(),
                "Permission Restricted",
                JOptionPane.WARNING_MESSAGE);
    }

    private void loadCounts() {
        lblFacultyCount.setText(String.valueOf(getCount("faculty")));
        lblSubjectCount.setText(String.valueOf(getCount("subjects")));
        lblDepartmentCount.setText(String.valueOf(getCount("departments")));
        lblRoomCount.setText(String.valueOf(getCount("rooms")));
    }

    private int getCount(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Unable to count " + tableName + ": " + e.getMessage());
        }
        return 0;
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}