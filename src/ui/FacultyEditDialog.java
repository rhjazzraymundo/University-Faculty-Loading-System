package ui;

import dao.FacultyDAO;
import model.Department;
import model.Faculty;
import util.DbErrors;
import util.Validator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class FacultyEditDialog extends JDialog {

    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final Faculty faculty;
    private boolean updated = false;

    private final JTextField txtEmployeeNo = new JTextField(16);
    private final JTextField txtFirstName = new JTextField(16);
    private final JTextField txtLastName = new JTextField(16);
    private final JTextField txtEmail = new JTextField(16);
    private final JTextField txtContactNo = new JTextField(16);
    private final JTextField txtMaxUnits = new JTextField(16);
    private final JComboBox<Department> cmbDepartment = new JComboBox<>();
    private final JComboBox<String> cmbEmploymentType = new JComboBox<>(new String[]{"Full-time", "Part-time"});
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Active", "Inactive"});

    public FacultyEditDialog(Frame owner, Faculty faculty, List<Department> departments) {
        super(owner, "Update Faculty Record", true);
        this.faculty = faculty;

        for (Department d : departments) {
            cmbDepartment.addItem(d);
            if (d.getDepartmentId() == faculty.getDepartmentId()) cmbDepartment.setSelectedItem(d);
        }
        txtEmployeeNo.setText(faculty.getEmployeeNo());
        txtFirstName.setText(faculty.getFirstName());
        txtLastName.setText(faculty.getLastName());
        txtEmail.setText(faculty.getEmail() == null ? "" : faculty.getEmail());
        txtContactNo.setText(faculty.getContactNo() == null ? "" : faculty.getContactNo());
        txtMaxUnits.setText(String.valueOf(faculty.getMaxUnits()));
        cmbEmploymentType.setSelectedItem(faculty.getEmploymentType());
        cmbStatus.setSelectedItem(faculty.getStatus());

        buildUI();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension(640, getHeight()));
        setLocationRelativeTo(owner);
    }

    public boolean isUpdated() {
        return updated;
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.HEADER);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel title = new JLabel("UPDATE FACULTY RECORD");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Editing " + faculty.getEmployeeNo() + " - "
                + faculty.getFirstName() + " " + faculty.getLastName());
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(Theme.HEADER_SUB);
        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1), new EmptyBorder(14, 16, 14, 16)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        addRow(card, c, 0, "Employee No:", txtEmployeeNo, "Department:", cmbDepartment);
        addRow(card, c, 1, "First Name:", txtFirstName, "Last Name:", txtLastName);
        addRow(card, c, 2, "Email Address:", txtEmail, "Contact No:", txtContactNo);
        addRow(card, c, 3, "Employment:", cmbEmploymentType, "Max Units:", txtMaxUnits);
        addRow(card, c, 4, "Status:", cmbStatus, "", new JLabel(""));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 16, 0, 16));
        body.add(card, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.setOpaque(false);
        JButton btnSave = createBtn("Update", Theme.PRIMARY);
        JButton btnCancel = createBtn("Cancel", Theme.SECONDARY);
        buttons.add(btnSave);
        buttons.add(btnCancel);
        root.add(buttons, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> save());
        btnCancel.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(btnSave);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        setContentPane(root);
    }

    private void addRow(JPanel p, GridBagConstraints c, int y,
                        String l1, Component comp1, String l2, Component comp2) {
        c.gridy = y;
        c.gridx = 0; p.add(new JLabel(l1), c);
        c.gridx = 1; p.add(comp1, c);
        c.gridx = 2; p.add(new JLabel(l2), c);
        c.gridx = 3; p.add(comp2, c);
    }

    private JButton createBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(95, 30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private void save() {
        String emp = txtEmployeeNo.getText().trim();
        String first = txtFirstName.getText().trim();
        String last = txtLastName.getText().trim();
        String maxStr = txtMaxUnits.getText().trim();

        if (emp.isEmpty() || first.isEmpty() || last.isEmpty() || maxStr.isEmpty()) {
            warn("Please fill in all required fields (Employee No, Names, Max Units).");
            return;
        }
        int maxUnits;
        try {
            maxUnits = Integer.parseInt(maxStr);
            if (maxUnits <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            warn("Max Units must be a positive number.");
            return;
        }
        Department d = (Department) cmbDepartment.getSelectedItem();
        if (d == null) {
            warn("Please select a department.");
            return;
        }
        if (emp.length() > 20 || first.length() > 50 || last.length() > 50) {
            warn("Employee No is limited to 20 characters; names to 50 characters.");
            return;
        }
        if (!Validator.isValidEmailOrEmpty(txtEmail.getText())) {
            warn("Please enter a valid email address (example: name@university.edu).");
            return;
        }
        if (!Validator.isValidPhoneOrEmpty(txtContactNo.getText())) {
            warn("Contact No may only contain digits, spaces, +, - and ( ) (7-20 characters).");
            return;
        }

        Faculty f = new Faculty();
        f.setFacultyId(faculty.getFacultyId());
        f.setEmployeeNo(emp);
        f.setDepartmentId(d.getDepartmentId());
        f.setFirstName(first);
        f.setLastName(last);
        f.setEmail(txtEmail.getText().trim());
        f.setContactNo(txtContactNo.getText().trim());
        f.setEmploymentType((String) cmbEmploymentType.getSelectedItem());
        f.setMaxUnits(maxUnits);
        f.setStatus((String) cmbStatus.getSelectedItem());

        try {
            if (facultyDAO.updateFaculty(f)) {
                JOptionPane.showMessageDialog(this, "Faculty record updated successfully.");
                updated = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "The record no longer exists.",
                        "Not Found", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            DbErrors.show(this, "Database Error", e);
        }
    }
}