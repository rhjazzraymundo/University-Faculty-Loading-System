package ui;

import dao.UserDAO;
import model.User;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/** System login screen: username, JPasswordField, Login / Clear / Exit. */
public class LoginFrame extends JFrame {

    private final JTextField txtUsername = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final JButton btnLogin = new JButton("Login");
    private final JButton btnClear = new JButton("Clear");
    private final JButton btnExit = new JButton("Exit");
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        super("Faculty Loading System - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        buildUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        txtUsername.requestFocusInWindow();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);

        JLabel title = new JLabel("SYSTEM LOGIN", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        JLabel subtitle = new JLabel("University Faculty Loading System", SwingConstants.CENTER);

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(title, c);
        c.gridy = 1;
        panel.add(subtitle, c);

        c.gridwidth = 1; c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.EAST;
        c.gridy = 2; c.gridx = 0; panel.add(new JLabel("Username:"), c);
        c.gridy = 3; panel.add(new JLabel("Password:"), c);

        c.anchor = GridBagConstraints.WEST;
        c.gridy = 2; c.gridx = 1; panel.add(txtUsername, c);
        c.gridy = 3; panel.add(txtPassword, c);

        JPanel buttons = new JPanel();
        buttons.add(btnLogin);
        buttons.add(btnClear);
        buttons.add(btnExit);
        c.gridy = 4; c.gridx = 0; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        panel.add(buttons, c);

        setContentPane(panel);
        getRootPane().setDefaultButton(btnLogin);   // Enter key = Login

        btnLogin.addActionListener(e -> doLogin());
        btnClear.addActionListener(e -> doClear());
        btnExit.addActionListener(e -> doExit());
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your username and password.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            (username.isEmpty() ? txtUsername : txtPassword).requestFocusInWindow();
            return;
        }

        try {
            User user = userDAO.authenticate(username, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this,
                        "Incorrect username or password, or the account is inactive.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                txtPassword.requestFocusInWindow();
                return;
            }
            new DashboardFrame(user).setVisible(true);
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Cannot connect to the database.\nMake sure the Derby Network Server is running.\n\n"
                    + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doClear() {
        txtUsername.setText("");
        txtPassword.setText("");
        txtUsername.requestFocusInWindow();
    }

    private void doExit() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?",
                "Exit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
