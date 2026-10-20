package ui;

import dao.RoomDAO;
import model.Room;
import util.DbErrors;
import util.Validator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class RoomDialog extends JDialog {

    private final RoomDAO roomDAO = new RoomDAO();
    private final JTextField txtName = new JTextField(10);
    private final JTextField txtBuilding = new JTextField(14);
    private final JTextField txtCapacity = new JTextField(5);
    private final JComboBox<String> cmbType = new JComboBox<>(new String[]{"Lecture", "Laboratory"});
    private final JTextField txtSearch = new JTextField(16);
    private final JTable table = new JTable();
    private DefaultTableModel tableModel;
    private int selectedId = -1;

    public RoomDialog(JFrame parent) {
        super(parent, "Room Management", true);
        setSize(820, 580);
        setLocationRelativeTo(parent);

        buildUI();
        loadRooms();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(Theme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.HEADER);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));
        JLabel title = new JLabel("ROOMS & FACILITIES");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 16, 12, 16));

        JPanel top = new JPanel(new GridLayout(2, 1, 0, 8));
        top.setOpaque(false);

        JPanel form = card();
        form.add(new JLabel("Room Name:"));
        form.add(txtName);
        form.add(new JLabel("Building:"));
        form.add(txtBuilding);
        form.add(new JLabel("Capacity:"));
        form.add(txtCapacity);
        form.add(new JLabel("Type:"));
        form.add(cmbType);
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

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Room Name", "Building", "Capacity", "Type"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setModel(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Theme.styleTable(table);
        body.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton btnClose = createBtn("Close", Theme.SECONDARY, 85);
        bottom.add(btnClose);
        body.add(bottom, BorderLayout.SOUTH);

        root.add(body, BorderLayout.CENTER);

        btnSave.addActionListener(e -> saveRoom());
        btnUpdate.addActionListener(e -> updateRoom());
        btnDelete.addActionListener(e -> deleteRoom());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> searchRooms());
        txtSearch.addActionListener(e -> searchRooms());
        btnShowAll.addActionListener(e -> { txtSearch.setText(""); loadRooms(); });
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

    private void loadRooms() {
        try {
            display(roomDAO.getAllRooms());
        } catch (SQLException e) {
            DbErrors.show(this, "Error loading rooms", e);
        }
    }

    private void searchRooms() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) { loadRooms(); return; }
        try {
            List<Room> found = roomDAO.searchRooms(keyword);
            display(found);
            if (found.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No matching rooms found.",
                        "Search", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            DbErrors.show(this, "Error searching rooms", e);
        }
    }

    private void display(List<Room> list) {
        tableModel.setRowCount(0);
        for (Room r : list) {
            tableModel.addRow(new Object[]{r.getRoomId(), r.getRoomName(), r.getBuilding(),
                    r.getCapacity(), r.getRoomType()});
        }
        selectedId = -1;
    }

    private void selectRow() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        selectedId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        txtName.setText(tableModel.getValueAt(row, 1).toString());
        Object building = tableModel.getValueAt(row, 2);
        txtBuilding.setText(building == null ? "" : building.toString());
        txtCapacity.setText(tableModel.getValueAt(row, 3).toString());
        cmbType.setSelectedItem(tableModel.getValueAt(row, 4).toString());
    }

    private Room readForm() {
        String name = txtName.getText().trim();
        String capStr = txtCapacity.getText().trim();
        if (Validator.isBlank(name) || Validator.isBlank(capStr)) {
            JOptionPane.showMessageDialog(this, "Please enter Room Name and Capacity.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (name.length() > 30 || txtBuilding.getText().trim().length() > 50) {
            JOptionPane.showMessageDialog(this, "Room Name is limited to 30 characters and Building to 50.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Integer capacity = Validator.parseInt(capStr);
        if (capacity == null || capacity <= 0) {
            JOptionPane.showMessageDialog(this, "Capacity must be a positive whole number.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String building = txtBuilding.getText().trim();
        return new Room(selectedId, name,
                building.isEmpty() ? "Main Building" : building,
                capacity, (String) cmbType.getSelectedItem());
    }

    private void saveRoom() {
        Room r = readForm();
        if (r == null) return;
        try {
            if (roomDAO.addRoom(r)) {
                JOptionPane.showMessageDialog(this, "Room added successfully.");
                clearForm();
                loadRooms();
            }
        } catch (SQLException ex) {
            DbErrors.show(this, "Error adding room", ex);
        }
    }

    private void updateRoom() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room to update.");
            return;
        }
        Room r = readForm();
        if (r == null) return;
        try {
            if (roomDAO.updateRoom(r)) {
                JOptionPane.showMessageDialog(this, "Room updated successfully.");
                clearForm();
                loadRooms();
            } else {
                JOptionPane.showMessageDialog(this, "The room no longer exists.",
                        "Not Found", JOptionPane.WARNING_MESSAGE);
                loadRooms();
            }
        } catch (SQLException ex) {
            DbErrors.show(this, "Error updating room", ex);
        }
    }

    private void deleteRoom() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room to delete.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this room?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            if (roomDAO.deleteRoom(selectedId)) {
                JOptionPane.showMessageDialog(this, "Room deleted successfully.");
                clearForm();
                loadRooms();
            }
        } catch (SQLException ex) {
            DbErrors.show(this, "Cannot delete room", ex,
                    "This room is used by one or more schedules.\n"
                  + "Move or delete those schedules first.");
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtName.setText("");
        txtBuilding.setText("");
        txtCapacity.setText("");
        cmbType.setSelectedIndex(0);
        table.clearSelection();
    }
}