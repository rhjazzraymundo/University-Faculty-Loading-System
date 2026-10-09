package ui;

import dao.DepartmentDAO;
import dao.SubjectDAO;
import model.Department;
import model.Subject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SubjectFrame extends JFrame {

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    private final JTextField txtSubjectCode = new JTextField(15);
    private final JTextField txtSubjectTitle = new JTextField(25);
    private final JTextField txtUnits = new JTextField(8);
    private final JTextField txtSearch = new JTextField(18);
    private final JComboBox<Department> cmbDepartment = new JComboBox<>();

    private final JTable tblSubjects = new JTable();
    private DefaultTableModel tableModel;
    private int selectedSubjectId = -1;

    public SubjectFrame() {
        super("Subject Management");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();
        loadDepartments();
        loadSubjects();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(new Color(241, 245, 249));

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59));
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("SUBJECT & CURRICULUM MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Manage course offerings, titles, academic departments and unit weights");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(203, 213, 225));

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        // BODY
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(12, 20, 15, 20));

        // FORM CARD
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; formCard.add(new JLabel("Subject Code:"), c);
        c.gridx = 1; formCard.add(txtSubjectCode, c);
        c.gridx = 2; formCard.add(new JLabel("Units (1 - 6):"), c);
        c.gridx = 3; formCard.add(txtUnits, c);

        c.gridx = 0; c.gridy = 1; formCard.add(new JLabel("Subject Title:"), c);
        c.gridx = 1; formCard.add(txtSubjectTitle, c);
        c.gridx = 2; formCard.add(new JLabel("Department:"), c);
        c.gridx = 3; formCard.add(cmbDepartment, c);

        // FORM BUTTONS
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btnPanel.setOpaque(false);

        JButton btnSave = createBtn("Save", new Color(37, 99, 235));
        JButton btnUpdate = createBtn("Update", new Color(13, 148, 136));
        JButton btnDelete = createBtn("Delete", new Color(220, 38, 38));
        JButton btnClear = createBtn("Clear", new Color(100, 116, 139));

        btnPanel.add(btnSave);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 4;
        formCard.add(btnPanel, c);

        body.add(formCard, BorderLayout.NORTH);

        // TABLE CARD
        JPanel tableCard = new JPanel(new BorderLayout(0, 8));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        searchBar.add(new JLabel("Search Subject:"));
        searchBar.add(txtSearch);

        JButton btnSearch = createBtn("Search", new Color(30, 41, 59));
        JButton btnShowAll = createBtn("Show All", new Color(71, 85, 105));
        searchBar.add(btnSearch);
        searchBar.add(btnShowAll);

        tableCard.add(searchBar, BorderLayout.NORTH);

        tblSubjects.setRowHeight(26);
        tblSubjects.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblSubjects.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tableCard.add(new JScrollPane(tblSubjects), BorderLayout.CENTER);
        body.add(tableCard, BorderLayout.CENTER);

        root.add(body, BorderLayout.CENTER);
        setContentPane(root);

        // EVENTS
        btnSave.addActionListener(e -> saveSubject());
        btnUpdate.addActionListener(e -> updateSubject());
        btnDelete.addActionListener(e -> deleteSubject());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> searchSubjects());
        btnShowAll.addActionListener(e -> loadSubjects());
        txtSearch.addActionListener(e -> searchSubjects());

        tblSubjects.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblSubjects.getSelectedRow() != -1) {
                selectSubject();
            }
        });
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
        cmbDepartment.removeAllItems();
        List<Department> list = departmentDAO.getAllDepartments();
        for (Department d : list) cmbDepartment.addItem(d);
    }

    private void loadSubjects() {
        displaySubjects(subjectDAO.getAllSubjects());
    }

    private void searchSubjects() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) { loadSubjects(); return; }
        displaySubjects(subjectDAO.searchSubjects(keyword));
    }

    private void displaySubjects(List<Subject> list) {
        String[] cols = {"ID", "Subject Code", "Subject Title", "Units", "Department"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Subject s : list) {
            tableModel.addRow(new Object[]{
                s.getSubjectId(), s.getSubjectCode(), s.getSubjectTitle(), s.getUnits(), getDeptName(s.getDepartmentId())
            });
        }
        tblSubjects.setModel(tableModel);
    }

    private String getDeptName(int id) {
        for (int i = 0; i < cmbDepartment.getItemCount(); i++) {
            Department d = cmbDepartment.getItemAt(i);
            if (d.getDepartmentId() == id) return d.toString();
        }
        return "Unknown";
    }

    private void selectSubject() {
        int r = tblSubjects.getSelectedRow();
        if (r == -1) return;

        selectedSubjectId = Integer.parseInt(tableModel.getValueAt(r, 0).toString());
        txtSubjectCode.setText(tableModel.getValueAt(r, 1).toString());
        txtSubjectTitle.setText(tableModel.getValueAt(r, 2).toString());
        txtUnits.setText(tableModel.getValueAt(r, 3).toString());

        String deptName = tableModel.getValueAt(r, 4).toString();
        for (int i = 0; i < cmbDepartment.getItemCount(); i++) {
            if (cmbDepartment.getItemAt(i).toString().equals(deptName)) {
                cmbDepartment.setSelectedIndex(i);
                break;
            }
        }
    }

    private void saveSubject() {
        Subject s = getSubjectFromForm();
        if (s == null) return;
        if (subjectDAO.addSubject(s)) {
            JOptionPane.showMessageDialog(this, "Subject saved successfully.");
            clearForm();
            loadSubjects();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save subject.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSubject() {
        if (selectedSubjectId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subject to update.");
            return;
        }
        Subject s = getSubjectFromForm();
        if (s == null) return;
        s.setSubjectId(selectedSubjectId);
        if (subjectDAO.updateSubject(s)) {
            JOptionPane.showMessageDialog(this, "Subject updated successfully.");
            clearForm();
            loadSubjects();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update subject.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSubject() {
        if (selectedSubjectId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subject to delete.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this subject?", "Confirm Delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        if (subjectDAO.deleteSubject(selectedSubjectId)) {
            JOptionPane.showMessageDialog(this, "Subject deleted successfully.");
            clearForm();
            loadSubjects();
        } else {
            JOptionPane.showMessageDialog(this, "Cannot delete subject (it may be scheduled).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Subject getSubjectFromForm() {
        String code = txtSubjectCode.getText().trim();
        String title = txtSubjectTitle.getText().trim();
        String unitsStr = txtUnits.getText().trim();

        if (code.isEmpty() || title.isEmpty() || unitsStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all subject details.", "Validation", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        int units;
        try {
            units = Integer.parseInt(unitsStr);
            if (units < 1 || units > 6) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Units must be a number between 1 and 6.", "Validation", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Department d = (Department) cmbDepartment.getSelectedItem();
        if (d == null) {
            JOptionPane.showMessageDialog(this, "Please select a department.", "Validation", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Subject s = new Subject();
        s.setSubjectCode(code);
        s.setSubjectTitle(title);
        s.setUnits(units);
        s.setDepartmentId(d.getDepartmentId());
        return s;
    }

    private void clearForm() {
        selectedSubjectId = -1;
        txtSubjectCode.setText("");
        txtSubjectTitle.setText("");
        txtUnits.setText("");
        if (cmbDepartment.getItemCount() > 0) cmbDepartment.setSelectedIndex(0);
        tblSubjects.clearSelection();
    }
}