package ui;

import database.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class RoomDialog extends JDialog {

    private final JTextField txtName = new JTextField(9);
    private final JTextField txtBuilding = new JTextField(12);
    private final JTextField txtCapacity = new JTextField(6);
    private final JComboBox<String> cmbType = new JComboBox<>(new String[]{"Lecture", "Laboratory"});
    private final JTable table = new JTable();
    private DefaultTableModel tableModel;

    public RoomDialog(JFrame parent) {
        super(parent, "Room Management", true);
        setSize(800, 500);
        setLocationRelativeTo(parent);

        buildUI();
        loadRooms();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(new Color(241, 245, 249));

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59));
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("ROOMS & FACILITIES DIRECTORY");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // BODY
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 16, 12, 16));

        // FORM CARD
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        form.add(new JLabel("Room:")); form.add(txtName);
        form.add(new JLabel("Building:")); form.add(txtBuilding);
        form.add(new JLabel("Capacity:")); form.add(txtCapacity);
        form.add(new JLabel("Type:")); form.add(cmbType);

        JButton btnAdd = createBtn("Add Room", new Color(37, 99, 235), 110);
        form.add(btnAdd);
        body.add(form, BorderLayout.NORTH);

        // TABLE CARD
        tableModel = new DefaultTableModel(new Object[]{"ID", "Room Name", "Building", "Capacity", "Type"}, 0) {
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

        btnAdd.addActionListener(e -> saveRoom());
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

    private void loadRooms() {
        tableModel.setRowCount(0);
        String sql = "SELECT room_id, room_name, building, capacity, room_type FROM rooms ORDER BY room_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("room_id"), rs.getString("room_name"),
                    rs.getString("building"), rs.getInt("capacity"), rs.getString("room_type")
                });
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
    }

    private void saveRoom() {
        String name = txtName.getText().trim();
        String capStr = txtCapacity.getText().trim();
        if (name.isEmpty() || capStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Room Name and Capacity.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int capacity;
        try {
            capacity = Integer.parseInt(capStr);
            if (capacity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Capacity must be a positive integer.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO rooms (room_name, building, capacity, room_type) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, txtBuilding.getText().trim().isEmpty() ? "Main Building" : txtBuilding.getText().trim());
            ps.setInt(3, capacity);
            ps.setString(4, (String) cmbType.getSelectedItem());

            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Room added successfully.");
                txtName.setText(""); txtBuilding.setText(""); txtCapacity.setText("");
                loadRooms();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding room:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}