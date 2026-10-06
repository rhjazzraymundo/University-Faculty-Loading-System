package ui;

import model.User;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Temporary dashboard so a successful login has somewhere to go.
 * Day 3 replaces this with the real dashboard (menu, counts, Faculty module).
 */
public class DashboardFrame extends JFrame {

    private final User currentUser;

    public DashboardFrame(User user) {
        super("Faculty Loading System - Dashboard");
        this.currentUser = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel welcome = new JLabel("Welcome, " + user.getFullName() + "!", SwingConstants.CENTER);
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 18f));
        JLabel role = new JLabel("Role: " + user.getRole(), SwingConstants.CENTER);
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> logout());

        JPanel center = new JPanel(new java.awt.GridLayout(2, 1, 0, 6));
        center.add(welcome);
        center.add(role);
        JPanel bottom = new JPanel();
        bottom.add(btnLogout);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(30, 50, 20, 50));
        root.add(center, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);

        pack();
        setLocationRelativeTo(null);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    /** Ends the session and returns to the login screen. */
    private void logout() {
        new LoginFrame().setVisible(true);
        dispose();
    }
}
