package ui;

import dao.DepartmentDAO;
import model.Department;
import util.DbErrors;
import util.Validator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DepartmentDialog extends JDialog {

    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final JTextField txtCode = new JTextField(10);
    private final JTextField txtName = new JTextField(22);
    private final JTextField txtSearch = new JTextField(16);
    private final JTable table = new JTable();
    private DefaultTableModel tableModel;
    private int selectedId = -1;

    public DepartmentDialog(JFrame parent) {
        super(parent, "Department Management", true);
        setSize(780, 560);
        setLocationRelativeTo(parent);

        buildUI();
        loadDepartments();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.BACKGROUND);

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.HEADER);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));
        JLabel title = new JLabel("ACADEMIC DEPARTMENT REGISTRY");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // BODY
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 16, 12, 16));

        // TOP: form card + search card
        JPanel top = new JPanel(new GridLayout(2, 1, 0, 8));
        top.setOpaque(false);

        JPanel form = card();
        form.add(new JLabel("Dept Code:"));
        form.add(txtCode);
        form.add(new JLabel("Dept Name:"));
        form.add(txtName);
        top.add(form);

        JPanel actions = card();
        JButton btnSave = createBtn("Save", Theme.PRIMARY, 85);
        JButton btnUpdate = createBtn("Update", Theme.ACCENT, 85);
        JButton btnDelete = createBtn("Delete", Theme.DANGER, 85);
        JButton btnClear = createBtn("Clear", Theme.SECONDARY, 85);
        actions.add(btnSave);
        actions.add(btnUpdate);
        actions.add(btnDelete);
        actions.add(btnClear);
        actions.add(new JLabel("   Search:"));
        actions.add(txtSearch);
        JButton btnSearch = createBtn("Search", Theme.PRIMARY, 85);
        JButton btnShowAll = createBtn("Show All", Theme.SECONDARY, 90);
        actions.add(btnSearch);
        actions.add(btnShowAll);
        top.add(actions);

        body.add(top, BorderLayout.NORTH);

        // TABLE
        tableModel = new DefaultTableModel(new Object[]{"ID", "Department Code", "Department Name"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setModel(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Theme.styleTable(table);
        body.add(new JScrollPane(table), BorderLayout.CENTER);

        // BOTTOM
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton btnClose = createBtn("Close", Theme.SECONDARY, 85);
        bottom.add(btnClose);
        body.add(bottom, BorderLayout.SOUTH);

        root.add(body, BorderLayout.CENTER);

        // EVENTS
        btnSave.addActionListener(e -> saveDepartment());
        btnUpdate.addActionListener(e -> updateDepartment());
        btnDelete.addActionListener(e -> deleteDepartment());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> searchDepartments());
        txtSearch.addActionListener(e -> searchDepartments());
        btnShowAll.addActionListener(e -> { txtSearch.setText(""); loadDepartments(); });
        btnClose.addActionListener(e -> dispose());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) selectRow();
        });

        setContentPane(root);
    }

    private JPanel card() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1), new EmptyBorder(4, 12, 4, 12)));
        return p;
    }

    private JButton createBtn(String text, Color bg, int width) {
        JButton btn = new JButton(text);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(width, 30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ---------------------------------------------------------------- data
    private void loadDepartments() {
        try {
            display(departmentDAO.getAllDepartments());
        } catch (SQLException e) {
            DbErrors.show(this, "Error loading departments", e);
        }
    }

    private void searchDepartments() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) { loadDepartments(); return; }
        try {
            List<Department> found = departmentDAO.searchDepartments(keyword);
            display(found);
            if (found.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No matching departments found.",
                        "Search", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            DbErrors.show(this, "Error searching departments", e);
        }
    }

    private void display(List<Department> list) {
        tableModel.setRowCount(0);
        for (Department d : list) {
            tableModel.addRow(new Object[]{d.getDepartmentId(), d.getDepartmentCode(), d.getDepartmentName()});
        }
        selectedId = -1;
    }

    private void selectRow() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        selectedId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        txtCode.setText(tableModel.getValueAt(row, 1).toString());
        txtName.setText(tableModel.getValueAt(row, 2).toString());
    }

    // ------------------------------------------------------------- actions
    private Department readForm() {
        String code = txtCode.getText().trim();
        String name = txtName.getText().trim();
        if (Validator.isBlank(code) || Validator.isBlank(name)) {
            JOptionPane.showMessageDialog(this, "Please enter both Department Code and Name.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (code.length() > 20 || name.length() > 100) {
            JOptionPane.showMessageDialog(this, "Code is limited to 20 characters and Name to 100.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return new Department(selectedId, code, name);
    }

    private void saveDepartment() {
        Department d = readForm();
        if (d == null) return;
        try {
            if (departmentDAO.addDepartment(d)) {
                JOptionPane.showMessageDialog(this, "Department added successfully.");
                clearForm();
                loadDepartments();
            }
        } catch (SQLException ex) {
            DbErrors.show(this, "Error adding department", ex);
        }
    }

    private void updateDepartment() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a department to update.");
            return;
        }
        Department d = readForm();
        if (d == null) return;
        try {
            if (departmentDAO.updateDepartment(d)) {
                JOptionPane.showMessageDialog(this, "Department updated successfully.");
                clearForm();
                loadDepartments();
            } else {
                JOptionPane.showMessageDialog(this, "The department no longer exists.",
                        "Not Found", JOptionPane.WARNING_MESSAGE);
                loadDepartments();
            }
        } catch (SQLException ex) {
            DbErrors.show(this, "Error updating department", ex);
        }
    }

    private void deleteDepartment() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a department to delete.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this department?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            if (departmentDAO.deleteDepartment(selectedId)) {
                JOptionPane.showMessageDialog(this, "Department deleted successfully.");
                clearForm();
                loadDepartments();
            }
        } catch (SQLException ex) {
            DbErrors.show(this, "Cannot delete department", ex,
                    "This department still has faculty or subjects assigned to it.\n"
                  + "Reassign or delete them first.");
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtCode.setText("");
        txtName.setText("");
        table.clearSelection();
    }
}