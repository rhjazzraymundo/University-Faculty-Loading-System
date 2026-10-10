package ui;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private final JTextField txtUsername = new JTextField(16);
    private final JPasswordField txtPassword = new JPasswordField(16);
    private final JButton btnLogin = new JButton("Login");
    private final JButton btnClear = new JButton("Clear");
    private final JButton btnExit = new JButton("Exit");
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        super("Faculty Loading System - Authentication");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        buildUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);

        // Header Banner
        JPanel header = new JPanel(new GridLayout(2, 1, 2, 2));
        header.setBackground(Theme.HEADER);
        header.setBorder(new EmptyBorder(18, 25, 18, 25));

        JLabel lblTitle = new JLabel("UNIVERSITY FACULTY LOADING SYSTEM", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Secure System Access", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(Theme.HEADER_SUB);

        header.add(lblTitle);
        header.add(lblSub);
        root.add(header, BorderLayout.NORTH);

        // Login Card
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(25, 30, 20, 30));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 13);

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(labelFont);
        txtUsername.setFont(inputFont);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(labelFont);
        txtPassword.setFont(inputFont);

        c.gridx = 0; c.gridy = 0;
        card.add(lblUser, c);
        c.gridx = 1;
        card.add(txtUsername, c);

        c.gridx = 0; c.gridy = 1;
        card.add(lblPass, c);
        c.gridx = 1;
        card.add(txtPassword, c);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttonPanel.setOpaque(false);

        styleButton(btnLogin, Theme.PRIMARY, Color.WHITE);
        styleButton(btnClear, Theme.SECONDARY, Color.WHITE);
        styleButton(btnExit, Theme.DANGER, Color.WHITE);

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnExit);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        c.insets = new Insets(18, 0, 5, 0);
        card.add(buttonPanel, c);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);
        getRootPane().setDefaultButton(btnLogin);

        btnLogin.addActionListener(e -> doLogin());
        btnClear.addActionListener(e -> doClear());
        btnExit.addActionListener(e -> doExit());
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(90, 32));
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your username and password.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = userDAO.authenticate(username, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this,
                        "Incorrect username or password, or account is inactive.",
                        "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                return;
            }
            new DashboardFrame(user).setVisible(true);
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error:\n" + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doClear() {
        txtUsername.setText("");
        txtPassword.setText("");
        txtUsername.requestFocusInWindow();
    }

    private void doExit() {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}