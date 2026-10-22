package ui;

import dao.SubjectDAO;
import model.Department;
import model.Subject;
import util.DbErrors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class SubjectEditDialog extends JDialog {

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final Subject subject;
    private boolean updated = false;

    private final JTextField txtSubjectCode = new JTextField(16);
    private final JTextField txtSubjectTitle = new JTextField(16);
    private final JTextField txtUnits = new JTextField(16);
    private final JComboBox<Department> cmbDepartment = new JComboBox<>();

    public SubjectEditDialog(Frame owner, Subject subject, List<Department> departments) {
        super(owner, "Update Subject Record", true);
        this.subject = subject;

        for (Department d : departments) {
            cmbDepartment.addItem(d);
            if (d.getDepartmentId() == subject.getDepartmentId()) cmbDepartment.setSelectedItem(d);
        }
        txtSubjectCode.setText(subject.getSubjectCode());
        txtSubjectTitle.setText(subject.getSubjectTitle());
        txtUnits.setText(String.valueOf(subject.getUnits()));

        buildUI();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension(600, getHeight()));
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
        JLabel title = new JLabel("UPDATE SUBJECT RECORD");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Editing " + subject.getSubjectCode() + " - " + subject.getSubjectTitle());
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

        addRow(card, c, 0, "Subject Code:", txtSubjectCode, "Units (1 - 6):", txtUnits);
        addRow(card, c, 1, "Subject Title:", txtSubjectTitle, "Department:", cmbDepartment);

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
        JOptionPane.showMessageDialog(this, msg, "Validation", JOptionPane.WARNING_MESSAGE);
    }

    private void save() {
        String code = txtSubjectCode.getText().trim();
        String title = txtSubjectTitle.getText().trim();
        String unitsStr = txtUnits.getText().trim();

        if (code.isEmpty() || title.isEmpty() || unitsStr.isEmpty()) {
            warn("Please fill in all subject details.");
            return;
        }
        int units;
        try {
            units = Integer.parseInt(unitsStr);
            if (units < 1 || units > 6) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            warn("Units must be a number between 1 and 6.");
            return;
        }
        Department d = (Department) cmbDepartment.getSelectedItem();
        if (d == null) {
            warn("Please select a department.");
            return;
        }

        Subject s = new Subject();
        s.setSubjectId(subject.getSubjectId());
        s.setSubjectCode(code);
        s.setSubjectTitle(title);
        s.setUnits(units);
        s.setDepartmentId(d.getDepartmentId());

        try {
            if (subjectDAO.updateSubject(s)) {
                JOptionPane.showMessageDialog(this, "Subject updated successfully.");
                updated = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "The subject no longer exists.",
                        "Not Found", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            DbErrors.show(this, "Error updating subject", e);
        }
    }
}