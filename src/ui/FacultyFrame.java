package ui;

import dao.DepartmentDAO;
import dao.FacultyDAO;
import model.Department;
import model.Faculty;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class FacultyFrame extends JFrame {

    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    private final JTextField txtEmployeeNo = new JTextField(14);
    private final JTextField txtFirstName = new JTextField(14);
    private final JTextField txtLastName = new JTextField(14);
    private final JTextField txtEmail = new JTextField(14);
    private final JTextField txtContactNo = new JTextField(14);
    private final JTextField txtMaxUnits = new JTextField(14);
    private final JTextField txtSearch = new JTextField(18);

    private final JComboBox<Department> cmbDepartment = new JComboBox<>();
    private final JComboBox<String> cmbEmploymentType = new JComboBox<>(new String[]{"Full-time", "Part-time"});
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"Active", "Inactive"});

    private final JTable table = new JTable();
    private DefaultTableModel tableModel;
    private int selectedFacultyId = -1;

    public FacultyFrame() {
        super("Faculty Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1080, 680);
        setLocationRelativeTo(null);

        buildUI();
        loadDepartments();
        loadFaculty();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.BACKGROUND);

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.HEADER);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("FACULTY INSTRUCTOR MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Register and maintain academic instructor profiles & max load");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(Theme.HEADER_SUB);

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        // BODY CONTAINER
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(12, 20, 15, 20));

        // FORM CARD
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        addFormRow(formCard, c, 0, 0, "Employee No:", txtEmployeeNo, "Department:", cmbDepartment);
        addFormRow(formCard, c, 0, 1, "First Name:", txtFirstName, "Last Name:", txtLastName);
        addFormRow(formCard, c, 0, 2, "Email Address:", txtEmail, "Contact No:", txtContactNo);
        addFormRow(formCard, c, 0, 3, "Employment:", cmbEmploymentType, "Max Units:", txtMaxUnits);
        addFormRow(formCard, c, 0, 4, "Status:", cmbStatus, "", new JLabel(""));

        // FORM BUTTONS
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

        c.gridx = 0; c.gridy = 5; c.gridwidth = 4;
        formCard.add(btnPanel, c);

        body.add(formCard, BorderLayout.NORTH);

        // SEARCH & TABLE CARD
        JPanel tableCard = new JPanel(new BorderLayout(0, 8));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Search Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        searchBar.add(new JLabel("Search Keyword:"));
        searchBar.add(txtSearch);

        JButton btnSearch = createBtn("Search", Theme.PRIMARY);
        JButton btnShowAll = createBtn("Show All", Theme.SECONDARY);
        searchBar.add(btnSearch);
        searchBar.add(btnShowAll);

        tableCard.add(searchBar, BorderLayout.NORTH);

        // Table
        table.setRowHeight(26);
        Theme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        body.add(tableCard, BorderLayout.CENTER);

        root.add(body, BorderLayout.CENTER);
        setContentPane(root);

        // LISTENERS
        btnSave.addActionListener(e -> saveFaculty());
        btnUpdate.addActionListener(e -> updateFaculty());
        btnDelete.addActionListener(e -> deleteFaculty());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> searchFaculty());
        btnShowAll.addActionListener(e -> loadFaculty());
        txtSearch.addActionListener(e -> searchFaculty());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                selectFaculty();
            }
        });
    }

    private void addFormRow(JPanel p, GridBagConstraints c, int x, int y,
                            String l1, Component comp1, String l2, Component comp2) {
        c.gridy = y; c.gridwidth = 1;
        c.gridx = x; p.add(new JLabel(l1), c);
        c.gridx = x + 1; p.add(comp1, c);
        c.gridx = x + 2; p.add(new JLabel(l2), c);
        c.gridx = x + 3; p.add(comp2, c);
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

    private void loadDepartments() {
        try {
            cmbDepartment.removeAllItems();
            List<Department> depts = departmentDAO.getAllDepartments();
            for (Department d : depts) cmbDepartment.addItem(d);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading departments: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadFaculty() {
        try {
            displayFaculty(facultyDAO.getAllFaculty());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading faculty: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchFaculty() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) { loadFaculty(); return; }
        try {
            displayFaculty(facultyDAO.searchFaculty(keyword));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayFaculty(List<Faculty> list) {
        String[] cols = {"ID", "Employee No", "First Name", "Last Name", "Dept ID", "Email", "Contact", "Type", "Max Units", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Faculty f : list) {
            tableModel.addRow(new Object[]{
                f.getFacultyId(), f.getEmployeeNo(), f.getFirstName(), f.getLastName(),
                f.getDepartmentId(), f.getEmail(), f.getContactNo(), f.getEmploymentType(),
                f.getMaxUnits(), f.getStatus()
            });
        }
        table.setModel(tableModel);
    }

    private void selectFaculty() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        selectedFacultyId = Integer.parseInt(table.getValueAt(row, 0).toString());
        txtEmployeeNo.setText(table.getValueAt(row, 1).toString());
        txtFirstName.setText(table.getValueAt(row, 2).toString());
        txtLastName.setText(table.getValueAt(row, 3).toString());

        int deptId = Integer.parseInt(table.getValueAt(row, 4).toString());
        for (int i = 0; i < cmbDepartment.getItemCount(); i++) {
            if (cmbDepartment.getItemAt(i).getDepartmentId() == deptId) {
                cmbDepartment.setSelectedIndex(i);
                break;
            }
        }

        Object email = table.getValueAt(row, 5);
        txtEmail.setText(email == null ? "" : email.toString());

        Object contact = table.getValueAt(row, 6);
        txtContactNo.setText(contact == null ? "" : contact.toString());

        cmbEmploymentType.setSelectedItem(table.getValueAt(row, 7).toString());
        txtMaxUnits.setText(table.getValueAt(row, 8).toString());
        cmbStatus.setSelectedItem(table.getValueAt(row, 9).toString());
    }

    private void saveFaculty() {
        Faculty faculty = getFacultyFromForm();
        if (faculty == null) return;
        try {
            if (facultyDAO.addFaculty(faculty)) {
                JOptionPane.showMessageDialog(this, "Faculty record saved successfully.");
                clearForm();
                loadFaculty();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFaculty() {
        if (selectedFacultyId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a faculty record to update.");
            return;
        }
        Faculty faculty = getFacultyFromForm();
        if (faculty == null) return;
        try {
            if (facultyDAO.updateFaculty(faculty)) {
                JOptionPane.showMessageDialog(this, "Faculty record updated successfully.");
                clearForm();
                loadFaculty();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteFaculty() {
        if (selectedFacultyId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a faculty record to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this faculty member?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            if (facultyDAO.deleteFaculty(selectedFacultyId)) {
                JOptionPane.showMessageDialog(this, "Faculty member deleted.");
                clearForm();
                loadFaculty();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Cannot delete faculty (may be assigned to active schedules):\n" + e.getMessage(),
                    "Constraint Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Faculty getFacultyFromForm() {
        String emp = txtEmployeeNo.getText().trim();
        String first = txtFirstName.getText().trim();
        String last = txtLastName.getText().trim();
        String maxStr = txtMaxUnits.getText().trim();

        if (emp.isEmpty() || first.isEmpty() || last.isEmpty() || maxStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields (Employee No, Names, Max Units).",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        int maxUnits;
        try {
            maxUnits = Integer.parseInt(maxStr);
            if (maxUnits <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Max Units must be a positive number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Department d = (Department) cmbDepartment.getSelectedItem();
        if (d == null) {
            JOptionPane.showMessageDialog(this, "Please select a department.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Faculty f = new Faculty();
        f.setFacultyId(selectedFacultyId);
        f.setEmployeeNo(emp);
        f.setDepartmentId(d.getDepartmentId());
        f.setFirstName(first);
        f.setLastName(last);
        f.setEmail(txtEmail.getText().trim());
        f.setContactNo(txtContactNo.getText().trim());
        f.setEmploymentType((String) cmbEmploymentType.getSelectedItem());
        f.setMaxUnits(maxUnits);
        f.setStatus((String) cmbStatus.getSelectedItem());
        return f;
    }

    private void clearForm() {
        selectedFacultyId = -1;
        txtEmployeeNo.setText("");
        txtFirstName.setText("");
        txtLastName.setText("");
        txtEmail.setText("");
        txtContactNo.setText("");
        txtMaxUnits.setText("");
        if (cmbDepartment.getItemCount() > 0) cmbDepartment.setSelectedIndex(0);
        cmbEmploymentType.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
        table.clearSelection();
    }
}