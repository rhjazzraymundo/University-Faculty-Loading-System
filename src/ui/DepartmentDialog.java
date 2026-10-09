package ui;

import dao.DepartmentDAO;
import model.Department;

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
    private final JTextField txtName = new JTextField(20);
    private final JTable table = new JTable();
    private DefaultTableModel tableModel;

    public DepartmentDialog(JFrame parent) {
        super(parent, "Department Management", true);
        setSize(700, 480);
        setLocationRelativeTo(parent);

        buildUI();
        loadDepartments();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(new Color(241, 245, 249));

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59));
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

        // FORM CARD
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        form.add(new JLabel("Dept Code:"));
        form.add(txtCode);
        form.add(new JLabel("Dept Name:"));
        form.add(txtName);

        JButton btnAdd = createBtn("Add Department", new Color(37, 99, 235), 140);
        form.add(btnAdd);
        body.add(form, BorderLayout.NORTH);

        // TABLE CARD
        tableModel = new DefaultTableModel(new Object[]{"ID", "Department Code", "Department Name"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setModel(tableModel);
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        body.add(new JScrollPane(table), BorderLayout.CENTER);

        // BOTTOM
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton btnClose = createBtn("Close", new Color(100, 116, 139), 85);
        bottom.add(btnClose);
        body.add(bottom, BorderLayout.SOUTH);

        root.add(body, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> saveDepartment());
        btnClose.addActionListener(e -> dispose());

        setContentPane(root);
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

    private void loadDepartments() {
        tableModel.setRowCount(0);
        List<Department> list = departmentDAO.getAllDepartments();
        for (Department d : list) {
            tableModel.addRow(new Object[]{ d.getDepartmentId(), d.getDepartmentCode(), d.getDepartmentName() });
        }
    }

    private void saveDepartment() {
        String code = txtCode.getText().trim();
        String name = txtName.getText().trim();
        if (code.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Department Code and Name.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (departmentDAO.addDepartment(new Department(0, code, name))) {
                JOptionPane.showMessageDialog(this, "Department added successfully.");
                txtCode.setText("");
                txtName.setText("");
                loadDepartments();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding department:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}