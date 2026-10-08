package ui;

import dao.DepartmentDAO;
import dao.FacultyDAO;
import model.Department;
import model.Faculty;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class FacultyFrame extends JFrame {

    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    private final JTextField txtEmployeeNo = new JTextField(15);
    private final JTextField txtFirstName = new JTextField(15);
    private final JTextField txtLastName = new JTextField(15);
    private final JTextField txtEmail = new JTextField(15);
    private final JTextField txtContactNo = new JTextField(15);
    private final JTextField txtMaxUnits = new JTextField(15);
    private final JTextField txtSearch = new JTextField(20);

    private final JComboBox<Department> cmbDepartment =
            new JComboBox<>();

    private final JComboBox<String> cmbEmploymentType =
            new JComboBox<>(
                    new String[]{"Full-time", "Part-time"});

    private final JComboBox<String> cmbStatus =
            new JComboBox<>(
                    new String[]{"Active", "Inactive"});

    private final JTable table = new JTable();

    private int selectedFacultyId = -1;

    public FacultyFrame() {

        super("Faculty Management");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();

        loadDepartments();
        loadFaculty();

        setSize(1050, 650);
        setLocationRelativeTo(null);
    }

    private void buildUI() {

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 10, 10, 10));

        root.add(createFormPanel(), BorderLayout.NORTH);
        root.add(createTablePanel(), BorderLayout.CENTER);
        root.add(createSearchPanel(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    // =========================================
    // FORM
    // =========================================

    private JPanel createFormPanel() {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "Faculty Information"));

        GridBagConstraints c =
                new GridBagConstraints();

        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Row 1
        addField(panel, c, 0, 0,
                "Employee No:", txtEmployeeNo);

        addField(panel, c, 2, 0,
                "Department:", cmbDepartment);

        // Row 2
        addField(panel, c, 0, 1,
                "First Name:", txtFirstName);

        addField(panel, c, 2, 1,
                "Last Name:", txtLastName);

        // Row 3
        addField(panel, c, 0, 2,
                "Email:", txtEmail);

        addField(panel, c, 2, 2,
                "Contact No:", txtContactNo);

        // Row 4
        addField(panel, c, 0, 3,
                "Employment:", cmbEmploymentType);

        addField(panel, c, 2, 3,
                "Max Units:", txtMaxUnits);

        // Row 5
        addField(panel, c, 0, 4,
                "Status:", cmbStatus);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.LEFT));

        JButton btnSave = new JButton("Save");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnClear = new JButton("Clear");

        buttons.add(btnSave);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnClear);

        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 4;

        panel.add(buttons, c);

        btnSave.addActionListener(e -> saveFaculty());
        btnUpdate.addActionListener(e -> updateFaculty());
        btnDelete.addActionListener(e -> deleteFaculty());
        btnClear.addActionListener(e -> clearForm());

        return panel;
    }

    private void addField(
            JPanel panel,
            GridBagConstraints c,
            int x,
            int y,
            String label,
            java.awt.Component component) {

        c.gridx = x;
        c.gridy = y;
        c.gridwidth = 1;

        panel.add(new JLabel(label), c);

        c.gridx = x + 1;

        panel.add(component, c);
    }

    // =========================================
    // TABLE
    // =========================================

    private JPanel createTablePanel() {

        JPanel panel = new JPanel(
                new BorderLayout());

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "Faculty Records"));

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        table.setAutoCreateRowSorter(true);

        table.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        selectFaculty();
                    }
                });

        JScrollPane scrollPane =
                new JScrollPane(table);

        panel.add(scrollPane,
                BorderLayout.CENTER);

        return panel;
    }

    // =========================================
    // SEARCH
    // =========================================

    private JPanel createSearchPanel() {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.LEFT));

        panel.add(new JLabel("Search:"));
        panel.add(txtSearch);

        JButton btnSearch =
                new JButton("Search");

        JButton btnShowAll =
                new JButton("Show All");

        panel.add(btnSearch);
        panel.add(btnShowAll);

        btnSearch.addActionListener(
                e -> searchFaculty());

        btnShowAll.addActionListener(
                e -> loadFaculty());

        return panel;
    }

    // =========================================
    // LOAD DEPARTMENTS
    // =========================================

    private void loadDepartments() {

        try {

            cmbDepartment.removeAllItems();

            List<Department> departments =
                    departmentDAO.getAllDepartments();

            for (Department department :
                    departments) {

                cmbDepartment.addItem(
                        department);
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load departments.\n"
                    + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================
    // LOAD FACULTY
    // =========================================

    private void loadFaculty() {

        try {

            List<Faculty> facultyList =
                    facultyDAO.getAllFaculty();

            displayFaculty(facultyList);

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load faculty records.\n"
                    + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================
    // DISPLAY TABLE
    // =========================================

    private void displayFaculty(
            List<Faculty> facultyList) {

        String[] columns = {
            "ID",
            "Employee No.",
            "First Name",
            "Last Name",
            "Department ID",
            "Email",
            "Contact No.",
            "Employment",
            "Max Units",
            "Status"
        };

        DefaultTableModel model =
                new DefaultTableModel(columns, 0) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {
                        return false;
                    }
                };

        for (Faculty faculty :
                facultyList) {

            model.addRow(new Object[]{
                faculty.getFacultyId(),
                faculty.getEmployeeNo(),
                faculty.getFirstName(),
                faculty.getLastName(),
                faculty.getDepartmentId(),
                faculty.getEmail(),
                faculty.getContactNo(),
                faculty.getEmploymentType(),
                faculty.getMaxUnits(),
                faculty.getStatus()
            });
        }

        table.setModel(model);
    }

    // =========================================
    // SELECT ROW
    // =========================================

    private void selectFaculty() {

        int row = table.getSelectedRow();

        if (row == -1) {
            return;
        }

        int modelRow =
                table.convertRowIndexToModel(row);

        selectedFacultyId =
                Integer.parseInt(
                        table.getModel()
                                .getValueAt(
                                        modelRow, 0)
                                .toString());

        txtEmployeeNo.setText(
                table.getModel()
                        .getValueAt(modelRow, 1)
                        .toString());

        txtFirstName.setText(
                table.getModel()
                        .getValueAt(modelRow, 2)
                        .toString());

        txtLastName.setText(
                table.getModel()
                        .getValueAt(modelRow, 3)
                        .toString());

        int departmentId =
                Integer.parseInt(
                        table.getModel()
                                .getValueAt(modelRow, 4)
                                .toString());

        selectDepartment(departmentId);

        Object email =
                table.getModel()
                        .getValueAt(modelRow, 5);

        txtEmail.setText(
                email == null ? "" :
                        email.toString());

        Object contact =
                table.getModel()
                        .getValueAt(modelRow, 6);

        txtContactNo.setText(
                contact == null ? "" :
                        contact.toString());

        cmbEmploymentType.setSelectedItem(
                table.getModel()
                        .getValueAt(modelRow, 7)
                        .toString());

        txtMaxUnits.setText(
                table.getModel()
                        .getValueAt(modelRow, 8)
                        .toString());

        cmbStatus.setSelectedItem(
                table.getModel()
                        .getValueAt(modelRow, 9)
                        .toString());
    }

    private void selectDepartment(
            int departmentId) {

        for (int i = 0;
             i < cmbDepartment.getItemCount();
             i++) {

            Department d =
                    cmbDepartment.getItemAt(i);

            if (d.getDepartmentId()
                    == departmentId) {

                cmbDepartment.setSelectedIndex(i);
                break;
            }
        }
    }

    // =========================================
    // VALIDATION
    // =========================================

    private Faculty getFacultyFromForm() {

        String employeeNo =
                txtEmployeeNo.getText().trim();

        String firstName =
                txtFirstName.getText().trim();

        String lastName =
                txtLastName.getText().trim();

        String email =
                txtEmail.getText().trim();

        String contactNo =
                txtContactNo.getText().trim();

        String maxUnitsText =
                txtMaxUnits.getText().trim();

        if (employeeNo.isEmpty()
                || firstName.isEmpty()
                || lastName.isEmpty()
                || maxUnitsText.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please fill in all required fields.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);

            return null;
        }

        if (cmbDepartment.getSelectedItem()
                == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a department.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);

            return null;
        }

        int maxUnits;

        try {

            maxUnits =
                    Integer.parseInt(maxUnitsText);

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Max Units must be a number.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);

            return null;
        }

        if (maxUnits <= 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Max Units must be greater than 0.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);

            return null;
        }

        if (!email.isEmpty()
                && !email.matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid email address.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);

            return null;
        }

        Department department =
                (Department) cmbDepartment
                        .getSelectedItem();

        Faculty faculty =
                new Faculty();

        faculty.setFacultyId(
                selectedFacultyId);

        faculty.setEmployeeNo(employeeNo);
        faculty.setDepartmentId(
                department.getDepartmentId());
        faculty.setFirstName(firstName);
        faculty.setLastName(lastName);
        faculty.setEmail(
                email.isEmpty() ? null : email);
        faculty.setContactNo(
                contactNo.isEmpty() ? null :
                        contactNo);
        faculty.setEmploymentType(
                cmbEmploymentType
                        .getSelectedItem()
                        .toString());
        faculty.setMaxUnits(maxUnits);
        faculty.setStatus(
                cmbStatus
                        .getSelectedItem()
                        .toString());

        return faculty;
    }

    // =========================================
    // SAVE
    // =========================================

    private void saveFaculty() {

        Faculty faculty =
                getFacultyFromForm();

        if (faculty == null) {
            return;
        }

        try {

            if (facultyDAO.addFaculty(faculty)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Faculty saved successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                clearForm();
                loadFaculty();
            }

        } catch (SQLException e) {

            showDatabaseError(e);
        }
    }

    // =========================================
    // UPDATE
    // =========================================

    private void updateFaculty() {

        if (selectedFacultyId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a faculty record first.",
                    "Update",
                    JOptionPane.WARNING_MESSAGE);

            return;
        }

        Faculty faculty =
                getFacultyFromForm();

        if (faculty == null) {
            return;
        }

        try {

            if (facultyDAO.updateFaculty(faculty)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Faculty updated successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                clearForm();
                loadFaculty();
            }

        } catch (SQLException e) {

            showDatabaseError(e);
        }
    }

    // =========================================
    // DELETE
    // =========================================

    private void deleteFaculty() {

        if (selectedFacultyId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a faculty record first.",
                    "Delete",
                    JOptionPane.WARNING_MESSAGE);

            return;
        }

        int choice =
                JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to delete this faculty?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            if (facultyDAO.deleteFaculty(
                    selectedFacultyId)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Faculty deleted successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                clearForm();
                loadFaculty();
            }

        } catch (SQLException e) {

            showDatabaseError(e);
        }
    }

    // =========================================
    // SEARCH
    // =========================================

    private void searchFaculty() {

        String keyword =
                txtSearch.getText().trim();

        if (keyword.isEmpty()) {
            loadFaculty();
            return;
        }

        try {

            List<Faculty> results =
                    facultyDAO.searchFaculty(keyword);

            displayFaculty(results);

        } catch (SQLException e) {

            showDatabaseError(e);
        }
    }

    // =========================================
    // CLEAR
    // =========================================

    private void clearForm() {

        selectedFacultyId = -1;

        txtEmployeeNo.setText("");
        txtFirstName.setText("");
        txtLastName.setText("");
        txtEmail.setText("");
        txtContactNo.setText("");
        txtMaxUnits.setText("");

        if (cmbDepartment.getItemCount() > 0) {
            cmbDepartment.setSelectedIndex(0);
        }

        cmbEmploymentType.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);

        table.clearSelection();

        txtEmployeeNo.requestFocus();
    }

    private void showDatabaseError(
            SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "Database error:\n"
                + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
}