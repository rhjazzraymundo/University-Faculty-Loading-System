package ui;

import database.DatabaseConnection;
import model.User;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class DashboardFrame extends JFrame {

    private final User currentUser;

    private final JLabel lblFacultyCount =
            new JLabel("0", SwingConstants.CENTER);

    private final JLabel lblSubjectCount =
            new JLabel("0", SwingConstants.CENTER);

    private final JLabel lblDepartmentCount =
            new JLabel("0", SwingConstants.CENTER);

    private final JLabel lblRoomCount =
            new JLabel("0", SwingConstants.CENTER);

    public DashboardFrame(User user) {

        super("University Faculty Loading System - Dashboard");

        this.currentUser = user;

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE);

        buildUI();

        loadCounts();

        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    private void buildUI() {

        JPanel root =
                new JPanel(new BorderLayout(10, 10));

        root.setBorder(
                BorderFactory.createEmptyBorder(
                        15, 15, 15, 15));

        // ==============================
        // TOP
        // ==============================

        JPanel top =
                new JPanel(new BorderLayout());

        JLabel title =
                new JLabel(
                        "UNIVERSITY FACULTY LOADING SYSTEM",
                        SwingConstants.CENTER);

        title.setFont(
                title.getFont().deriveFont(
                        Font.BOLD, 22f));

        JLabel welcome =
                new JLabel(
                        "Welcome, "
                        + currentUser.getFullName()
                        + " | Role: "
                        + currentUser.getRole(),
                        SwingConstants.CENTER);

        top.add(title,
                BorderLayout.NORTH);

        top.add(welcome,
                BorderLayout.SOUTH);

        root.add(top,
                BorderLayout.NORTH);

        // ==============================
        // COUNTS
        // ==============================

        JPanel cards =
                new JPanel(new GridLayout(
                        1, 4, 15, 15));

        cards.add(
                createCountCard(
                        "FACULTY",
                        lblFacultyCount));

        cards.add(
                createCountCard(
                        "SUBJECTS",
                        lblSubjectCount));

        cards.add(
                createCountCard(
                        "DEPARTMENTS",
                        lblDepartmentCount));

        cards.add(
                createCountCard(
                        "ROOMS",
                        lblRoomCount));

        root.add(cards,
                BorderLayout.CENTER);

        // ==============================
        // MENU
        // ==============================

        JPanel menu =
                new JPanel();

        JButton btnFaculty =
                new JButton("Faculty");

        JButton btnSubjects =
                new JButton("Subjects");

        JButton btnDepartments =
                new JButton("Departments");

        JButton btnRooms =
                new JButton("Rooms");

        JButton btnLogout =
                new JButton("Logout");

        menu.add(btnFaculty);
        menu.add(btnSubjects);
        menu.add(btnDepartments);
        menu.add(btnRooms);
        menu.add(btnLogout);

        root.add(menu,
                BorderLayout.SOUTH);

        btnFaculty.addActionListener(
                e -> openFaculty());

        btnLogout.addActionListener(
                e -> logout());

    
        btnSubjects.addActionListener(e -> {
         SubjectFrame subjectFrame = new SubjectFrame();
             subjectFrame.setVisible(true);
        });

        btnDepartments.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        "Department module will be added next."));

        btnRooms.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        "Room module will be added next."));

        setContentPane(root);
    }

    private JPanel createCountCard(
            String title,
            JLabel countLabel) {

        JPanel card =
                new JPanel(new BorderLayout());

        card.setBorder(
                BorderFactory.createTitledBorder(
                        title));

        countLabel.setFont(
                countLabel.getFont().deriveFont(
                        Font.BOLD, 36f));

        card.add(
                countLabel,
                BorderLayout.CENTER);

        return card;
    }

    // ==============================
    // COUNTS
    // ==============================

    private void loadCounts() {

        lblFacultyCount.setText(
                String.valueOf(
                        getCount("faculty")));

        lblSubjectCount.setText(
                String.valueOf(
                        getCount("subjects")));

        lblDepartmentCount.setText(
                String.valueOf(
                        getCount("departments")));

        lblRoomCount.setText(
                String.valueOf(
                        getCount("rooms")));
    }

    private int getCount(String tableName) {

        String sql =
                "SELECT COUNT(*) FROM "
                + tableName;

        try (Connection conn =
                     DatabaseConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs =
                     ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Unable to count "
                    + tableName
                    + ": "
                    + e.getMessage());
        }

        return 0;
    }

    // ==============================
    // FACULTY
    // ==============================

    private void openFaculty() {

        FacultyFrame frame =
                new FacultyFrame();

        frame.setVisible(true);
    }

    // ==============================
    // LOGOUT
    // ==============================

    private void logout() {

        int choice =
                JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to logout?",
                        "Logout",
                        JOptionPane.YES_NO_OPTION);

        if (choice ==
                JOptionPane.YES_OPTION) {

            new LoginFrame().setVisible(true);

            dispose();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }
}