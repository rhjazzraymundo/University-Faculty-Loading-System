package ui;

import dao.DepartmentDAO;
import dao.SubjectDAO;
import model.Department;
import model.Subject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SubjectFrame extends JFrame {

    private JTextField txtSubjectCode;
    private JTextField txtSubjectTitle;
    private JTextField txtUnits;
    private JTextField txtSearch;

    private JComboBox<Department> cmbDepartment;

    private JTable tblSubjects;
    private DefaultTableModel tableModel;

    private JButton btnSave;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    private int selectedSubjectId = -1;

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SubjectFrame() {

        setTitle("Subject Management");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initializeUI();

        loadDepartments();
        loadSubjects();
    }


    // =========================================================
    // UI
    // =========================================================

    private void initializeUI() {

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        // =====================================================
        // TITLE
        // =====================================================

        JLabel lblTitle = new JLabel("SUBJECT MANAGEMENT");

        lblTitle.setFont(
                new Font("Arial", Font.BOLD, 22)
        );

        mainPanel.add(lblTitle, BorderLayout.NORTH);


        // =====================================================
        // FORM PANEL
        // =====================================================

        JPanel formPanel = new JPanel(
                new GridBagLayout()
        );

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        // Subject Code

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;

        formPanel.add(
                new JLabel("Subject Code:"),
                gbc
        );

        txtSubjectCode = new JTextField();

        gbc.gridx = 1;
        gbc.weightx = 1;

        formPanel.add(
                txtSubjectCode,
                gbc
        );


        // Subject Title

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;

        formPanel.add(
                new JLabel("Subject Title:"),
                gbc
        );

        txtSubjectTitle = new JTextField();

        gbc.gridx = 1;
        gbc.weightx = 1;

        formPanel.add(
                txtSubjectTitle,
                gbc
        );


        // Units

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;

        formPanel.add(
                new JLabel("Units:"),
                gbc
        );

        txtUnits = new JTextField();

        gbc.gridx = 1;
        gbc.weightx = 1;

        formPanel.add(
                txtUnits,
                gbc
        );


        // Department

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;

        formPanel.add(
                new JLabel("Department:"),
                gbc
        );

        cmbDepartment = new JComboBox<>();

        gbc.gridx = 1;
        gbc.weightx = 1;

        formPanel.add(
                cmbDepartment,
                gbc
        );


        // =====================================================
        // BUTTON PANEL
        // =====================================================

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 5)
        );

        btnSave = new JButton("Save");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");

        buttonPanel.add(btnSave);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);


        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 1;

        formPanel.add(
                buttonPanel,
                gbc
        );


        // =====================================================
        // SEARCH PANEL
        // =====================================================

        JPanel searchPanel = new JPanel(
                new BorderLayout(10, 5)
        );

        JLabel lblSearch = new JLabel("Search:");

        txtSearch = new JTextField();

        JButton btnSearch = new JButton("Search");
        JButton btnShowAll = new JButton("Show All");

        searchPanel.add(
                lblSearch,
                BorderLayout.WEST
        );

        searchPanel.add(
                txtSearch,
                BorderLayout.CENTER
        );

        JPanel searchButtons = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 5, 0)
        );

        searchButtons.add(btnSearch);
        searchButtons.add(btnShowAll);

        searchPanel.add(
                searchButtons,
                BorderLayout.EAST
        );


        // =====================================================
        // TOP CENTER PANEL
        // =====================================================

        JPanel topPanel = new JPanel(
                new BorderLayout(10, 10)
        );

        topPanel.add(
                formPanel,
                BorderLayout.CENTER
        );

        topPanel.add(
                searchPanel,
                BorderLayout.SOUTH
        );


        mainPanel.add(
                topPanel,
                BorderLayout.CENTER
        );


        // =====================================================
        // TABLE
        // =====================================================

        String[] columns = {
                "ID",
                "Subject Code",
                "Subject Title",
                "Units",
                "Department"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblSubjects = new JTable(tableModel);

        tblSubjects.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblSubjects.setRowHeight(25);

        tblSubjects.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(50);

        tblSubjects.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(100);

        tblSubjects.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(300);

        tblSubjects.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(70);

        tblSubjects.getColumnModel()
                .getColumn(4)
                .setPreferredWidth(250);


        JScrollPane scrollPane = new JScrollPane(
                tblSubjects
        );

        JPanel tablePanel = new JPanel(
                new BorderLayout()
        );

        tablePanel.add(
                scrollPane,
                BorderLayout.CENTER
        );


        // =====================================================
        // MAIN LAYOUT
        // =====================================================

        JPanel contentPanel = new JPanel(
                new BorderLayout(10, 10)
        );

        contentPanel.add(
                topPanel,
                BorderLayout.NORTH
        );

        contentPanel.add(
                tablePanel,
                BorderLayout.CENTER
        );

        mainPanel.removeAll();

        mainPanel.add(
                lblTitle,
                BorderLayout.NORTH
        );

        mainPanel.add(
                contentPanel,
                BorderLayout.CENTER
        );

        add(mainPanel);


        // =====================================================
        // BUTTON EVENTS
        // =====================================================

        btnSave.addActionListener(e -> saveSubject());

        btnUpdate.addActionListener(e -> updateSubject());

        btnDelete.addActionListener(e -> deleteSubject());

        btnClear.addActionListener(e -> clearForm());

        btnSearch.addActionListener(e -> searchSubjects());

        btnShowAll.addActionListener(e -> loadSubjects());


        // Search when pressing Enter

        txtSearch.addActionListener(e -> searchSubjects());


        // =====================================================
        // TABLE ROW SELECTION
        // =====================================================

        tblSubjects.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        selectSubject();
                    }

                });
    }


    // =========================================================
    // LOAD DEPARTMENTS
    // =========================================================

    private void loadDepartments() {

        cmbDepartment.removeAllItems();

        List<Department> departments =
                departmentDAO.getAllDepartments();

        for (Department department : departments) {

            cmbDepartment.addItem(department);
        }
    }


    // =========================================================
    // LOAD SUBJECTS
    // =========================================================

    private void loadSubjects() {

        tableModel.setRowCount(0);

        List<Subject> subjects =
                subjectDAO.getAllSubjects();

        for (Subject subject : subjects) {

            String departmentName =
                    getDepartmentName(
                            subject.getDepartmentId()
                    );

            tableModel.addRow(
                    new Object[]{
                            subject.getSubjectId(),
                            subject.getSubjectCode(),
                            subject.getSubjectTitle(),
                            subject.getUnits(),
                            departmentName
                    }
            );
        }
    }


    // =========================================================
    // SEARCH
    // =========================================================

    private void searchSubjects() {

        String keyword =
                txtSearch.getText().trim();

        if (keyword.isEmpty()) {

            loadSubjects();
            return;
        }

        tableModel.setRowCount(0);

        List<Subject> subjects =
                subjectDAO.searchSubjects(keyword);

        for (Subject subject : subjects) {

            String departmentName =
                    getDepartmentName(
                            subject.getDepartmentId()
                    );

            tableModel.addRow(
                    new Object[]{
                            subject.getSubjectId(),
                            subject.getSubjectCode(),
                            subject.getSubjectTitle(),
                            subject.getUnits(),
                            departmentName
                    }
            );
        }
    }


    // =========================================================
    // SAVE
    // =========================================================

    private void saveSubject() {

        if (!validateForm()) {
            return;
        }

        Subject subject =
                getSubjectFromForm();

        if (subjectDAO.addSubject(subject)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Subject saved successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearForm();
            loadSubjects();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Failed to save subject.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    // =========================================================
    // UPDATE
    // =========================================================

    private void updateSubject() {

        if (selectedSubjectId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a subject first."
            );

            return;
        }

        if (!validateForm()) {
            return;
        }

        Subject subject =
                getSubjectFromForm();

        subject.setSubjectId(
                selectedSubjectId
        );

        if (subjectDAO.updateSubject(subject)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Subject updated successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearForm();
            loadSubjects();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Failed to update subject.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    // =========================================================
    // DELETE
    // =========================================================

    private void deleteSubject() {

        if (selectedSubjectId == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a subject first."
            );

            return;
        }

        int confirm =
                JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to delete this subject?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        if (subjectDAO.deleteSubject(
                selectedSubjectId
        )) {

            JOptionPane.showMessageDialog(
                    this,
                    "Subject deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearForm();
            loadSubjects();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete subject.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    // =========================================================
    // CLEAR
    // =========================================================

    private void clearForm() {

        txtSubjectCode.setText("");
        txtSubjectTitle.setText("");
        txtUnits.setText("");

        if (cmbDepartment.getItemCount() > 0) {
            cmbDepartment.setSelectedIndex(0);
        }

        tblSubjects.clearSelection();

        selectedSubjectId = -1;

        txtSubjectCode.requestFocus();
    }


    // =========================================================
    // SELECT TABLE ROW
    // =========================================================

    private void selectSubject() {

        int row =
                tblSubjects.getSelectedRow();

        if (row == -1) {
            return;
        }

        selectedSubjectId =
                Integer.parseInt(
                        tableModel.getValueAt(row, 0)
                                .toString()
                );

        txtSubjectCode.setText(
                tableModel.getValueAt(row, 1)
                        .toString()
        );

        txtSubjectTitle.setText(
                tableModel.getValueAt(row, 2)
                        .toString()
        );

        txtUnits.setText(
                tableModel.getValueAt(row, 3)
                        .toString()
        );

        String departmentName =
                tableModel.getValueAt(row, 4)
                        .toString();

        for (int i = 0;
             i < cmbDepartment.getItemCount();
             i++) {

            Department department =
                    cmbDepartment.getItemAt(i);

            if (department.toString()
                    .equals(departmentName)) {

                cmbDepartment.setSelectedIndex(i);
                break;
            }
        }
    }


    // =========================================================
    // GET SUBJECT FROM FORM
    // =========================================================

    private Subject getSubjectFromForm() {

        Subject subject = new Subject();

        subject.setSubjectCode(
                txtSubjectCode.getText().trim()
        );

        subject.setSubjectTitle(
                txtSubjectTitle.getText().trim()
        );

        subject.setUnits(
                Integer.parseInt(
                        txtUnits.getText().trim()
                )
        );

        Department department =
                (Department) cmbDepartment.getSelectedItem();

        if (department != null) {

            subject.setDepartmentId(
                    department.getDepartmentId()
            );
        }

        return subject;
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private boolean validateForm() {

        String code =
                txtSubjectCode.getText().trim();

        String title =
                txtSubjectTitle.getText().trim();

        String unitsText =
                txtUnits.getText().trim();


        if (code.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Subject code is required."
            );

            txtSubjectCode.requestFocus();

            return false;
        }


        if (title.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Subject title is required."
            );

            txtSubjectTitle.requestFocus();

            return false;
        }


        if (unitsText.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Units are required."
            );

            txtUnits.requestFocus();

            return false;
        }


        int units;

        try {

            units =
                    Integer.parseInt(unitsText);

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Units must be a whole number."
            );

            txtUnits.requestFocus();

            return false;
        }


        // Matches database CHECK constraint:
        // units BETWEEN 1 AND 6

        if (units < 1 || units > 6) {

            JOptionPane.showMessageDialog(
                    this,
                    "Units must be between 1 and 6."
            );

            txtUnits.requestFocus();

            return false;
        }


        if (cmbDepartment.getSelectedItem() == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a department."
            );

            return false;
        }


        return true;
    }


    // =========================================================
    // GET DEPARTMENT NAME
    // =========================================================

    private String getDepartmentName(int departmentId) {

        for (int i = 0;
             i < cmbDepartment.getItemCount();
             i++) {

            Department department =
                    cmbDepartment.getItemAt(i);

            if (department.getDepartmentId()
                    == departmentId) {

                return department.toString();
            }
        }

        return "Unknown";
    }
}